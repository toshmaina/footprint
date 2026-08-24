//package com.skyworld.util.http;
//
//import io.undertow.server.HttpServerExchange;
//import io.undertow.util.StatusCodes;
//import ke.co.skyworld.internship.skycore.domain.beans.ExceptionRepresentation;
//
///**
// * sky-core (ke.co.skyworld.internship.skycore.util.http)
// * Created by: oloo
// * On: 8/12/26. 5:34 PM
// * Description:
// **/
//
//public class FallBack extends SkyCoreHttpHandler {
//    @Override
//    public void handleRequest(HttpServerExchange exchange) {
//
//        super.handleRequest(exchange);
//
//        send(exchange, new ExceptionRepresentation(
//                "URI Not Found",
//                exchange.getRequestURI(),
//                "URI " + exchange.getRequestURI() + " not found on server",
//                StatusCodes.NOT_FOUND,
//                exchange.getRequestMethod()
//        ), StatusCodes.NOT_FOUND);
//    }
//}
