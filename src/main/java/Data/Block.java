package Data;

import java.io.*;
import java.util.ArrayList;

public class Block implements Serializable {

    private ArrayList<Record> listOfRecords;
    private int next;
    private int before;
    private int overflowAddress;
    private int validCount;
    private int blockFactor;
    private Record emptyRecord;


    public Block(int blockFactor, Class type) {
        this.listOfRecords = new ArrayList<>();
        this.validCount = 0;
        this.blockFactor = blockFactor;
        this.emptyRecord = createEmptyRecord(type);
        this.next = -1;
        this.before = -1;
        this.overflowAddress = -1;
    }

    public int getSize(){
        return 4*4 + blockFactor * emptyRecord.getSize();
    }

    public Record find(Record record) {
        for (Record r: this.listOfRecords) {
            if (r.equals(record)) {
                return r;
            }
        }
        return null;
    }

    public byte[] toByteArray(){
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
        try (DataOutputStream dataOS = new DataOutputStream(byteOS)) {
            dataOS.writeInt(this.validCount);
            dataOS.writeInt(this.next);
            dataOS.writeInt(this.before);
            dataOS.writeInt(this.overflowAddress);
            for (Record record: this.listOfRecords) {
                dataOS.write(record.toByteArray());
            }
            int emptyRecords = this.blockFactor - this.listOfRecords.size();
            for (int i = 0; i < emptyRecords; i++) {
                for (int j = 0; j < emptyRecord.getSize(); j++) {
                    dataOS.writeByte(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return byteOS.toByteArray();
    }

    public Block fromByteArray(byte[] bytes){
        try (DataInputStream dataIS = new DataInputStream(new ByteArrayInputStream(bytes))) {
            this.validCount = dataIS.readInt();
            this.next = dataIS.readInt();
            this.before = dataIS.readInt();
            this.overflowAddress = dataIS.readInt();
            for (int i = 0; i < this.validCount; i++) {
                byte[] recordBytes = new byte[emptyRecord.getSize()];
                System.arraycopy(bytes, 4*4 + emptyRecord.getSize()*i, recordBytes, 0, emptyRecord.getSize());
                this.emptyRecord = createEmptyRecord(emptyRecord.getClass());
                this.listOfRecords.add(emptyRecord.fromByteArray(recordBytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Record createEmptyRecord(Class type) {
        Record r;
        try {
            r = (Record) type.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return r;
    }
    public ArrayList<Record> getListOfRecords() {
        return listOfRecords;
    }

    public void setListOfRecords(ArrayList<Record> listOfRecords) {
        this.listOfRecords = listOfRecords;
    }

    public void addRecord(Record record) {
        this.validCount++;
        this.listOfRecords.add(record);
    }

    public void removeRecord(Record record) {
        this.listOfRecords.remove(this.find(record));
        this.validCount--;
    }

    public void removeAll() {
        this.listOfRecords.clear();
        this.validCount = 0;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Block { blockFactor: " + this.blockFactor + ", validCount: " + this.validCount
                + ", overflowAddress: " + this.overflowAddress + ", next: " + this.next + ", before: " + this.before + ", "
                + this.listOfRecords.size() + " Records: ");
        for (Record record: this.listOfRecords) {
            result.append(record.toString());
        }
        result.append("}");
        return result.toString();
    }

    public int getValidCount() {
        return validCount;
    }

    public void setValidCount(int validCount) {
        this.validCount = validCount;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public int getBefore() {
        return before;
    }

    public void setBefore(int before) {
        this.before = before;
    }

    public int getOverflowAddress() {
        return overflowAddress;
    }

    public void setOverflowAddress(int overflowAddress) {
        this.overflowAddress = overflowAddress;
    }
}
