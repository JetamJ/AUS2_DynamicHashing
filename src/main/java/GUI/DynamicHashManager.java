package GUI;


import Data.GPS;
import Data.Property;
import Data.Parcel;
import DH.DynamicHash;
import QuadTree.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

public class DynamicHashManager {

    private DynamicHash<Property> dhProperty;
    private DynamicHash<Parcel> dhParcel;
    private QuadTree<QProperty> qtProperty;
    private QuadTree<QParcel> qtParcel;
    private int parcelId;
    private int propertyId;
    private Random random;

    public DynamicHashManager(DynamicHash<Property> dhProperty, DynamicHash<Parcel> dhParcel) {
        this.dhParcel = dhParcel;
        this.dhProperty = dhProperty;
        this.parcelId = 1;
        this.propertyId = 1;
        this.random = new Random();
        MainWindow mw = new MainWindow(this);
        mw.setVisible(true);
        this.qtProperty = new QuadTree<QProperty>(0,0,500,500,50);
        this.qtParcel = new QuadTree<QParcel>(0,0,500,500,50);
    }

    public String insertParcel(String desc, GPS gps1, GPS gps2) throws IOException, NoSuchAlgorithmException {
        Parcel parcel = new Parcel(this.parcelId, desc, gps1, gps2);
        QParcel qParcel = new QParcel(this.parcelId, gps1, gps2);

        ArrayList<QProperty> foundProperties = qtProperty.find(gps1.getX(), gps1.getY(), gps2.getX(), gps2.getY());
        if (foundProperties.size() >= 5) {
            return "Parcela presiahla pocet moznych nehnutelnost";
        }

        for (QProperty p : foundProperties) {
            ArrayList<QParcel> foundParcels = qtParcel.find(p.getGps1().getX(), p.getGps1().getY(), p.getGps2().getX(), p.getGps2().getY());
            if (foundParcels.size() >= 6) {
                return "Parcela sa nemoze pridat pretoze jej nehnutelnosti maju uz privela parciel";
            }
        }

        for (QProperty p : foundProperties) {
            parcel.addProperty(p.getId());
            Property property = this.findProperty(p.getId());
            property.addParcel(parcel.getId());
            this.editProperty(p.getId(), property);
        }

        this.dhParcel.insert(parcel);
        this.parcelId++;
        this.qtParcel.add(qParcel);

        StringBuilder stringBuilder = new StringBuilder();
        for (int id : parcel.getList()) {
            Property property = this.findProperty(id);
            if (property != null) {
                stringBuilder.append(property.toString() + "\n");
            }
        }

        return stringBuilder.toString();
    }

    public String insertProperty(int registerNumber, String desc, GPS gps1, GPS gps2) throws IOException, NoSuchAlgorithmException {
        Property property = new Property(this.propertyId, registerNumber, desc, gps1, gps2);
        QProperty qProperty = new QProperty(this.propertyId, gps1, gps2);

        ArrayList<QParcel> foundParcels = qtParcel.find(gps1.getX(), gps1.getY(), gps2.getX(), gps2.getY());
        if (foundParcels.size() >= 6) {
            return "Nehnutelnost presiahla pocet moznych parciel";
        }

        for (QParcel p : foundParcels) {
            ArrayList<QProperty> foundProperties = qtProperty.find(p.getGps1().getX(), p.getGps1().getY(), p.getGps2().getX(), p.getGps2().getY());
            if (foundProperties.size() >= 5) {
                return "Nehnutelnost sa nemoze pridat pretoze jej parceli maju uz privela nehnutelnosti";
            }
        }

        for (QParcel p : foundParcels) {
            property.addParcel(p.getId());
            Parcel parcel = this.findParcel(p.getId());
            parcel.addProperty(parcel.getId());
            this.editParcel(p.getId(), parcel);
        }

        this.dhProperty.insert(property);
        this.propertyId++;
        this.qtProperty.add(qProperty);

        StringBuilder stringBuilder = new StringBuilder();
        for (int id : property.getList()) {
            Parcel parcel = this.findParcel(id);
            if (parcel != null) {
                stringBuilder.append(parcel.toString() + "\n");
            }
        }

        return stringBuilder.toString();
    }

    public Parcel findParcel(int id) throws NoSuchAlgorithmException, IOException {
        Parcel parcel = new Parcel();
        parcel.setId(id);
        return this.dhParcel.find(parcel);
    }

    public Property findProperty(int id) throws NoSuchAlgorithmException, IOException {
        Property property = new Property();
        property.setId(id);
        return this.dhProperty.find(property);
    }

    public boolean deleteParcel(int id) throws NoSuchAlgorithmException, IOException {
        Parcel parcel = this.findParcel(id);
        if (parcel == null) {
            return false;
        }
        parcel = this.findParcel(id);
        QParcel qParcel = new QParcel(parcel.getId(), parcel.getGps1(), parcel.getGps2());
        this.dhParcel.delete(parcel);
        this.qtParcel.delete(qParcel);

        for (int i : parcel.getList()) {
            if (i != 0) {
                Property property = this.findProperty(i);
                property.removeParcel(id);
                this.editProperty(i, property);
            }
        }

        return true;
    }

