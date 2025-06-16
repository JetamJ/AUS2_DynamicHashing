import Data.GPS;
import Data.Parcel;
import Data.Property;
import Data.Record;
import DH.DynamicHash;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

public class Generator<T extends Record> {

    private DynamicHash<T> dh;
    private Random random;
    private ArrayList<T> insertedObjects;
    private Class<T> type;

    public Generator(DynamicHash<T> dh, Class<T> type) {
        this.dh = dh;
        this.random = new Random(2);
        this.insertedObjects = new ArrayList<>();
    }

    public void insertRecords(int numberOfRecords) throws IOException, NoSuchAlgorithmException {
        Record object;
        GPS gps1;
        GPS gps2;
        double x;
        double y;
        this.insertedObjects.clear();
        for (int i = 1; i < numberOfRecords + 1; i++) {
            x = random.nextDouble() * (250);
            y = random.nextDouble() * (250);
            gps1 = new GPS('X', x,'Y', y);
            x = x + random.nextDouble() * (50);
            y = y + random.nextDouble() * (50);
            gps2 = new GPS('X', x,'Y', y);
            if (this.type == Property.class) {
                object = (T) new Property(i, i, "Nehnutelnost: " + i, gps1, gps2);
            } else {
                object = (T) new Parcel(i, "Parcela: " + i, gps1, gps2);
            }
            dh.insert((T) object);
            insertedObjects.add((T) object);
        }
    }

    public boolean findObjects(int numberOfIterations) throws NoSuchAlgorithmException, IOException {
        boolean result = true;
        for (int i = 1; i < numberOfIterations + 1; i++) {
            int randomId = random.nextInt(this.insertedObjects.size() - 1);
            T randomRecord = this.insertedObjects.get(randomId + 1);
            T record = dh.find(randomRecord);
            if (record == null) {
                System.out.println("Nenasiel sa record s id: " + randomRecord.toString());
                result = false;
            }
        }
        return result;
    }

    public boolean delete(int numberOfIterations) throws NoSuchAlgorithmException, IOException {
        boolean result = true;
        int randomId;
        T record;
        for (int i = 1; i < numberOfIterations + 1; i++) {
            if (this.insertedObjects.size() != 1) {
                randomId = random.nextInt(this.insertedObjects.size() - 1);
                record = this.insertedObjects.get(randomId + 1);
            } else {
                record = this.insertedObjects.get(0);
            }
            boolean deleted = dh.delete(record);
            if (deleted == false) {
                System.out.println("Nepodarilo sa vymazat record: - " + record.toString());
                result = false;
            } else {
                this.insertedObjects.remove(record);
            }
        }
        return result;
    }

    public void random(int numberOfIterations) throws IOException, NoSuchAlgorithmException {
        double x;
        double y;
        T object;
        this.insertedObjects.clear();
        for (int i = 1; i < numberOfIterations + 1; i++) {
            if (i == 89) {
                int test = 0;
            }
            int number = random.nextInt(3);
            switch (number) {
                case 0 :
                    x = random.nextDouble() * (250);
                    y = random.nextDouble() * (250);
                    GPS gps1 = new GPS('X', x,'Y', y);
                    x = x + random.nextDouble() * (50);
                    y = y + random.nextDouble() * (50);
                    GPS gps2 = new GPS('X', x,'Y', y);
                    if (this.type == Property.class) {
                        object = (T) new Property(i, i, "Nehnutelnost: " + i, gps1, gps2);
                    } else {
                        object = (T) new Parcel(i, "Parcela: " + i, gps1, gps2);
                    }
                    dh.insert(object);
                    insertedObjects.add(object);
//                    System.out.println(dh.mainFileRecords(object));
//                    System.out.println(dh.overflowFileRecords(object));
                    break;
                case 1 :
                    if (this.insertedObjects.isEmpty()) {
                        break;
                    }
                    int randomId;
                    T randomRecord;
                    if (this.insertedObjects.size() != 1) {
                        randomId = random.nextInt(this.insertedObjects.size() - 1);
                        randomRecord = this.insertedObjects.get(randomId + 1);
                    } else {
                        randomRecord = this.insertedObjects.get(0);
                    }

                    T record = dh.find(randomRecord);
                    if (record == null) {
                        System.out.println("Nenasiel sa record s id: " + randomRecord.toString());
                    }
                    break;
                case 2:
                    if (this.insertedObjects.isEmpty()) {
                        break;
                    }
                    int randomId2;
                    T record2;
                    if (this.insertedObjects.size() != 1) {
                        randomId2 = random.nextInt(this.insertedObjects.size() - 1);
                        record2 = this.insertedObjects.get(randomId2 + 1);
                    } else {
                        record2 = this.insertedObjects.get(0);
                    }
                    boolean deleted = dh.delete(record2);
                    if (deleted == false) {
                        System.out.println("Nepodarilo sa vymazat record: - " + record2.toString());
                    } else {
                        this.insertedObjects.remove(record2);
                    }
                    break;
            }
        }
    }
}
