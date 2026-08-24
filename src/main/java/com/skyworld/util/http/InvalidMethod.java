//package com.skyworld.util.http;
//
//import io.undertow.server.HttpServerExchange;
//import io.undertow.util.StatusCodes;
//
//
///**
// * sky-core (ke.co.skyworld.internship.skycore.util.http)
// * Created by: oloo
// * On: 8/12/26. 5:35 PM
// * Description:
// **/
//
//public class InvalidMethod extends SkyCoreHttpHandler {
//
//    @Override
//    public void handleRequest(HttpServerExchange exchange) {
//
//        super.handleRequest(exchange);
//
//        send(exchange, new ExceptionRepresentation(
//                "Method Not Allowed",
//                exchange.getRequestURI(),
//                "Method " + exchange.getRequestMethod() + " not allowed",
//                StatusCodes.METHOD_NOT_ALLOWED,
//                exchange.getRequestMethod()
//        ), StatusCodes.METHOD_NOT_ALLOWED);
//    }
//}
