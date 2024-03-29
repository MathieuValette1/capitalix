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
        System.out.println("GET WORLD");
        World world = this.readWorldFromXml(username);
        this.update_player_score(world);
        System.out.println("[get world] ARGENT: " + world.getMoney());
        System.out.println("[get world] SCORE: " + world.getScore());
        this.saveWorldToXml(world, username);
        return world;
    }


// prend en paramètre le pseudo du joueur et le produit
// sur lequel une action a eu lieu (lancement manuel de production ou
// achat d’une certaine quantité de produit)
// renvoie false si l’action n’a pas pu être traitée
    public Boolean updateProduct(String username, ProductType newproduct) {
        System.out.println("UPDATEPRODUCT");
        System.out.println("[updateProduct] Le joueur met à jour des " + newproduct.getName());
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
            System.out.println("ARGENT1: " + world.getMoney());
            Double qtPrice = this.getCostOfNProducts(product, qtchange);
            double new_player_money = player_money - qtPrice;
            world.setMoney(new_player_money);
            System.out.println("ARGENT2: " + world.getMoney());

            ///Calculer le nouveau coût du produit
            product.setCout(newproduct.getCout());
            System.out.println("[updateProduct] Nouveau cout de " + product.getName() + ": " + product.getCout());
            /// Calculer la nouvelle quantité
            product.setQuantite(newproduct.getQuantite());
            System.out.println("[updateProduct] Nouvelle quantite de " + product.getName() + ": " + product.getQuantite());

            PalliersType palliersType = product.getPalliers();
            List<PallierType> liste_palliers = palliersType.getPallier();
            for (PallierType pallier: liste_palliers){
                if (product.getQuantite() >= pallier.getSeuil() && !pallier.isUnlocked()){
                    /// On débloque le pallier
                    pallier.setUnlocked(true);
                    if (pallier.getTyperatio() == TyperatioType.VITESSE){
                        product.setVitesse((int) (product.getVitesse() / pallier.getRatio()));
                        product.setTimeleft((long) (product.getTimeleft() / pallier.getRatio()));
                    }
                    else if (pallier.getTyperatio() == TyperatioType.GAIN){
                        product.setRevenu(product.getRevenu() * pallier.getRatio());
                    }
                }
            }

        } else {
            // initialiser product.timeleft à product.vitesse
            // pour lancer la production
            System.out.println("[updateProduct] lancement de production de " + product.getName());
            product.setTimeleft(product.getVitesse());
        }
        // sauvegarder les changements du monde
        world.setLastupdate(System.currentTimeMillis());
        this.saveWorldToXml(world, username);
        return true;
    }

    double getCostOfNProducts(ProductType product, int qte){
        double costProduct = product.getCout();
        double cost = (costProduct * (1-Math.pow(product.getCroissance(), qte))) / (1-product.getCroissance());
        System.out.println("L'achat a couté " + cost);
        return cost;
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

        System.out.println("[UPDATEMANAGER] ARGENT2: " + world.getMoney());
        System.out.println("UPDATEMANAGER");
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé
        // en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            System.out.println(" [UPDATEMANAGER] Manager null");
            return false;
        }

        // débloquer ce manager
        manager.setUnlocked(true);
        System.out.println(" [UPDATEMANAGER]"+manager.getName() + " débloqué");
        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            System.out.println(" [UPDATEMANAGER] Produit null");
            return false;
        }
        // débloquer le manager de ce produit
        product.setManagerUnlocked(true);
        System.out.println(" [UPDATEMANAGER] "+product.getName() + " automatisé");

        // soustraire de l'argent du joueur le cout du manager
        double player_money = world.getMoney();
        int manager_cost = manager.getSeuil();
        double new_player_money = player_money - manager_cost;
        world.setMoney(new_player_money);
        System.out.println("[UPDATEMANAGER] ARGENT2: " + world.getMoney());

        // sauvegarder les changements au monde
        world.setLastupdate(System.currentTimeMillis());
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
        System.out.println("UPDATE SCORE");
        long time_since_last_update = System.currentTimeMillis() - world.getLastupdate()  ;
        double score_to_add = 0;

        ProductsType productsType = world.getProducts();
        List<ProductType> list_of_product = productsType.getProduct();

        for (ProductType product: list_of_product){
            if (product.isManagerUnlocked()){
//                System.out.println("[update_player_score] "+ product.getName() + " a un manager");
                /// Le produit a un manager
                long nb_creation = time_since_last_update /  product.getVitesse();
                if (nb_creation > 0){
                    /// Des produits ont été créés
//                    System.out.println("[update_player_score] "+ nb_creation +" " + product.getName() + " on été créés");
                    double bonusAngels = (1+ world.getActiveangels()*world.getAngelbonus()/100);
                    score_to_add += (nb_creation + product.getQuantite()) * product.getRevenu() * bonusAngels;
//                    System.out.println("Cela a généré " + score_to_add + " revenus");
                }
                else{
                    /// Le produit n'a pas pu être créé
                    product.setTimeleft(product.getTimeleft() - time_since_last_update);
                }
            }
            else{
                /// Le produit n'a pas de manager
//                System.out.println("[update_player_score] "+ product.getName() + " n'a pas de manager");

                if ((product.getTimeleft() != 0) && (product.getTimeleft()<=time_since_last_update)){
                    /// Un produit a été créé
                    System.out.println("[update_player_score] "+ product.getName() + " a été créé");
                    System.out.println("Cela a rapporté: " + product.getRevenu());
                    double bonusAngels = (1+ world.getActiveangels()*world.getAngelbonus()/100);
                    score_to_add += product.getRevenu() * product.getQuantite() * bonusAngels;
                    product.setTimeleft(0);
                }
                else{
                    /// On met à jour le temps écoulé
//                    System.out.println("[update_player_score] "+ product.getName() + " n'a pas été créé");
                    if (product.getTimeleft() > 0){
                        System.out.println("[update_player_score] "+ product.getName() + " est en production ");
                        /// Le produit est en production, on met à jour son timeleft
                        product.setTimeleft(product.getTimeleft() - time_since_last_update);
                    }
                }
            }

        }
        world.setLastupdate(System.currentTimeMillis());
        world.setScore(world.getScore() + score_to_add);
        world.setMoney(world.getMoney() + score_to_add);
    }

    PallierType findUpgradeByName(World world, String upgradeName){
        PalliersType palliersType = world.getUpgrades();
        List<PallierType> listOfUpgrades = palliersType.getPallier();
        PallierType upgrade_to_return;

        for(PallierType upgrade: listOfUpgrades){
            if (Objects.equals(upgrade.getName(), upgradeName)){
                upgrade_to_return = upgrade;
                return upgrade_to_return;
            }
        }
        return null;
    }

    public boolean updateUpgrade(String username, PallierType newUpgrade){
        System.out.println("UPDATEUPGRADE");
        System.out.println(newUpgrade.isUnlocked());
        /// Achat d'un upgrade
        /// Il débloque l'upgrade si le joueur a assez d'argent
        /// Il faut déduire le prix de l'upgrade au score du joueur
        World world = getWorld(username);
        //////System.out.println(upgrade.getName() + " débloqué");
        // Le joueur a assez d'argent

        PallierType upgrade = findUpgradeByName(world, newUpgrade.getName());

        if(newUpgrade.getIdcible() == -1){
            System.out.println("UPGRADE productivité ANGE");
        }
        else if (newUpgrade.getIdcible() == 0){
            // L'upgrade s'applique à tous les produits
            for (ProductType productType: world.getProducts().getProduct()){
                if (upgrade.getTyperatio() == TyperatioType.VITESSE){
                    productType.setVitesse((int) (productType.getVitesse() / upgrade.getRatio()));
                    productType.setTimeleft((long) (productType.getTimeleft() / upgrade.getRatio()));
                }
                else if (upgrade.getTyperatio() == TyperatioType.GAIN){
                    System.out.println("Revenu: " + productType.getRevenu());
                    productType.setRevenu(productType.getRevenu() * upgrade.getRatio());
                    System.out.println("Nouveau revenu: " + productType.getRevenu());

                }
            }
        }
        else{
            ProductType product = findProductById(world, newUpgrade.getIdcible());
            if (product == null){return false;}
            if (upgrade.getTyperatio() == TyperatioType.VITESSE){
                product.setVitesse((int) (product.getVitesse() / upgrade.getRatio()));
                product.setTimeleft((long) (product.getTimeleft() / upgrade.getRatio()));
            }
            else if (upgrade.getTyperatio() == TyperatioType.GAIN){
                System.out.println("Revenu: " + product.getRevenu());
                product.setRevenu(product.getRevenu() * upgrade.getRatio());
                System.out.println("Nouveau revenu: " + product.getRevenu());
            }
        }

        upgrade.setUnlocked(true);
        world.setMoney(world.getMoney()-upgrade.getSeuil());


        upgrade.setRatio(1);
        world.setLastupdate(System.currentTimeMillis());
        this.saveWorldToXml(world, username);
        System.out.println("State " + upgrade.isUnlocked());
        return true;

    }


    public void deleteWorld(String username) {
        System.out.println("DELETEWORLD");

        JAXBContext jaxbContext;
        try {
            System.out.println("DELETEWORLD");
            //Récupération des données du monde initial
            jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            World newWorld = (World) jaxbUnmarshaller.unmarshal(input);
            assert input != null;
            input.close();

            //Récupération des données du monde actuel
            File f = new File(path + "/" + username + "-world.xml");

            world = (World) jaxbUnmarshaller.unmarshal(f);

            //On garde les anges et le score
            newWorld.setScore(world.getScore());
            newWorld.setActiveangels(calculSuppAngels(world));
            newWorld.setTotalangels(calculSuppAngels(world));

            saveWorldToXml(newWorld, username);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double calculSuppAngels(World world) {
        return 150 * Math.sqrt((world.getScore() / Math.pow(10, 15))) - world.getTotalangels();
    }
}