    public boolean deleteProperty(int id) throws NoSuchAlgorithmException, IOException {
        Property property = this.findProperty(id);
        if (property == null) {
            return false;
        }

        property = this.findProperty(id);
        QProperty qProperty = new QProperty(property.getId(), property.getGps1(), property.getGps2());
        this.dhProperty.delete(property);
        this.qtProperty.delete(qProperty);

        for (int i : property.getList()) {
            if (i != 0) {
                Parcel parcel = this.findParcel(i);
                parcel.removeProperty(id);
                this.editParcel(i, parcel);
            }
        }

        return true;
    }

    public boolean editParcel(int id, Parcel newParcel) throws NoSuchAlgorithmException, IOException {
        Parcel parcel = this.findParcel(id);
        if (parcel.getGps1().getX() != newParcel.getGps1().getX() || parcel.getGps1().getY() != newParcel.getGps1().getY()
                || parcel.getGps2().getX() != newParcel.getGps2().getX() || parcel.getGps2().getY() != newParcel.getGps2().getY()) {
            this.deleteParcel(id);
            this.parcelId--;
            this.insertParcel(newParcel.getDescription(), newParcel.getGps1(), newParcel.getGps2());
            return true;
        } else {
            return this.dhParcel.edit(parcel, newParcel);
        }
    }

    public boolean editProperty(int id, Property newProperty) throws NoSuchAlgorithmException, IOException {
        Property property = this.findProperty(id);
        if (property.getGps1().getX() != newProperty.getGps1().getX() || property.getGps1().getY() != newProperty.getGps1().getY()
                || property.getGps2().getX() != newProperty.getGps2().getX() || property.getGps2().getY() != newProperty.getGps2().getY()) {
            this.deleteProperty(id);
            this.propertyId--;
            this.insertProperty(newProperty.getRegisterNumber(), newProperty.getDescription(), newProperty.getGps1(), newProperty.getGps2());
            return true;
        } else {
            return this.dhProperty.edit(property, newProperty);
        }
    }

    public String parcelOutput() throws IOException {
        Parcel parcel = new Parcel();
        String mainFileRecords = this.dhParcel.mainFileRecords(parcel);
        String overflowFileRecords = this.dhParcel.overflowFileRecords(parcel);
        return mainFileRecords + "\n" + overflowFileRecords;
    }

    public String propertyOutput() throws IOException {
        Property property = new Property();
        String mainFileRecords = this.dhProperty.mainFileRecords(property);
        String overflowFileRecords = this.dhProperty.overflowFileRecords(property);
        return mainFileRecords + "\n" + overflowFileRecords;
    }

    public void generateParcels(int numberOfRecords) throws IOException, NoSuchAlgorithmException {
        GPS gps1;
        GPS gps2;
        double x;
        double y;
        int iterations = parcelId + numberOfRecords;
        for (int i = parcelId; i < iterations; i++) {
            x = random.nextDouble() * (250);
            y = random.nextDouble() * (250);
            gps1 = new GPS('X', x,'Y', y);
            x = x + random.nextDouble() * (50);
            y = y + random.nextDouble() * (50);
            gps2 = new GPS('X', x,'Y', y);
            //Parcel parcel = new Parcel(i, "Parcela: " + i, gps1, gps2);
//            dhParcel.insert(parcel);
//            parcelId++;
            this.insertParcel("Parcela: " + i, gps1, gps2);
        }
    }

    public void generateProperty(int numberOfRecords) throws IOException, NoSuchAlgorithmException {
        GPS gps1;
        GPS gps2;
        double x;
        double y;
        int iterations = propertyId + numberOfRecords;
        for (int i = propertyId; i < iterations; i++) {
            x = random.nextDouble() * (250);
            y = random.nextDouble() * (250);
            gps1 = new GPS('X', x,'Y', y);
            x = x + random.nextDouble() * (50);
            y = y + random.nextDouble() * (50);
            gps2 = new GPS('X', x,'Y', y);
            //Property property = new Property(i,i, "Nehnutelnost: " + i, gps1, gps2);
//            dhProperty.insert(property);
//            propertyId++;
            this.insertProperty(i,"Nehnutelnost: " + i, gps1, gps2);
        }
    }

    public void saveDHParcel(String path) {
        this.dhParcel.save(path);
    }

    public void loadDHParcel(String path) {
        this.dhParcel.load(path);
    }

    public void saveDHProperty(String path) {
        this.dhProperty.save(path);
    }

    public void loadDHProperty(String path) {
        this.dhProperty.load(path);
    }

    public void saveQTParcel(String path) {
        this.qtParcel.save(path);
    }

    public void loadQTParcel(String path) {
        this.qtParcel.load(path);
    }

    public void saveQTProperty(String path) {
        this.qtProperty.save(path);
    }

    public void loadQTProperty(String path) {
        this.qtProperty.load(path);
    }
}
