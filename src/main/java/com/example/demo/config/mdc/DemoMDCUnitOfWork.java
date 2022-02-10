package com.example.demo.config.mdc;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.TypeConversionException;
import org.apache.camel.impl.engine.MDCUnitOfWork;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
public class DemoMDCUnitOfWork extends MDCUnitOfWork {

    /**
     * List of header names that will be added to MDC logging if found. Also the list used to clean up
     * once the Unit of Work is done, so nothing's left behind
     */
    private final List<String> headerNames;

    public DemoMDCUnitOfWork(Exchange exchange, final List<String> headerNames) {
        super(exchange, exchange.getContext().getInflightRepository(),
                exchange.getContext().getMDCLoggingKeysPattern(), exchange.getContext().isAllowUseOriginalMessage(), exchange.getContext().isUseBreadcrumb());
        this.headerNames = headerNames;
    }

    public DemoMDCUnitOfWork(Exchange exchange) {
        super(exchange, exchange.getContext().getInflightRepository(),
                exchange.getContext().getMDCLoggingKeysPattern(), exchange.getContext().isAllowUseOriginalMessage(), exchange.getContext().isUseBreadcrumb());
        this.headerNames = null;
    }

    private Optional<List<String>> headerNames() {
        return Optional.ofNullable(headerNames);
    }

    @Override
    public DemoMDCUnitOfWork newInstance(Exchange exchange) {
        return new DemoMDCUnitOfWork(exchange);
    }

    private void addMyHeaders(Exchange exchange, String headerName) {

        String headerValue;
        try {
            headerValue = exchange.getIn().getHeader(headerName, String.class);
            if (!StringUtils.isEmpty(headerValue)) {
                MDC.put(headerName, headerValue);
                log.trace("Adding header ({}={}) to MDC logging", headerName, headerValue);
            }
        } catch (TypeConversionException e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * beforeProcess is called before every step in a route. Add values to MDC here because many of
     * our headers will be set in route beans along the way and not be present when the exchange was
     * created
     */
    @Override
    public AsyncCallback beforeProcess(Processor processor, Exchange exchange,
                                       AsyncCallback callback) {

        // Add values from our headers
        if(headerNames().isPresent()){
            for (String headerName : headerNames().get()) {
                addMyHeaders(exchange, headerName);
            }
        }


        return super.beforeProcess(processor, exchange, callback);
    }

    /**
     * also remove our custom headers
     */
    @Override
    public void clear() {
        super.clear();
        if(headerNames().isPresent()){
            for (String headerName : headerNames().get()) {
                MDC.remove(headerName);
            }
        }
    }
}
