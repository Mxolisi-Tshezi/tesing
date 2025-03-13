package com.sumer.sumerstores.controllers;

import com.sumer.sumerstores.dto.SessionDto;
import com.sumer.sumerstores.entities.Order;
import com.sumer.sumerstores.services.StripeService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/v2/stripe")
@AllArgsConstructor
public class StripeV2Api {
    private final StripeService stripeService;
    @PostMapping("/session/payment")
    @ResponseBody
    public SessionDto sessionPayment(@RequestBody SessionDto model, Order order) {

        return stripeService.createPaymentSession(model, order);
    }
    @PostMapping("/session/subscription")
    @ResponseBody
    public SessionDto createSubscriptionSession(@RequestBody SessionDto model) {

        return stripeService.createSubscriptionSession(model);
    }


}

