package com.redhat;

//import com.redhat.dto.Customer;
//import com.redhat.dto.CustomerSuccess;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.netty.http.NettyHttpMessage;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;
import org.apache.camel.LoggingLevel;

import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import java.util.Map;

@Component
public class Routes extends RouteBuilder {

  String Url = "";
  String queryBase = "";
  //System.out.println("URL OAuth:"+ erpOAuth);

  @Override
  public void configure() throws Exception {

    Url = "https://apisgratis.com/api/codigospostales/v2/ciudades/?";
    queryBase = "";


    restConfiguration()
      .component("netty-http")
      .port("8080")
      .bindingMode(RestBindingMode.auto);

    rest()
      .path("/").consumes("application/json").produces("application/json")
        .put("/get-ciudades")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:put-customer")
        .post("/get-ciudades")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:post-customer")
        .get("/get-ciudades")
  //          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:get-customer");
    
    from("direct:post-customer")
      .setHeader("HTTP_METHOD", constant("POST"))
      .to("direct:request");
    from("direct:put-customer")
      .setHeader("HTTP_METHOD", constant("PUT"))
      .to("direct:request");
    from("direct:get-customer")
      .setHeader("HTTP_METHOD", constant("GET"))
      .to("direct:request");

    from("direct:request")
      .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
          Message inMessage = exchange.getIn();
          String query = inMessage.getHeader(Exchange.HTTP_QUERY, String.class);
          System.out.println("Query:"+query);
          if(query != null){
              Url = Url + "" + query + "&bridgeEndpoint=true&throwExceptionOnFailure=false";
              System.out.println("Query is not null:"+query);
          }else{
              Url = Url + "bridgeEndpoint=true&throwExceptionOnFailure=false";
          }
          exchange.getMessage().setHeader("Access-Control-Allow-Origin", "*");
          exchange.getMessage().setHeader("CamelHttpRawQuery", query);
          System.out.println("Query:"+query);
          exchange.getMessage().setHeader(Exchange.HTTP_URI, Url);
          System.out.println("URI:"+Url);
        }
      })
      .to("log:DEBUG?showBody=true&showHeaders=true")
      .to("https://ciudades")
      .streamCaching()
      .log(LoggingLevel.INFO, "${in.headers.CamelFileName}")
      .to("log:DEBUG?showBody=true&showHeaders=true")
      .removeHeaders("*");
      
//      .choice()
//        .when(simple("${header.CamelHttpResponseCode} != 201 && ${header.CamelHttpResponseCode} != 202"))
//          .log("err")
//          .transform(constant("Error"))
//        .otherwise()
//          .log("ok")
//      .endChoice();
  }
}