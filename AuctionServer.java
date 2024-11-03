import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.security.NoSuchAlgorithmException;


public class AuctionServer implements Auction {
    AuctionItem[] items = new AuctionItem[10];

    public AuctionServer() throws NoSuchAlgorithmException, FileNotFoundException, IOException {

        if (!Files.exists(Paths.get("keys/testKey.aes"))) {

            KeyGenerator key_generator = KeyGenerator.getInstance("AES");
                

            SecretKey secret_key = key_generator.generateKey();
            byte[] scrambeld_key = secret_key.getEncoded();

            FileOutputStream stream = new FileOutputStream("keys/testKey.aes");
            stream.write(scrambeld_key);
            stream.close();

            System.out.println("Secret key generated");
        
        } else {
            System.out.println("Secret key already exists");
        }


        for (int x = 0; x < this.items.length; x++) {
            AuctionItem item = new AuctionItem();
            item.itemID = x;
            item.name = "thing";
            item.description = "item";
            item.highestBid = 0;
            items[x] = item;
        }

    }

    @Override
    public SealedObject getSpec(int itemID) {
        try {

            byte[] scrambeld_key = Files.readAllBytes(Paths.get("keys/testKey.aes"));
            SecretKeySpec spec = new SecretKeySpec(scrambeld_key, "AES");


            SecretKey secret_key = spec;
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secret_key);
    
    
            for (int x = 0; x < this.items.length; x++) {
            
                if (items[x].itemID == itemID) {
                    SealedObject sealed_obj = new SealedObject(items[x], cipher);
                    return sealed_obj;
                }
            }
        } catch(Exception e) {
            System.out.println(e);
        }

        return null;
    }

    public static void main(String[] args) throws RemoteException, NoSuchAlgorithmException, FileNotFoundException, IOException {
        AuctionServer auction = new AuctionServer();
        Auction stub = (Auction) UnicastRemoteObject.exportObject(auction, 0);

        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("Auction", stub);

        System.out.println("waiting....");
    }
}
