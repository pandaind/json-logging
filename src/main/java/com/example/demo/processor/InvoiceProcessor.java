package com.example.demo.processor;

import com.example.demo.model.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.Random;

import static net.logstash.logback.argument.StructuredArguments.keyValue;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class InvoiceProcessor implements Processor {
    @Override
    public void process(Exchange e) {
        var invoice = new Invoice(Math.abs(new Random().nextLong()), "num123");
        log.info("1. invoice generated {}", keyValue("coupaInvoiceId", invoice.getId()));
        log.info("2. invoice generated {} and {}", keyValue("coupaInvoiceId", invoice.getId()), keyValue("coupaInvoiceNumber", invoice.getNumber()));
        log.info("3. invoice generated", kv("coupaInvoiceId", invoice.getId()), kv("coupaInvoiceNumber", invoice.getNumber()));
        log.info("4. invoice generated", kv("coupaInvoice",invoice));

        e.getIn().setHeader("CoupaInvoiceId", invoice.getId());
        e.getIn().setBody(invoice);
    }
}
