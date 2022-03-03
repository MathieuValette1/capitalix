package com.tp.capitalix;

import generated.ProductType;
import generated.ProductsType;
import generated.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Services {

    World world = new World();
    String path = "src/main/resources";

    World readWorldFromXml(String username){
        JAXBContext jaxbContext;
        try{
            try {
                jaxbContext = JAXBContext.newInstance(World.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                File f = new File(path+"/" + username +"-world.xml");
                world = (World) jaxbUnmarshaller.unmarshal(f);
                return world;
            } catch (Exception ex) {
                jaxbContext = JAXBContext.newInstance(World.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                // File f = new File(path+"/world.xml");
                InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
                world = (World) jaxbUnmarshaller.unmarshal(input);
                return world;
            }
        }catch (Exception ex){
            System.out.println("Erreur lecture du fichier:"+ex.getMessage());
            ex.printStackTrace();
        }

        return world;
    }

    void saveWorldToXml(World world, String username){

        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(World.class);
            Marshaller march = jaxbContext.createMarshaller();
            File file = new File(path+"/"+username+"-world.xml");
            OutputStream output = new FileOutputStream(file);
            march.marshal(world, output);
        } catch (Exception ex) {
            System.out.println("Erreur écriture du fichier:"+ex.getMessage());
            ex.printStackTrace();
        }

    }

    World getWorld(String username){
        World world = this.readWorldFromXml(username);
        this.saveWorldToXml(world, username);
        return world;
    }


// prend en paramètre le pseudo du joueur et le produit
// sur lequel une action a eu lieu (lancement manuel de production ou
// achat d’une certaine quantité de produit)
// renvoie false si l’action n’a pas pu être traitée
    public Boolean updateProduct(String username, ProductType newproduct) {
        // aller chercher le monde qui correspond au joueur
        World world = this.getWorld(username);
        // trouver dans ce monde, le produit équivalent à celui passé
        // en paramètre
        ProductType product = this.findProductById(world, newproduct.getId());
        if (product == null) { return false;}

        // calculer la variation de quantité. Si elle est positive c'est
        // que le joueur a acheté une certaine quantité de ce produit
        // sinon c’est qu’il s’agit d’un lancement de production.
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            // soustraire de l'argent du joueur le cout de la quantité
            // achetée et mettre à jour la quantité de product
            Double player_money = world.getMoney();
            double product_price = newproduct.getCout();
            Double qtPrice = qtchange * product_price;
            double new_player_money = player_money - qtPrice;
            world.setMoney(new_player_money);

            product.setQuantite(product.getQuantite()+newproduct.getQuantite());

        } else {
            // initialiser product.timeleft à product.vitesse
            // pour lancer la production
            product.setTimeleft(product.getVitesse());
        }
        // sauvegarder les changements du monde
        this.saveWorldToXml(world, username);
        return true;
    }

    ProductType findProductById(World world, Integer id){
        ProductsType productsType = world.getProducts();
        List<ProductType> list_of_product = productsType.getProduct();
        ProductType product_to_return = new ProductType();
        for (ProductType product: list_of_product){
            if (product.getId() == id){
                product_to_return = product;
                return product_to_return;
            }
        }
        return null;
    }
}
