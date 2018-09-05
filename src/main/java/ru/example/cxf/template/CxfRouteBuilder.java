package ru.example.cxf.template;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.component.cxf.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CxfRouteBuilder extends RouteBuilder {

    @Autowired
    CamelContext context;

    @Override
    public void configure() throws Exception {
        String WSDL_URI = "wsdl/hello.wsdl";
        
        CamelContext camelContext = getContext();
        camelContext.setStreamCaching(Boolean.TRUE);
        camelContext.disableJMX();
        //camelContext.setMessageHistory(Boolean.FALSE);

        CxfEndpoint cxfInputEndpoint = new CxfEndpoint();
        cxfInputEndpoint.setAddress("http://localhost:8888/services/hello");
        cxfInputEndpoint.setWsdlURL(WSDL_URI);
        cxfInputEndpoint.setService("{http://www.examples.com/wsdl/HelloService.wsdl}Hello_Service");
        cxfInputEndpoint.setEndpointNameString("{http://www.examples.com/wsdl/HelloService.wsdl}Hello_Port");
        cxfInputEndpoint.setMergeProtocolHeaders(true);
        cxfInputEndpoint.setSkipFaultLogging(true);
        cxfInputEndpoint.setDataFormat(DataFormat.PAYLOAD);
        //cxfInputEndpoint.setDataFormat(DataFormat.MESSAGE);
        cxfInputEndpoint.setLoggingFeatureEnabled(true);
        cxfInputEndpoint.setCamelContext(camelContext);
        context.addEndpoint("soap_input", cxfInputEndpoint);
        
        CxfEndpoint cxfOutputEndpoint = new CxfEndpoint();
        cxfOutputEndpoint.setAddress("http://desktop-bnspjc6:8088/services/helloBack");
        cxfOutputEndpoint.setMergeProtocolHeaders(true);
        cxfOutputEndpoint.setDataFormat(DataFormat.PAYLOAD);
        cxfOutputEndpoint.setSkipFaultLogging(true);
        cxfOutputEndpoint.setCamelContext(camelContext);
        context.addEndpoint("soap_output", cxfOutputEndpoint);
        
        from(cxfInputEndpoint)
            .onException(java.lang.Exception.class)
                .logExhausted(false)
                .to("log:message.fault?"
                        + "showCaughtException=true"
                        + "&showExchangeId=true"
                        + "&showHeaders=true"
                        + "&level=ERROR")
                .end()
            .removeHeaders("CamelHttp*|operationName|operationNamespace|accept-encoding")
            .to("log:message.in?"
                    + "showExchangeId=true"
                    + "&showHeaders=true")
            //.to("http4://desktop-bnspjc6:8088/services/helloBack?throwExceptionOnFailure=false&bridgeEndpoint=true")
            .to(cxfOutputEndpoint)
            .to("log:message.out?"
                    + "showExchangeId=true"
                    + "&showHeaders=true");

    }

}

