package com.tp.capitalix;

import com.tp.capitalix.generated.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Services {

    World world = new World();
    String path = "src/main/resources";

    World readWorldFromXml(){
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            // File f = new File(path+"/world.xml");
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            world = (World) jaxbUnmarshaller.unmarshal(input);
            return world;
        } catch (Exception ex) {
            System.out.println("Erreur lecture du fichier:"+ex.getMessage());
            ex.printStackTrace();
        }
        return world;
    }

    void saveWordlToXml(World world){

        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(World.class);
            Marshaller march = jaxbContext.createMarshaller();
            File file = new File(path+"/contacts.xml");
            OutputStream output = new FileOutputStream(file);
            march.marshal(world, output);
        } catch (Exception ex) {
            System.out.println("Erreur écriture du fichier:"+ex.getMessage());
            ex.printStackTrace();
        }

    }

    World getWorld(){
        return this.readWorldFromXml();
    }
}
