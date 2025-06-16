import Data.GPS;
import Data.Parcel;
import DH.DynamicHash;
import Data.Property;
import GUI.DynamicHashManager;
import GUI.MainWindow;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        DynamicHash<Parcel> dh = new DynamicHash<>(0,2,2, "dataParcel1.txt", "dataParcel2.txt");
        DynamicHash<Property> dh2 = new DynamicHash<>(0,2,2, "dataProperty1.txt", "dataProperty2.txt");

        DynamicHashManager manager = new DynamicHashManager(dh2, dh);

//        Parcel parcel = new Parcel();
//        Generator g = new Generator<>(dh, Parcel.class);
//        g.insertRecords(1000);
//        g.findObjects(1000);
//        g.delete(1000);
//        g.random(1000);
//        System.out.println(dh.mainFileRecords(parcel));
//        System.out.println(dh.overflowFileRecords(parcel));
    }
}
