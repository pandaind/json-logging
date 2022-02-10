package com.example.demo.config;

import com.example.demo.config.mdc.DemoUnitOfWorkFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelContextConfig implements CamelContextConfiguration {
    @Autowired
    private DemoUnitOfWorkFactory demoUnitOfWorkFactory;

    @Override
    public void beforeApplicationStart(CamelContext camelContext) {
        if (camelContext.isUseMDCLogging()) {
            ExtendedCamelContext extendedCamelContext = camelContext.adapt(ExtendedCamelContext.class);
            extendedCamelContext.setUnitOfWorkFactory(demoUnitOfWorkFactory);
        }
    }

    @Override
    public void afterApplicationStart(CamelContext camelContext) {
    }
}
