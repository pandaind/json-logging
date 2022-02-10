package com.example.demo.config;

import com.fasterxml.jackson.core.JsonStreamContext;
import net.logstash.logback.encoder.org.apache.commons.lang3.RegExUtils;
import net.logstash.logback.mask.ValueMasker;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SensitiveDataMask implements ValueMasker {

    private final static String DEFAULT_MASK = "****";

    private final String[] regExArray = {
                                    "(password[-=: ])\\w+", // password :/=/ /-
                                    "([a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+[a-zA-Z])", // email
                                    "(\\+(?:[0-9] ?){6,14}[0-9])", //phone number
                                };

    private final List<String> patterns;

    public SensitiveDataMask(){
        this.patterns = getSensitivePatterns();
    }

    private List<String> getSensitivePatterns() {
        return Arrays.asList(regExArray);
    }

    @Override
    public Object mask(JsonStreamContext jsonStreamContext, Object obj) {
        if (obj instanceof CharSequence) {
            return removeSensitiveData((String) obj);
        }
        return obj;
    }

    private Object removeSensitiveData(String msg) {
        for(String p : patterns){
            if(Pattern.compile(p).matcher(msg).find()){
                msg =  RegExUtils.replaceAll(msg,p,DEFAULT_MASK);
            }
        }
        return msg;
    }
}
