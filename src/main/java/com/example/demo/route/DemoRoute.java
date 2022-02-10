package com.example.demo.route;

import static net.logstash.logback.argument.StructuredArguments.*;

import com.example.demo.model.Invoice;
import com.example.demo.processor.InvoiceProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class DemoRoute extends RouteBuilder {

    @Autowired
    private InvoiceProcessor invoiceProcessor;

    @Override
    public void configure() {
        from("timer:test?period=5s").id("demoRoute")
                .process(invoiceProcessor)
               .log(LoggingLevel.INFO, "Hello ${body}");
    }
}
