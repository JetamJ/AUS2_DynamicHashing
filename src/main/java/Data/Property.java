package Data;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.BitSet;

public class Property implements Record {

    private int id;
    private int registerNumber;
    private String desc;
    private int[] parcelsArray;
    private GPS gps1;
    private GPS gps2;
    private static final int MAX_DESC_LENGTH = 15;

    public Property(int id, int registerNumber, String desc, GPS gps1, GPS gps2){
        this.id = id;
        this.registerNumber = registerNumber;
        this.parcelsArray = new int[6];
        this.desc = checkString(desc);
        this.gps1 = gps1;
        this.gps2 = gps2;
    }

    public Property() {
        this.parcelsArray = new int[6];
    }
    @Override
    public int getSize() {
        return 8*4 + MAX_DESC_LENGTH + 20*2;
    }

    @Override
    public BitSet getHash() throws NoSuchAlgorithmException {
        int hashCode = Integer.valueOf(this.id).hashCode();
        BigInteger bigInteger = BigInteger.valueOf(hashCode);
        byte[] byteArray = bigInteger.toByteArray();
        BitSet bitSet = BitSet.valueOf(byteArray);
        return bitSet;
    }

    @Override
    public boolean equals(Record record) {
        return this.id == ((Property) record).getId();
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
        try (DataOutputStream dataOS = new DataOutputStream(byteOS)) {
            dataOS.writeInt(this.id);
            dataOS.writeInt(this.registerNumber);
            for (int i = 0; i < this.parcelsArray.length; i++) {
                dataOS.writeInt(this.parcelsArray[i]);
            }
            dataOS.write(checkString(this.desc).getBytes());
            dataOS.write(this.gps1.toByteArray());
            dataOS.write(this.gps2.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return byteOS.toByteArray();
    }

    @Override
    public Record fromByteArray(byte[] bytes) throws ClassNotFoundException {
        try (DataInputStream dataIS = new DataInputStream(new ByteArrayInputStream(bytes))) {
            this.id = dataIS.readInt();
            this.registerNumber = dataIS.readInt();
            for (int i = 0; i < this.parcelsArray.length; i++) {
                this.parcelsArray[i] = dataIS.readInt();
            }

            byte[] descBytes = new byte[MAX_DESC_LENGTH];
            System.arraycopy(bytes, 8*4, descBytes, 0, MAX_DESC_LENGTH);
            this.desc = new String(descBytes);

            GPS gps1 = new GPS();
            byte[] gps1Bytes = new byte[gps1.getSize()];
            System.arraycopy(bytes, 8*4 + MAX_DESC_LENGTH, gps1Bytes, 0, gps1Bytes.length);
            this.gps1 = gps1.fromByteArray(gps1Bytes);

            GPS gps2 = new GPS();
            byte[] gps2Bytes = new byte[gps1.getSize()];
            System.arraycopy(bytes, 8*4 + MAX_DESC_LENGTH + gps1.getSize(), gps2Bytes, 0, gps2Bytes.length);
            this.gps2 = gps2.fromByteArray(gps2Bytes);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return this;
    }

    @Override
    public String toString(){
        return "Property { id: " + this.id + ", registerNumber: " + this.registerNumber + ", description: " + this.desc +
                ", propertiesArray: " + Arrays.toString(this.parcelsArray) + ", " + this.gps1 + ", " + this.gps2 + " } ";
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(int registerNumber) {
        this.registerNumber = registerNumber;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public void setDescription(String desc) {
        this.desc = desc;
    }

    @Override
    public GPS getGps1() {
        return gps1;
    }

    @Override
    public void setGps1(GPS gps1) {
        this.gps1 = gps1;
    }

    @Override
    public GPS getGps2() {
        return gps2;
    }

    @Override
    public void setGps2(GPS gps2) {
        this.gps2 = gps2;
    }

    @Override
    public int[] getList() {
        return this.parcelsArray;
    }

    @Override
    public void setList(int[] list) {
        this.parcelsArray = list;
    }

    private String checkString(String s) {
        if (s == null) {
            return "***************";
        }
        if (s.length() < MAX_DESC_LENGTH) {
            int iterations = MAX_DESC_LENGTH - s.length();
            for (int i = 0; i < iterations; i++) {
                s += "*";
            }
            return s;
        }
        if (s.length() > MAX_DESC_LENGTH) {
            return s.substring(0,MAX_DESC_LENGTH);
        }
        return s;
    }

    public void addParcel(int id) {
        for (int i = 0; i < 6; i++) {
            if (this.parcelsArray[i] == 0) {
                this.parcelsArray[i] = id;
                return;
            }
        }
    }

    public void removeParcel(int id) {
        for (int i = 0; i < 6; i++) {
            if (this.parcelsArray[i] == id) {
                this.parcelsArray[i] = 0;
                return;
            }
        }
    }
}
