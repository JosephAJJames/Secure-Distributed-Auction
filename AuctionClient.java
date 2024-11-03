import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;



public class AuctionClient {

    public static void main(String[] args) throws RemoteException, NotBoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, ClassNotFoundException, BadPaddingException {
        Scanner int_scanner = new Scanner(System.in);
        System.out.println("Enter Item ID: ");
        int n = int_scanner.nextInt();
        int_scanner.close();

        
        SecretKey spec = null;
        try {
            byte[] key_data = Files.readAllBytes(Paths.get("keys/testKey.aes"));
            spec = new SecretKeySpec(key_data, "AES");
        } catch (Exception e) {
            System.err.println("Error loading the key.");
            e.printStackTrace();
            return;
        }

        
        Registry rmi_register = LocateRegistry.getRegistry("localhost");

        Auction auction = (Auction) rmi_register.lookup("Auction");
        SealedObject sealed_item = auction.getSpec(n);

        if (sealed_item != null) {
                
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, spec);

               
            AuctionItem item = (AuctionItem) sealed_item.getObject(cipher);

                
            System.out.println("ItemID: " + item.itemID);
            System.out.println("Item name: " + item.name + " Item description: " + item.description);
            System.out.println("Item highest bid: " + item.highestBid);
            System.out.println("");
        } else {
            System.out.println("No item found with ID: " + n);
        }
    }
}
