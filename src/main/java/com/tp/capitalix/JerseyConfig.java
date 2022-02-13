package com.tp.capitalix;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/adventureisis")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {

        register(Webservices.class);
        register(CORSResponseFilter.class);
    }
}

