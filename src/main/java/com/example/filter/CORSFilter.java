package com.example.filter;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;

@Provider
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        res.getHeaders().add("Access-Control-Allow-Origin", "*");
        res.getHeaders().add("Access-Control-Allow-Headers", 
                             "origin, content-type, accept, authorization");
        res.getHeaders().add("Access-Control-Allow-Methods", 
                             "GET, POST, PUT, DELETE, OPTIONS");
    }
}
