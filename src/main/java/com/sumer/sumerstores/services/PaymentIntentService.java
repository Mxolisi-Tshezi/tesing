package com.sumer.sumerstores.services;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.PaymentIntentCreateParams;
import com.sumer.sumerstores.auth.dto.StripeSubscriptionDto;
import com.sumer.sumerstores.auth.dto.StripeSubscriptionResponse;
import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.entities.Order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PaymentIntentService {

    @Autowired
    private QueueService queueService;

    /**
     * Creates a PaymentIntent for an order.
     *
     * @param order The order for which the PaymentIntent is created.
     * @return A map containing the client secret for the PaymentIntent.
     * @throws StripeException If there is an error with the Stripe API.
     */
    public Map<String, String> createPaymentIntent(Order order) throws StripeException {
        User user = order.getUser();
        Map<String, String> metaData = new HashMap<>();
        metaData.put("orderId", order.getId().toString());
        PaymentIntentCreateParams paymentIntentCreateParams = PaymentIntentCreateParams.builder()
                .setAmount((long) (order.getTotalAmount() * 100 * 80)) // USD to INR conversion
                .setCurrency("zar") // INR currency
                .putAllMetadata(metaData)
                .setDescription("Test Payment Sumer Project")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build();
        PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentCreateParams);

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("client_secret", paymentIntent.getClientSecret());

        // Send a message to the queue about the payment intent creation
        queueService.sendMessageToQueue(order.getId(), "CREATE_PAYMENT_INTENT");

        return responseMap;
    }

    /**
     * Creates a subscription using Stripe.
     *
     * @param subscriptionDto The DTO containing subscription details.
     * @return A response containing subscription details.
     */
    public StripeSubscriptionResponse createSubscription(StripeSubscriptionDto subscriptionDto) {
        PaymentMethod paymentMethod = createPaymentMethod(subscriptionDto);
        Customer customer = createCustomer(paymentMethod, subscriptionDto);
        paymentMethod = attachCustomerToPaymentMethod(customer, paymentMethod);
        Subscription subscription = createSubscription(subscriptionDto, paymentMethod, customer);

        return createResponse(subscriptionDto, paymentMethod, customer, subscription);
    }

    /**
     * Creates a PaymentMethod for the subscription.
     *
     * @param subscriptionDto The DTO containing subscription details.
     * @return The created PaymentMethod.
     */
    private PaymentMethod createPaymentMethod(StripeSubscriptionDto subscriptionDto) {
        try {
            Map<String, Object> cardDetails = new HashMap<>();
            cardDetails.put("number", subscriptionDto.getCardNumber());
            cardDetails.put("exp_month", subscriptionDto.getExpMonth());
            cardDetails.put("exp_year", subscriptionDto.getExpYear());
            cardDetails.put("cvc", subscriptionDto.getCvc());

            Map<String, Object> paymentMethodParams = new HashMap<>();
            paymentMethodParams.put("type", "card");
            paymentMethodParams.put("card", cardDetails);

            return PaymentMethod.create(paymentMethodParams);
        } catch (StripeException e) {
            log.error("StripeService (createPaymentMethod)", e);
            throw new RuntimeException("Failed to create payment method: " + e.getMessage());
        }
    }

    /**
     * Creates a Customer in Stripe.
     *
     * @param paymentMethod   The PaymentMethod to associate with the customer.
     * @param subscriptionDto The DTO containing subscription details.
     * @return The created Customer.
     */
    private Customer createCustomer(PaymentMethod paymentMethod, StripeSubscriptionDto subscriptionDto) {
        try {
            Map<String, Object> customerParams = new HashMap<>();
            customerParams.put("name", subscriptionDto.getUsername());
            customerParams.put("email", subscriptionDto.getEmail());
            customerParams.put("payment_method", paymentMethod.getId());

            return Customer.create(customerParams);
        } catch (StripeException e) {
            log.error("StripeService (createCustomer)", e);
            throw new RuntimeException("Failed to create customer: " + e.getMessage());
        }
    }

    /**
     * Attaches a PaymentMethod to a Customer.
     *
     * @param customer      The Customer to attach the PaymentMethod to.
     * @param paymentMethod The PaymentMethod to attach.
     * @return The updated PaymentMethod.
     */
    private PaymentMethod attachCustomerToPaymentMethod(Customer customer, PaymentMethod paymentMethod) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("customer", customer.getId());

            return paymentMethod.attach(params);
        } catch (StripeException e) {
            log.error("StripeService (attachCustomerToPaymentMethod)", e);
            throw new RuntimeException("Failed to attach payment method to customer: " + e.getMessage());
        }
    }

    /**
     * Creates a Subscription in Stripe.
     *
     * @param subscriptionDto The DTO containing subscription details.
     * @param paymentMethod   The PaymentMethod to use for the subscription.
     * @param customer        The Customer to associate with the subscription.
     * @return The created Subscription.
     */
    private Subscription createSubscription(StripeSubscriptionDto subscriptionDto, PaymentMethod paymentMethod, Customer customer) {
        try {
            List<Object> items = new ArrayList<>();
            Map<String, Object> item1 = new HashMap<>();
            item1.put("price", subscriptionDto.getPriceId());
            item1.put("quantity", subscriptionDto.getNumberOfLicense());
            items.add(item1);

            Map<String, Object> params = new HashMap<>();
            params.put("customer", customer.getId());
            params.put("default_payment_method", paymentMethod.getId());
            params.put("items", items);

            return Subscription.create(params);
        } catch (StripeException e) {
            log.error("StripeService (createSubscription)", e);
            throw new RuntimeException("Failed to create subscription: " + e.getMessage());
        }
    }

    /**
     * Creates a response for the subscription creation.
     *
     * @param subscriptionDto The DTO containing subscription details.
     * @param paymentMethod   The PaymentMethod used for the subscription.
     * @param customer        The Customer associated with the subscription.
     * @param subscription    The created Subscription.
     * @return The response containing subscription details.
     */
    private StripeSubscriptionResponse createResponse(StripeSubscriptionDto subscriptionDto, PaymentMethod paymentMethod, Customer customer, Subscription subscription) {
        return StripeSubscriptionResponse.builder()
                .username(subscriptionDto.getUsername())
                .stripePaymentMethodId(paymentMethod.getId())
                .stripeSubscriptionId(subscription.getId())
                .stripeCustomerId(customer.getId())
                .build();
    }

    /**
     * Cancels a subscription in Stripe.
     *
     * @param subscriptionId The ID of the subscription to cancel.
     * @return The canceled Subscription.
     */
    public Subscription cancelSubscription(String subscriptionId) {
        try {
            Subscription retrieve = Subscription.retrieve(subscriptionId);
            return retrieve.cancel();
        } catch (StripeException e) {
            log.error("StripeService (cancelSubscription)", e);
            throw new RuntimeException("Failed to cancel subscription: " + e.getMessage());
        }
    }
}