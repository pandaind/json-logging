package com.example.demo.config.mdc;

import com.example.demo.config.DemoConfigProperties;
import com.example.demo.config.mdc.DemoMDCUnitOfWork;
import org.apache.camel.Exchange;
import org.apache.camel.impl.engine.DefaultUnitOfWork;
import org.apache.camel.spi.UnitOfWork;
import org.apache.camel.spi.UnitOfWorkFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DemoUnitOfWorkFactory implements UnitOfWorkFactory {

    @Autowired
    private DemoConfigProperties properties;

    public Optional<List<String>> headerNames() {
        return Optional.ofNullable(properties.getMdcHeaderNames());
    }

    @Override
    public UnitOfWork createUnitOfWork(Exchange exchange) {
        UnitOfWork unit;

        if (exchange.getContext().isUseMDCLogging()) {
            // allow customization of the logged headers
            if (headerNames().isPresent()) {
                unit = new DemoMDCUnitOfWork(exchange, headerNames().get());
            } else {
                // fallback to default headers
                unit = new DemoMDCUnitOfWork(exchange);
            }
        } else {
            unit = new DefaultUnitOfWork(exchange);
        }

        return unit;
    }
}
