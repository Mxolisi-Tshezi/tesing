package com.sumer.sumerstores.services;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

    @Value("${cloud.aws.end-point.uri}")
    private String queueUrl;

    private final AmazonSQS amazonSQS;
    private final ObjectMapper objectMapper;

    public QueueService(AmazonSQS amazonSQS, ObjectMapper objectMapper) {
        this.amazonSQS = amazonSQS;
        this.objectMapper = objectMapper;
    }

    /**
     * Send any type of message to the queue
     *
     * @param message   the message object to send
     * @param eventType the type of event associated with this message
     * @param <T>       the type of the message
     */
    public <T> void sendMessageToQueue(T message, String eventType) {
        MessageWrapper<T> wrappedMessage = new MessageWrapper<>();
        wrappedMessage.setMessage(message);
        wrappedMessage.setEventType(eventType);

        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(serializeMessage(wrappedMessage));
        amazonSQS.sendMessage(sendMessageRequest);
    }

    /**
     * Serialize the message into a JSON string
     *
     * @param message the message object to serialize
     * @param <T>     the type of the message
     * @return the serialized JSON string
     */
    private <T> String serializeMessage(MessageWrapper<T> message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize message", e);
        }
    }}