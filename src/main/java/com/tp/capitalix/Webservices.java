package com.tp.capitalix;



import generated.PallierType;
import generated.ProductType;
import generated.World;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
/// http://localhost:8080/adventureisis/generic/world
@RestController
@RequestMapping("adventureisis/generic")
@CrossOrigin
public class Webservices {
    Services services;

    public Webservices() {
        services = new Services();
    }

    @GetMapping(value="world", produces={"application/xml","application/json"})
    public ResponseEntity<World> getWorld(@RequestHeader(value = "X-User",
            required = false) String username) {
        World world = services.getWorld(username);
        return ResponseEntity.ok(world);
    }


    @PutMapping(value = "/product", consumes ={"application/xml","application/json"})
    public ProductType putProduct(@RequestHeader(value = "X-User", required = false) String username,
                                  @RequestBody ProductType product) {
        System.out.println("PUT product");
        Boolean isProductUpdated = services.updateProduct(username,product);
        if (isProductUpdated){
            return product;
        }
        else{
            return null;
        }
    }
//    @PUT
//    @Path("/manager")
//    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
//    public Response updateProduct(String username, PallierType manager){
//        services.updateManager(username, manager);
//        return Response.ok(services.getWorld(username)).build();
//    }

//    @PUT
//    @Path("/upgrade")
//    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
//    public Response updateUpgrade(@Context HttpServletRequest request){
//        String username = request.getHeader("X-user");
//        String pallierName = request.getHeader("X-pallierName");
//        services.updateUpgrade(username, pallierName);
//
//        return Response.ok(services.getWorld(username)).build();
//    }


}

