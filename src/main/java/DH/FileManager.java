package DH;

import Data.Block;
import Data.Record;

import java.io.*;

public class FileManager {

    private RandomAccessFile mainFile;
    private RandomAccessFile overflowFile;
    private int freeAdress;
    private int freeOverflowAdress;
    private int numberOfRecordsMainFile;
    private int numberOfRecordsOverflowFile;
    private String mainFileName;
    private String overflowFileName;

    public FileManager(String mainFileName, String overflowFileName) {
        try {
//            File file = new File("data.txt");
//            File file2 = new File("overflowData.txt");
//            if (file.exists()) {
//                file.delete();
//            }
//            if (file2.exists()) {
//                file2.delete();
//            }
            this.mainFile = new RandomAccessFile(mainFileName, "rw");
            this.overflowFile = new RandomAccessFile(overflowFileName, "rw");
            this.mainFileName = mainFileName;
            this.overflowFileName = overflowFileName;
//            this.mainFile.setLength(0);
//            this.overflowFile.setLength(0);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.freeAdress = -1;
        this.freeOverflowAdress = -1;
        this.numberOfRecordsMainFile = 0;
        this.numberOfRecordsOverflowFile = 0;
    }

    public void write(Block block, int address, boolean overflow) throws IOException {
        if(overflow) {
            overflowFile.seek(block.getSize() * address);
            overflowFile.write(block.toByteArray());
        } else {
            mainFile.seek(block.getSize() * address);
            mainFile.write(block.toByteArray());
        }
    }

    public Block read(int address, int blockFactor, Record record, boolean overflow) throws IOException {
        Block block = new Block(blockFactor, record.getClass());
        byte[] blockBytes = new byte[block.getSize()];
        if(overflow) {
            overflowFile.seek(block.getSize() * address);
            overflowFile.read(blockBytes, 0, block.getSize());
        } else {
            mainFile.seek(block.getSize() * address);
            mainFile.read(blockBytes, 0, block.getSize());
        }
        return block.fromByteArray(blockBytes);
    }

    public long getLengthOfMainFile() throws IOException {
        return this.mainFile.length();
    }

    public int nextAddress(int blockFactor, Record record, boolean overflow) throws IOException {
        Block block = new Block(blockFactor, record.getClass());
        if (overflow) {
            if (this.freeOverflowAdress == -1) {
                return (int) (overflowFile.length() / block.getSize());
            } else {
                Block block1 = this.read(this.freeOverflowAdress, blockFactor, record, overflow);
                if (block1.getNext() == -1) {
                    int result = this.freeOverflowAdress;
                    this.freeOverflowAdress = -1;
                    return result;
                } else {
                    int result = this.freeOverflowAdress;
                    Block block2 = this.read(block1.getNext(), blockFactor, record, overflow);
                    block2.setBefore(-1);
                    this.write(block2, block1.getNext(), overflow);
                    this.freeOverflowAdress = block1.getNext();
                    return result;
                }
            }
        } else {
            if (this.freeAdress == -1) {
                return (int) (mainFile.length() / block.getSize());
            } else {
                Block block1 = this.read(this.freeAdress, blockFactor, record, overflow);
                if (block1.getNext() == -1) {
                    int result = this.freeAdress;
                    this.freeAdress = -1;
                    return result;
                } else {
                    int result = this.freeAdress;
                    Block block2 = this.read(block1.getNext(), blockFactor, record, overflow);
                    block2.setBefore(-1);
                    this.write(block2, block1.getNext(), overflow);
                    this.freeAdress = block1.getNext();
                    return result;
                }
            }
        }
    }

    public void freeAdress(int address, int blockFactor, Record record, boolean overflow) throws IOException {
        if (overflow) {
            if (this.freeOverflowAdress == -1 || this.freeOverflowAdress == address) {
                this.freeOverflowAdress = address;
            } else {
                Block block = this.read(this.freeOverflowAdress, blockFactor, record, overflow);
                block.setBefore(address);
                this.write(block, this.freeOverflowAdress, overflow);
                Block block2 = this.read(address, blockFactor, record, overflow);
                block2.setNext(this.freeOverflowAdress);
                this.write(block2, address, overflow);
                this.freeOverflowAdress = address;
            }
        } else {
            if (this.freeAdress == -1 || this.freeAdress == address) {
                this.freeAdress = address;
            } else {
                Block block = this.read(this.freeAdress, blockFactor, record, overflow);
                block.setBefore(address);
                this.write(block, this.freeAdress, overflow);
                Block block2 = this.read(address, blockFactor, record, overflow);
                block2.setNext(this.freeAdress);
                this.write(block2, address, overflow);
                this.freeAdress = address;
            }
        }
    }

    public void freeEndBlock2(Block block, int blockFactor, Record record, boolean overflow) throws IOException {
        if (overflow) {
            while (true) {
                int endAddress = (int) this.overflowFile.length() / block.getSize() - 1;
                block = read(endAddress, blockFactor, record, overflow);
                if (block.getValidCount() == 0) {
                    int newLength = (int) this.overflowFile.length() - block.getSize();
                    //freeAdress(endAddress, blockFactor, record, overflow);

                    if (this.freeOverflowAdress == endAddress && block.getNext() == -1) {
                        this.freeOverflowAdress = -1;
                    } else if (this.freeOverflowAdress == endAddress && block.getNext() != -1) {
                        this.freeOverflowAdress = block.getNext();
                    }

                    if (block.getBefore() != -1 && block.getNext() == -1) {
                        Block beforeBlock = this.read(block.getBefore(), blockFactor, record, overflow);
                        beforeBlock.setNext(-1);
                        this.write(beforeBlock, block.getBefore(), overflow);
                    } else if (block.getBefore() != -1 && block.getNext() != -1) {
                        Block beforeBlock = this.read(block.getBefore(), blockFactor, record, overflow);
                        Block nextBlock = this.read(block.getNext(), blockFactor, record, overflow);
                        beforeBlock.setNext(block.getNext());
                        nextBlock.setBefore(block.getBefore());
                        this.write(beforeBlock, block.getBefore(), overflow);
                        this.write(nextBlock, block.getNext(), overflow);
                    }


                    if (endAddress == 0) {
                        this.overflowFile.setLength(0);
                        this.freeOverflowAdress = -1;
                        break;
                    }
                    this.overflowFile.setLength(newLength);
                } else {
                    break;
                }
            }
        } else {
            while (true) {
                int endAddress = (int) this.mainFile.length() / block.getSize() - 1;
                block = read(endAddress, blockFactor, record, overflow);
                if (block.getValidCount() == 0) {
                    int newLength = (int) this.mainFile.length() - block.getSize();
                    //freeAdress(endAddress, blockFactor, record, overflow);

                    if (this.freeAdress == endAddress && block.getNext() == -1) {
                        this.freeAdress = -1;
                    } else if (this.freeAdress == endAddress && block.getNext() != -1) {
                        this.freeAdress = block.getNext();
                    }

                    if (block.getBefore() != -1 && block.getNext() == -1) {
                        Block beforeBlock = this.read(block.getBefore(), blockFactor, record, overflow);
                        beforeBlock.setNext(-1);
                        this.write(beforeBlock, block.getBefore(), overflow);
                    } else if (block.getBefore() != -1 && block.getNext() != -1) {
                        Block beforeBlock = this.read(block.getBefore(), blockFactor, record, overflow);
                        Block nextBlock = this.read(block.getNext(), blockFactor, record, overflow);
                        beforeBlock.setNext(block.getNext());
                        nextBlock.setBefore(block.getBefore());
                        this.write(beforeBlock, block.getBefore(), overflow);
                        this.write(nextBlock, block.getNext(), overflow);
                    }

                    if (endAddress == 0) {
                        this.mainFile.setLength(0);
                        this.freeAdress = -1;
                        break;
                    }
                    this.mainFile.setLength(newLength);
                } else {
                    break;
                }
            }
        }
    }

    public void freeEndBlock(Block block, int address, int blockFactor, Record record, boolean overflow) throws IOException {
        if (overflow) {
            while (true) {
                if (block.getValidCount() == 0) {
                    int endAddress = getOverflowFileLength() / block.getSize();
                    if (address == endAddress) {
                        freeAdress(address, blockFactor, record, overflow);
                        this.overflowFile.setLength(this.overflowFile.length() - block.getSize());
                        address = endAddress - 1;
                        block = read(address, blockFactor, record, overflow);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        } else {
            while (true) {
                if (block.getValidCount() == 0) {
                    int endAddress = getMainFileLength() / block.getSize();
                    if (address == endAddress) {
                        freeAdress(address, blockFactor, record, overflow);
                        this.mainFile.setLength(this.mainFile.length() - block.getSize());
                        address = endAddress - 1;
                        block = read(address, blockFactor, record, overflow);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    public int getMainFileLength() {
        try {
            return (int) this.mainFile.length();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getOverflowFileLength() {
        try {
            return (int) this.overflowFile.length();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(BufferedWriter bufferedWriter) {
        try {
            bufferedWriter.write(String.valueOf(this.freeAdress));
            bufferedWriter.newLine();
            bufferedWriter.write(String.valueOf(this.freeOverflowAdress));
            bufferedWriter.newLine();
            bufferedWriter.write(this.mainFileName);
            bufferedWriter.newLine();
            bufferedWriter.write(this.overflowFileName);
            bufferedWriter.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load(BufferedReader bufferedReader) throws IOException {
        this.freeAdress = Integer.parseInt(bufferedReader.readLine());
        this.freeOverflowAdress = Integer.parseInt(bufferedReader.readLine());
        this.mainFile = new RandomAccessFile(bufferedReader.readLine(), "rw");
        this.overflowFile = new RandomAccessFile(bufferedReader.readLine(), "rw");
    }

    public String mainFileRecords(Record record, int blockFactor) throws IOException {
        StringBuilder toString = new StringBuilder();
        toString.append("------------------ MAIN FILE --------------------------\n");
        int numberOfRecords = 0 ;
        Block block = new Block(blockFactor, record.getClass());
        int endAddress = this.getMainFileLength() / block.getSize();
        for (int i = 0; i < endAddress; i++) {
            block = this.read(i, blockFactor, record, false);
            toString.append(i + " " + block.toString() + "\n");
            for (Record r : block.getListOfRecords()) {
                numberOfRecords++;
            }
        }
        toString.append("Number of records in main file: " + numberOfRecords);
        this.setNumberOfRecordsMainFile(numberOfRecords);
        return toString.toString();
    }

    public String overflowFileRecords(Record record, int blockFactor) throws IOException {
        StringBuilder toString = new StringBuilder();
        toString.append("------------------ OVERFLOW FILE --------------------------\n");
        int numberOfRecords = 0 ;
        Block block = new Block(blockFactor, record.getClass());
        int endAddress = this.getOverflowFileLength() / block.getSize();
        for (int i = 0; i < endAddress; i++) {
            block = this.read(i, blockFactor, record, true);
            toString.append(i + " " + block.toString() + "\n");
            for (Record r : block.getListOfRecords()) {
                numberOfRecords++;
            }
        }
        toString.append("Number of records in overflow file: " + numberOfRecords);
        this.setNumberOfRecordsOverflowFile(numberOfRecords);
        return toString.toString();
    }

    public int getNumberOfRecordsMainFile(Record record, int blockFactor) throws IOException {
        this.mainFileRecords(record, blockFactor);
        return numberOfRecordsMainFile;
    }

    public void setNumberOfRecordsMainFile(int numberOfRecordsMainFile) {
        this.numberOfRecordsMainFile = numberOfRecordsMainFile;
    }

    public int getNumberOfRecordsOverflowFile(Record record, int blockFactor) throws IOException {
        this.overflowFileRecords(record, blockFactor);
        return numberOfRecordsOverflowFile;
    }

    public void setNumberOfRecordsOverflowFile(int numberOfRecordsOverflowFile) {
        this.numberOfRecordsOverflowFile = numberOfRecordsOverflowFile;
    }
}
