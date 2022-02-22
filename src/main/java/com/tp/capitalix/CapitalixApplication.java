package com.tp.capitalix;

import generated.World;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CapitalixApplication {

	public static void main(String[] args) {
		SpringApplication.run(CapitalixApplication.class, args);

//		Services services = new Services();
//		World world = services.readWorldFromXml("toto");
//		services.saveWorldToXml(world, "toto");
	}

}
