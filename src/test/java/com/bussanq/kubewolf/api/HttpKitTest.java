package com.bussanq.kubewolf.api;

import com.bussanq.kubewolf.common.error.ApiException;
import com.bussanq.kubewolf.common.utils.HttpKit;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class HttpKitTest {
    @Test void rejectsTransportAndBusinessFailuresAndHandlesMissingResources() throws Exception {
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/business",exchange->{byte[] body="{\"success\":false}".getBytes(StandardCharsets.UTF_8);exchange.sendResponseHeaders(200,body.length);exchange.getResponseBody().write(body);exchange.close();});
        server.createContext("/missing",exchange->{exchange.sendResponseHeaders(404,-1);exchange.close();});
        server.createContext("/gone",exchange->{byte[] body="{\"success\":false,\"message\":\"record not found\"}".getBytes(StandardCharsets.UTF_8);exchange.sendResponseHeaders(200,body.length);exchange.getResponseBody().write(body);exchange.close();});
        server.createContext("/error",exchange->{exchange.sendResponseHeaders(503,-1);exchange.close();});
        server.start();
        var http=new HttpKit();String base="http://127.0.0.1:"+server.getAddress().getPort();
        try {
            assertThrows(ApiException.class,()->http.request("POST",base+"/business","token",null,false));
            assertThrows(ApiException.class,()->http.request("GET",base+"/error","token",null,false));
            assertNull(http.request("GET",base+"/missing","token",null,true));
            assertNull(http.request("GET",base+"/gone","token",null,true));
            assertThrows(ApiException.class,()->http.request("POST",base+"/gone","token",null,true));
            assertThrows(ApiException.class,()->http.request("GET",base+"/business","token",null,true));
        } finally {http.close();server.stop(0);}
    }
}
