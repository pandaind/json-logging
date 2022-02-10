# Structured (JSON) logging



##  Why Structured (JSON) Logging?

The biggest benefit of logging in JSON is that it’s a structured data format. This makes it possible for you to analyze the logs of your application and troubleshoot issues faster. 

In ELK / EFK stack instead of parsing our logs to JSON format using regex or using grok filters. we can make our application to emit logs as JSON objects. where we can add additional JSON fields easily so that it can help us to query and analyze our logs effectively.  [ReadMoreHere](https://www.elastic.co/blog/structured-logging-filebeat)

```verilog
2020-08-10 23:31:25.443  INFO 20536 --- [ - timer://test] com.example.demo.DemoRoute               : 2. invoice generated coupaInvoiceId=262877147957987637 and coupaInvoiceNumber=num123
```

Structured Log :

```Json
{
  "@timestamp" : "2020-08-10T18:03:27.090Z",
  "@version" : "0.0.1-SNAPSHOT",
  "message" : "2. invoice generated coupaInvoiceId=9126432498243682631 and coupaInvoiceNumber=num123",
  "logger_name" : "com.example.demo.DemoRoute",
  "thread_name" : "Camel (camel-1) thread #1 - timer://test",
  "level" : "INFO",
  "camel.exchangeId" : "ID-C-LAP-1597082607075-0-1",
  "camel.contextId" : "camel-1",
  "camel.messageId" : "ID-C-LAP-1597082607075-0-1",
  "camel.routeId" : "demoRoute",
  "coupaInvoiceId" : 9126432498243682631,
  "coupaInvoiceNumber" : "num123",
  "application-name" : "demo-app"
}
```





## How can we achieve Structured Logging in Spring Boot Application?

There are few library dependencies available which can be used to achieve structured logging in our java application.

- By using [log4J2 or logback ](https://www.baeldung.com/java-log-json-output)
- By using [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder)

Using logstash-logback-encoder gives us the benefit of StructuredArguments and Markers to add additional fields to the JSON output. Read the logstash-logback-encoder document for more information.

:warning: **Note** : *StructuredArguments can be used in Java Classes like Camel Processor and Camel Bean but not in camel routes.*

```java
from("timer:test?period=5s").id("demoRoute")
               .log(LoggingLevel.INFO, "Hello, World"); // No use of StructuredArguments here.

// We will use MDC logging in camel routes to log Exchange Header as JSON fields.
```

Let's enable logstash-logback-encoder in our application :

**Step 1: Add dependencies to pom.xml**

```xml
		<dependency>
			<groupId>net.logstash.logback</groupId>
			<artifactId>logstash-logback-encoder</artifactId>
			<version>6.4</version>
		</dependency>
```

**Step 2: Create a logback-spring.xml in our resources folder. Example : [logback-spring.xml](src/main/resources/logback-spring.xml)**

Now our JSON Logging is enabled in our application.

**Step 3: Use StructuredArguments in our Camel Processor and Beans. Example : [InvoiceProcessor](src/main/java/com/example/demo/processor/InvoiceProcessor.java)**





## How to control normal logging and JSON logging using spring profile in Spring boot Application?

**Step 1: Create two profiles in spring-boot application application-standalone.yml and application-kubernetes.yml**

We will use standalone properties yml for our normal log configurations and kubernetes properties yml for JSON log configuration. So we will move all our JSON logging related configuration to application-kubernetes.yml

**Step 2: Create two springProfile in logback-spring.xml same like application yml.**

Standalone for normal logging

```xml
<!--Standalone Configuration-->
<springProfile name="standalone">
   <!-- Use the default logging config of Spring Boot -->
   <include resource="org/springframework/boot/logging/logback/base.xml"/>
</springProfile>
```

Kubernetes for JSON logging

```xml
<springProfile name="kubernetes">
    <!-- Add all our JSON Logging configuration here -->
</springProfile>
```

**Step 3: Activate profiles as per our requirement**

```yaml
spring:
  profiles:
    active: default, uat, kubernetes  # use kubernetes for JSON logging and standalone for normal logging
```





## How can we use MDC Logging in the camel route?

To enable MDC (mapped diagnostic context) Logging in camel application. 

**Step 1 : Enable MDC logging in our camel spring-boot application by configuring our application-kubernetes.yaml like bellow**

```yaml
# Enable Camel MDC logging in Application
camel:
  springboot:
    use-mdc-logging: true
```

After this we will see   "camel.exchangeId", "camel.contextId", "camel.routeId", "camel.breadcrumbId", "camel.messageId" in our JSON logs by default.

**Step 2: Add our custom header configuration list** 

```yaml
# Custom Header names. This is the custom property which we will use in our configuration
application:
  log:
    MDC-header-names:
      - CoupaInvoiceId
      - CoupaInvoiceNumber
```

 Read the properties 

```java
@Data
@Configuration
@ConfigurationProperties(prefix = "application.log")
public class DemoConfigProperties {
    private List<String> mdcHeaderNames;
}
```

**Step 3 : Create   [CustomUnitOfWorkFactory](src/main/java/com/example/demo/config/mdc/DemoUnitOfWorkFactory.java) and [CustomMDCUnitOfWork](src/main/java/com/example/demo/config/mdc/DemoMDCUnitOfWork.java)**

**Step 4: Configure UnitOfWorkFactory to camel context before application start**

```java
@Configuration
public class CamelContextConfig implements CamelContextConfiguration {
    @Autowired
    private DemoUnitOfWorkFactory demoUnitOfWorkFactory;

    @Override
    public void beforeApplicationStart(CamelContext camelContext) {
        // if camel.springboot.use-mdc-logging is true else dont configure
        if (camelContext.isUseMDCLogging()) {
            ExtendedCamelContext extendedCamelContext = camelContext.adapt(ExtendedCamelContext.class);
            extendedCamelContext.setUnitOfWorkFactory(demoUnitOfWorkFactory);
        }
    }

    @Override
    public void afterApplicationStart(CamelContext camelContext) {
    }
}
```

Now all the custom headers which are configured in our properties file will be added to the JSON log as an additional field.

:warning: **Note** : *we are reading the header values as string in line number 49 of [CustomMDCUnitOfWork](src/main/java/com/example/demo/config/mdc/DemoMDCUnitOfWork.java). Make sure we set string values in camel exchange header.*
