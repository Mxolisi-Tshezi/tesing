package com.sumer.sumerstores.services;

public class MessageWrapper<T> {
    private T message;
    private String eventType;

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}

