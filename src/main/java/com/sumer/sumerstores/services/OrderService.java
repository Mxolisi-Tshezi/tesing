package com.sumer.sumerstores.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.sumer.sumerstores.auth.dto.OrderResponse;
import com.sumer.sumerstores.auth.dto.StripeSubscriptionDto;
import com.sumer.sumerstores.auth.dto.StripeSubscriptionResponse;
import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.dto.*;
import com.sumer.sumerstores.entities.*;
import com.sumer.sumerstores.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private QueueService queueService;

    @Autowired
    private OzowPaymentService ozowPaymentService;

    @Value("${bank.reference}")
    private String bankReference;

    @Value("${ozow.site.code}")
    private String ozowSiteCode;

    @Autowired
    private PaypalService paypalService;


    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest, Principal principal) throws Exception {
        log.info("Creating order for user: {}", principal.getName());

        User user = (User) userDetailsService.loadUserByUsername(principal.getName());
        log.info("User retrieved: {}", user.getEmail());

        Address address = user.getAddressList().stream()
                .filter(address1 -> orderRequest.getAddressId().equals(address1.getId()))
                .findFirst().orElseThrow(() -> new BadRequestException("Invalid address ID: " + orderRequest.getAddressId()));

        log.info("Selected address: {}", address.getStreet());

        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalAmount(orderRequest.getTotalAmount())
                .orderDate(orderRequest.getOrderDate())
                .discount(orderRequest.getDiscount())
                .expectedDeliveryDate(orderRequest.getExpectedDeliveryDate())
                .paymentMethod(orderRequest.getPaymentMethod())
                .orderStatus(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = orderRequest.getOrderItemRequests().stream().map(orderItemRequest -> {
            try {
                Product product = productService.fetchProductById(orderItemRequest.getProductId());
                log.info("Product retrieved: {}", product.getName());
                return OrderItem.builder()
                        .product(product)
                        .productVariantId(orderItemRequest.getProductVariantId())
                        .quantity(orderItemRequest.getQuantity())
                        .order(order)
                        .build();
            } catch (Exception e) {
                log.error("Error retrieving product: {}", orderItemRequest.getProductId(), e);
                throw new RuntimeException(e);
            }
        }).toList();

        order.setOrderItemList(orderItems);

        PaymentStripe paymentStripe = new PaymentStripe();
        paymentStripe.setPaymentStatus(PaymentStatus.PENDING);
        paymentStripe.setPaymentDate(new Date());
        paymentStripe.setOrder(order);
        paymentStripe.setAmount(order.getTotalAmount());
        paymentStripe.setPaymentMethod(order.getPaymentMethod());
        order.setPaymentStripe(paymentStripe);

        log.info("Saving order...");
        Order savedOrder = orderRepository.save(order);
        log.info("Order saved with ID: {}", savedOrder.getId());

        // Publish order event to queue
        queueService.sendMessageToQueue(savedOrder, "OrderCreated");

        OrderResponse orderResponse = OrderResponse.builder()
                .paymentMethod(orderRequest.getPaymentMethod())
                .orderId(savedOrder.getId())
                .build();

        // Stripe payment flow
        if ("STRIPE".equals(orderRequest.getPaymentMethod())) {
            log.info("Creating payment session for order: {}", savedOrder.getId());

            SessionDto sessionDto = new SessionDto();
            sessionDto.setUserId(user.getId().toString());
            sessionDto.setData(Map.of("order_id", savedOrder.getId().toString()));

            SessionDto sessionResponse = stripeService.createPaymentSession(sessionDto, order);
            orderResponse.setCredentials(Map.of("sessionUrl", sessionResponse.getSessionUrl()));
            queueService.sendMessageToQueue(savedOrder, "PaymentSessionCreated");
        }

        // Ozow payment flow
        if ("OZOW".equals(orderRequest.getPaymentMethod())) {
            log.info("Initiating Ozow payment for order: {}", savedOrder.getId());

            OzowPaymentDto ozowPaymentDto = OzowPaymentDto.builder()
                    .siteCode(ozowSiteCode)
                    .countryCode("ZA")
                    .currencyCode("ZAR")
                    .amount(BigDecimal.valueOf(order.getTotalAmount()))
                    .transactionReference(savedOrder.getId().toString())
                    .bankReference(bankReference)
                    .cancelUrl("https://localhost:5000/cancel")
                    .errorUrl("https://localhost:5000/error")
                    .successUrl("https://localhost:5000/success")
                    .notifyUrl("https://localhost:5000/notify")
                    .isTest(true)
                    .customerIdentifier(orderRequest.getCustomerIdentifier())
                    .build();

            String ozowResponse = ozowPaymentService.initiateOzowPayment(ozowPaymentDto);
            log.info("Ozow response: {}", ozowResponse);

            Map<String, String> ozowResponseMap = parseOzowResponse(ozowResponse);
            orderResponse.setCredentials(ozowResponseMap);
        }

        // PayPal payment flow
        if ("PAYPAL".equals(orderRequest.getPaymentMethod())) {
            log.info("Initiating PayPal payment for order: {}", savedOrder.getId());

            try {
                String cancelUrl = "https://locahost:5000/payment/cancel";
                String successUrl = "https://locahost:5000/payment/success";
                Payment paypalPaymentStripe = paypalService.createPayment(
                        order.getTotalAmount(),
                        "USD", // Currency for PayPal
                        "paypal",
                        "sale",
                        "Order #" + savedOrder.getId(),
                        cancelUrl,
                        successUrl
                );

                for (Links link : paypalPaymentStripe.getLinks()) {
                    if (link.getRel().equals("approval_url")) {
                        orderResponse.setCredentials(Map.of("approvalUrl", link.getHref()));
                        queueService.sendMessageToQueue(savedOrder, "PayPalPaymentCreated");
                        break;
                    }
                }

            } catch (PayPalRESTException e) {
                log.error("PayPal payment error for order: {}", savedOrder.getId(), e);
                throw new RuntimeException("PayPal payment failed", e);
            }
        }

        return orderResponse;
    }

    public StripeSubscriptionResponse createSubscription(StripeSubscriptionDto subscriptionDto) {
        return stripeService.createSubscription(subscriptionDto);
    }

    public Subscription cancelSubscription(String subscriptionId) {
        return stripeService.cancelSubscription(subscriptionId);
    }

    private Map<String, String> parseOzowResponse(String ozowResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseMap = objectMapper.readValue(ozowResponse, new TypeReference<Map<String, Object>>() {});
            Map<String, String> result = new HashMap<>();
            result.put("paymentRequestId", (String) responseMap.get("paymentRequestId"));
            result.put("url", (String) responseMap.get("url"));
            result.put("errorMessage", (String) responseMap.get("errorMessage"));
            return result;
        } catch (Exception e) {
            log.error("Error parsing Ozow response", e);
            return Collections.emptyMap();
        }
    }

    public Map<String, String> updateStatus(String sessionId, String status) {
        try {
            Session session = Session.retrieve(sessionId);
            if (session != null && session.getPaymentStatus().equals("paid")) {
                String orderId = session.getMetadata().get("order_id");
                Order order = orderRepository.findById(Long.valueOf(orderId)).orElseThrow(BadRequestException::new);
                PaymentStripe paymentStripe = order.getPaymentStripe();
                paymentStripe.setPaymentStatus(PaymentStatus.COMPLETED);
                paymentStripe.setPaymentMethod(session.getPaymentMethodTypes().get(0));
                order.setPaymentMethod(session.getPaymentMethodTypes().get(0));
                order.setOrderStatus(OrderStatus.IN_PROGRESS);
                order.setPaymentStripe(paymentStripe);
                Order savedOrder = orderRepository.save(order);
                queueService.sendMessageToQueue(savedOrder, "PaymentCompleted");
                Map<String, String> map = new HashMap<>();
                map.put("orderId", String.valueOf(savedOrder.getId()));

                return map;
            } else {
                throw new IllegalArgumentException("Session not found or payment not completed");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Session not found or payment not completed");
        }
    }

    public List<OrderDetails> getOrdersByUser(String name) {
        User user = (User) userDetailsService.loadUserByUsername(name);
        List<Order> orders = orderRepository.findByUser(user);
        return orders.stream().map(order -> OrderDetails.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .shipmentNumber(order.getShipmentTrackingNumber())
                .address(order.getAddress())
                .totalAmount(order.getTotalAmount())
                .orderItemList(getItemDetails(order.getOrderItemList()))
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .build()).toList();
    }

    private List<OrderItemDetail> getItemDetails(List<OrderItem> orderItemList) {
        return orderItemList.stream().map(orderItem -> OrderItemDetail.builder()
                .id(orderItem.getId())
                .itemPrice(orderItem.getItemPrice())
                .product(orderItem.getProduct())
                .productVariantId(orderItem.getProductVariantId())
                .quantity(orderItem.getQuantity())
                .build()).toList();
    }

    public void cancelOrder(Long id, Principal principal) {
        User user = (User) userDetailsService.loadUserByUsername(principal.getName());
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getUser().getId().equals(user.getId())) {
            order.setOrderStatus(OrderStatus.CANCELLED);

            // Logic to refund amount
            if ("STRIPE".equals(order.getPaymentMethod())) {
                try {
                    // Retrieve the payment session associated with the order
                    PaymentStripe paymentStripe = order.getPaymentStripe();
                    if (paymentStripe != null && paymentStripe.getPaymentStatus() == PaymentStatus.COMPLETED) {
                        // Create a refund using the Stripe API
                        Map<String, Object> refundParams = new HashMap<>();
                        refundParams.put("payment_intent", paymentStripe.getId());
                        refundParams.put("amount", (long) (order.getTotalAmount() * 100)); // Amount in cents
                        Refund refund = Refund.create(refundParams);
                        paymentStripe.setPaymentStatus(PaymentStatus.REFUNDED);
                        order.setPaymentStripe(paymentStripe);

                        log.info("Refund processed successfully for order: {}", order.getId());
                    } else {
                        log.warn("Payment not completed or payment session not found for order: {}", order.getId());
                    }
                } catch (StripeException e) {
                    log.error("Error processing refund for order: {}", order.getId(), e);
                    throw new RuntimeException("Failed to process refund: " + e.getMessage());
                }
            } else {
                log.warn("Refund logic not implemented for payment method: {}", order.getPaymentMethod());
            }
            queueService.sendMessageToQueue(order, "OrderCancelled");

            orderRepository.save(order);
        } else {
            throw new RuntimeException("Invalid request");
        }
    }
}