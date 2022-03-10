package com.tp.capitalix;

import generated.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

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
                assert input != null;
                input.close();
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
            output.close();
        } catch (Exception ex) {
            System.out.println("Erreur écriture du fichier:"+ex.getMessage());
            ex.printStackTrace();
        }

    }

    World getWorld(String username){
        World world = this.readWorldFromXml(username);
        this.update_player_score(world);
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
            Double qtPrice = this.getCostOfNProducts(product, qtchange);
            double new_player_money = player_money - qtPrice;
            world.setMoney(new_player_money);

            product.setCout(product.getCout()*Math.pow(product.getCroissance(), qtchange));
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

    double getCostOfNProducts(ProductType product, int qte){
        double costProduct = product.getCout();
        return (costProduct * (1-Math.pow(product.getCroissance(), qte))) / (1-product.getCroissance());
    }

    ProductType findProductById(World world, Integer id){
        ProductsType productsType = world.getProducts();
        List<ProductType> list_of_product = productsType.getProduct();
        ProductType product_to_return;
        for (ProductType product: list_of_product){
            if (product.getId() == id){
                product_to_return = product;
                return product_to_return;
            }
        }
        return null;
    }

    // prend en paramètre le pseudo du joueur et le manager acheté.
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateManager(String username, PallierType newmanager) {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé
        // en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            return false;
        }

        // débloquer ce manager
        manager.setUnlocked(true);
        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // débloquer le manager de ce produit
        product.setManagerUnlocked(true);

        // soustraire de l'argent du joueur le cout du manager
        double player_money = world.getMoney();
        int manager_cost = manager.getSeuil();
        double new_player_money = player_money - manager_cost;
        world.setMoney(new_player_money);

        // sauvegarder les changements au monde
        this.saveWorldToXml(world, username);
        return true;
    }

    PallierType findManagerByName(World world, String managerName){
        PalliersType palliersType = world.getManagers();
        List<PallierType> listOfManager = palliersType.getPallier();
        PallierType manager_to_return;

        for(PallierType manager: listOfManager){
            if (Objects.equals(manager.getName(), managerName)){
                manager_to_return = manager;
                return manager_to_return;
            }
        }
        return null;
    }

    void update_player_score(World world){

        long time_since_last_update = System.currentTimeMillis() - world.getLastupdate()  ;
        world.setLastupdate(System.currentTimeMillis());
        double score_to_add = 0;

        ProductsType productsType = world.getProducts();
        List<ProductType> list_of_product = productsType.getProduct();

        for (ProductType product: list_of_product){
            if (product.isManagerUnlocked()){
                /// Le produit a un manager
                long nb_creation = time_since_last_update /  product.getVitesse();
                if (nb_creation > 0){
                    /// Des produits ont été créés
                    score_to_add += nb_creation * product.getRevenu();
                }
                else{
                    /// Le produit n'a pas pu être créé
                    product.setTimeleft(product.getTimeleft() - time_since_last_update);
                }
            }
            else{
                /// Le produit n'a pas de manager
                if (product.getTimeleft() != 0 & product.getTimeleft()<=time_since_last_update){
                    /// Un produit a été créé
                    score_to_add += product.getRevenu();
                }
                else{
                    /// On met à jour le temps écoulé
                    product.setTimeleft(product.getTimeleft() - time_since_last_update);
                }
            }

            world.setScore(world.getScore() + score_to_add);
        }

    }



    public boolean updateUpgrade(String username, String pallierName){
        /// Achat d'un upgrade
        /// Il débloque l'upgrade si le joueur a assez d'argent
        /// Il faut déduire le prix de l'upgrade au score du joueur
        World world = getWorld(username);
        PallierType upgrade = this.findUpgradeByName(pallierName);
        if (world.getMoney() >= upgrade.getSeuil()){
            // Le joueur a assez d'argent
            upgrade.setUnlocked(true);
            world.setMoney(world.getMoney()-upgrade.getSeuil());
            return true;
        }
        return false;
    }

    PallierType findUpgradeByName(String upgradeName){
        PalliersType palliersType = world.getUpgrades();
        List<PallierType> listOfUpgrade = palliersType.getPallier();
        PallierType upgrade_to_return;

        for(PallierType manager: listOfUpgrade){
            if (Objects.equals(manager.getName(), upgradeName)){
                upgrade_to_return = manager;
                return upgrade_to_return;
            }
        }
        return null;
    }


}
