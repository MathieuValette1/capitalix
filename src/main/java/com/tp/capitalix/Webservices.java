package com.tp.capitalix;



import generated.PallierType;
import generated.ProductType;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
/// http://localhost:8080/adventureisis/generic/world
@Path("generic")
public class Webservices {
    Services services;

    public Webservices() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorld(@Context HttpServletRequest request) {
        String username = request.getHeader("X-user");
        return Response.ok(services.getWorld(username)).build();
    }

    @PUT
    @Path("/product")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateProduct(String username, ProductType product){
        services.updateProduct(username, product);
        return Response.ok(services.getWorld(username)).build();
    }

    @PUT
    @Path("/manager")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateProduct(String username, PallierType manager){
        services.updateManager(username, manager);
        return Response.ok(services.getWorld(username)).build();
    }

    @PUT
    @Path("/upgrade")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateUpgrade(@Context HttpServletRequest request){
        String username = request.getHeader("X-user");
        String pallierName = request.getHeader("X-pallierName");
        services.updateUpgrade(username, pallierName);

        return Response.ok(services.getWorld(username)).build();
    }


}

