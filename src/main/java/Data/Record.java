package Data;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

public interface Record {

    public int getSize();
    public BitSet getHash() throws NoSuchAlgorithmException;
    public boolean equals(Record record);
    public byte[] toByteArray();
    public Record fromByteArray(byte[] bytes) throws ClassNotFoundException;
    public String toString();
    public String getDescription();
    public void setDescription(String desc);
    public GPS getGps1();
    public void setGps1(GPS gps1);
    public GPS getGps2();
    public void setGps2(GPS gps2);
    public int[] getList();
    public void setList(int[] list);
    public int getId();
    public void setId(int id);
}
