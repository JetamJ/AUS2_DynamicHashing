import Data.GPS;
import Data.Parcel;
import DH.DynamicHash;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DynamicHashTests {
    DynamicHash<Parcel> dh = new DynamicHash<>(32, 2,2, "data.txt", "data2.txt");
    Generator<Parcel> g = new Generator<>(dh, Parcel.class);
    int numberOfIterations = 100;
    Parcel parcel1 = new Parcel(1,"test", new GPS('x', 10, 'y', 10),  new GPS('x', 20, 'y', 20));
    ArrayList<Parcel> insertedParcels = new ArrayList<>();
    Random random = new Random();

    @Before
    public  void insertData() {
        Parcel parcel;
        GPS gps1;
        GPS gps2;
        double x;
        double y;
        for (int i = 1; i < numberOfIterations + 1; i++) {
            x = random.nextDouble() * (250);
            y = random.nextDouble() * (250);
            gps1 = new GPS('X', x,'Y', y);
            x = x + random.nextDouble() * (50);
            y = y + random.nextDouble() * (50);
            gps2 = new GPS('X', x,'Y', y);
            parcel =  new Parcel(i, "Parcela: " + i, gps1, gps2);
            insertedParcels.add(parcel);
        }
    }

    @Test
    public void test1NoDataTest() throws NoSuchAlgorithmException, IOException {
        assertNull(dh.find(parcel1));
        assertFalse(dh.delete(parcel1));
    }

    @Test
    public void test2InsertTest() throws IOException, NoSuchAlgorithmException {
        for (Parcel parcel : insertedParcels) {
            dh.insert(parcel);
        }
        assertEquals(dh.getNumberOfRecordsMainFile(parcel1), numberOfIterations);
    }

    @Test
    public void test3FindTest() throws NoSuchAlgorithmException, IOException {
        for (int i = 1; i < numberOfIterations + 1; i++) {
            int randomId = random.nextInt(this.insertedParcels.size() - 1);
            Parcel randomRecord = this.insertedParcels.get(randomId + 1);
            Parcel record = dh.find(randomRecord);
            assertNotNull(record);
        }
        assertEquals(dh.getNumberOfRecordsMainFile(parcel1), numberOfIterations);
    }

    @Test
    public void test4DeleteTest() throws IOException, NoSuchAlgorithmException {
        int randomId;
        Parcel record;
        for (int i = 1; i < numberOfIterations + 1; i++) {
            if (this.insertedParcels.size() != 1) {
                randomId = random.nextInt(this.insertedParcels.size() - 1);
                record = this.insertedParcels.get(randomId + 1);
            } else {
                record = this.insertedParcels.get(0);
            }
            boolean deleted = dh.delete(record);
            assertTrue(deleted);
            if (deleted != false) {
                this.insertedParcels.remove(record);
            }
        }
        assertEquals(dh.getNumberOfRecordsMainFile(parcel1), 0);
    }
}
