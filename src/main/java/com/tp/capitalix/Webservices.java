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
//        System.out.println("PUT product");
        Boolean isProductUpdated = services.updateProduct(username,product);
        if (isProductUpdated){
            return product;
        }
        else{
            return null;
        }
    }

    @PutMapping(value = "/manager", consumes ={"application/xml","application/json"})
    public PallierType putManager(@RequestHeader(value = "X-User", required = false) String username,
                                  @RequestBody PallierType manager) {
        System.out.println("PUT MANAGER");
        Boolean isManagerUpdated = services.updateManager(username,manager);
        if (isManagerUpdated){
            return manager;
        }
        else{
            return null;
        }
    }

    @PutMapping(value = "/upgrade", consumes ={"application/xml","application/json"})
    public PallierType putUpgrade(@RequestHeader(value = "X-User", required = false) String username,
                                  @RequestBody PallierType upgrade) {
        System.out.println("PUT UPGRADE");
        Boolean isUpgradeUpdated = services.updateUpgrade(username,upgrade);
        if (isUpgradeUpdated){
            return upgrade;
        }
        else{
            return null;
        }
    }




}

