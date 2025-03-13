package com.sumer.sumerstores.entities;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpException;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RequestLogger implements HttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLogger.class);

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        log.info("Request URI: {}", request.getRequestLine().getUri());
        log.info("Request Method: {}", request.getRequestLine().getMethod());
        log.info("Request Headers: {}", request.getAllHeaders());
    }
}

