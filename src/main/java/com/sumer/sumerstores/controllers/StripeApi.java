package com.sumer.sumerstores.controllers;


import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.sumer.sumerstores.auth.dto.StripeSubscriptionDto;
import com.sumer.sumerstores.auth.dto.StripeSubscriptionResponse;
import com.sumer.sumerstores.dto.StripeChargeDto;
import com.sumer.sumerstores.dto.StripeTokenDto;
import com.sumer.sumerstores.record.SubscriptionCancelRecord;
import com.sumer.sumerstores.services.StripeService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/public/stripe")
@AllArgsConstructor
public class StripeApi {

    private final StripeService stripeService;


    @PostMapping("/card/token")
    @ResponseBody
    public StripeTokenDto createCardToken(@RequestBody StripeTokenDto model) {


        return stripeService.createCardToken(model);
    }
    @PostMapping("/charge")
    @ResponseBody
    public StripeChargeDto charge(@RequestBody StripeChargeDto model) {

        return stripeService.charge(model);
    }

    @PostMapping("/customer/subscription")
    @ResponseBody
    public StripeSubscriptionResponse subscription(@RequestBody StripeSubscriptionDto model) throws StripeException {

        return stripeService.createSubscription(model);
    }

    @DeleteMapping("/subscription/{id}")
    @ResponseBody
    public SubscriptionCancelRecord cancelSubscription(@PathVariable String id){

        Subscription subscription = stripeService.cancelSubscription(id);
        if(nonNull(subscription)){

            return new SubscriptionCancelRecord(subscription.getStatus());
        }

        return null;
    }

}

