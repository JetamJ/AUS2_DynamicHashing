package DH;

import Data.Block;
import Data.Property;
import Data.Record;
import Trie.*;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class DynamicHash<T extends Record> {

    private Trie trie;
    private int blockFactor;
    private int overflowBlockFactor;
    private FileManager fileManger;
    private int maxHeight;

    public DynamicHash (int maxHeight, int blockFactor, int overflowBlockFactor, String mainFileName, String overflowFileName) {
        this.blockFactor = blockFactor;
        this.overflowBlockFactor = overflowBlockFactor;
        this.fileManger = new FileManager(mainFileName, overflowFileName);
        this.trie = new Trie();
        this.maxHeight = maxHeight;
    }
    public void insert(T record) throws IOException, NoSuchAlgorithmException {
        Node node = this.trie.getNodeForRecord(record);
        Block block = new Block(blockFactor, record.getClass());
        boolean endOfInsert = false;

        if (node == null) {
            //ak vkladam prvy record
            ExternalNode exNode = new ExternalNode(0);
            exNode.setLevel(0);
            trie.setRoot(exNode);
            block = new Block(this.blockFactor, record.getClass());
            block.addRecord(record);
            exNode.countIncrement();
            fileManger.write(block, exNode.getAddress(), false);
        } else {
            //ak vkladam do bloku kde je miesto
            if (((ExternalNode) node).getAddress() == -1) {
                ((ExternalNode) node).setAddress(fileManger.nextAddress(this.blockFactor, record, false));
                block = new Block(this.blockFactor, record.getClass());
            } else {
                block = fileManger.read(((ExternalNode) node).getAddress(), this.blockFactor, record,false);
            }
            if (block.getValidCount() < this.blockFactor) {
                block.addRecord(record);
                ((ExternalNode) node).countIncrement();
                fileManger.write(block, ((ExternalNode) node).getAddress(), false);
            } else {
                //blok je plny
                while (true) {
                    if (node.getLevel() != this.maxHeight) {
                        InternalNode inNode = new InternalNode();
                        inNode.setLevel(node.getLevel());
                        ExternalNode exNodeLeft = new ExternalNode(((ExternalNode) node).getAddress());
                        exNodeLeft.setLevel(node.getLevel() + 1);
                        ExternalNode exNodeRight = new ExternalNode(fileManger.nextAddress(blockFactor, record, false));
                        exNodeRight.setLevel(node.getLevel() + 1);
                        inNode.setLeftSon(exNodeLeft);
                        inNode.setRightSon(exNodeRight);
                        exNodeLeft.setParent(inNode);
                        exNodeRight.setParent(inNode);

                        //vytvaram nove vrcholy
                        if (node == trie.getRoot()) {
                            trie.setRoot(inNode);
                        } else {
                            Node parent = node.getParent();
                            inNode.setParent(parent);
                            if (((InternalNode) parent).getLeftSon() == node) {
                                ((InternalNode) parent).setLeftSon(inNode);
                            } else {
                                ((InternalNode) parent).setRightSon(inNode);
                            }
                        }

                        //premiestnujem recordy do novych blokov

                        //block = fileManger.read(((ExternalNode) node).getAddress(), this.blockFactor, record, false);
                        Block leftBlock = new Block(this.blockFactor, record.getClass());
                        Block rightBlock = new Block(this.blockFactor, record.getClass());
                        for (Record r : block.getListOfRecords()) {
                            if (r.getHash().get(node.getLevel())) {
                                rightBlock.addRecord(r);
                                exNodeRight.countIncrement();
                            } else {
                                leftBlock.addRecord(r);
                                exNodeLeft.countIncrement();
                            }
                        }

                        //vkladam novy udaj
                        if (record.getHash().get(node.getLevel()) && rightBlock.getValidCount() < this.blockFactor) {
                            rightBlock.addRecord(record);
                            exNodeRight.countIncrement();
                            endOfInsert = true;
                        }

                        if (!record.getHash().get(node.getLevel()) && leftBlock.getValidCount() < this.blockFactor) {
                            leftBlock.addRecord(record);
                            exNodeLeft.countIncrement();
                            endOfInsert = true;
                        }

                        //ak je jeden z listov prazdy tak sa adressa uvolni
                        if (rightBlock.getValidCount() == 0) {
                            fileManger.write(rightBlock, exNodeRight.getAddress(), false);
                            fileManger.freeAdress(exNodeRight.getAddress(), this.blockFactor, record, false);
                            exNodeRight.setAddress(-1);
                        }
                        if (leftBlock.getValidCount() == 0) {
                            fileManger.write(leftBlock, exNodeLeft.getAddress(), false);
                            fileManger.freeAdress(exNodeLeft.getAddress(), this.blockFactor, record, false);
                            exNodeLeft.setAddress(-1);
                        }

                        if (endOfInsert) {
                            fileManger.write(rightBlock, exNodeRight.getAddress(), false);
                            fileManger.write(leftBlock, exNodeLeft.getAddress(), false);
                            return;
                        }

                        node = trie.getNodeForRecord(record);
                    } else {
                        //trie dosiahol maximalnu vysku, vkladam do preplnujuceho suboru
                        Block overflowBlock;
                        block = fileManger.read(((ExternalNode) node).getAddress(), this.blockFactor, record, false);
                        if (block.getOverflowAddress() == -1) {
                            //este nema preplnujuci
                            int address = fileManger.nextAddress(overflowBlockFactor, record, true);
                            block.setOverflowAddress(address);
                            ((ExternalNode) node).overFlowBlockCountIncrement();
                            fileManger.write(block, ((ExternalNode) node).getAddress(), false);

                            overflowBlock = new Block(this.overflowBlockFactor, record.getClass());
                            overflowBlock.addRecord(record);
                            ((ExternalNode) node).countIncrement();
                            fileManger.write(overflowBlock, address, true);
                            return;
                        } else {
                            //uz ma preplnujuci
                            overflowBlock = fileManger.read(block.getOverflowAddress(), this.overflowBlockFactor, record, true);
                            int overFlowAddress = block.getOverflowAddress();
                            while (true) {
                                if (overflowBlock.getValidCount() < this.overflowBlockFactor) {
                                    //neni plny
                                    overflowBlock.addRecord(record);
                                    ((ExternalNode) node).countIncrement();
                                    fileManger.write(overflowBlock, overFlowAddress, true);
                                    return;
                                } else {
                                    //je plny
                                    if (overflowBlock.getOverflowAddress() == -1) {
                                        int address = fileManger.nextAddress(overflowBlockFactor, record, true);
                                        overflowBlock.setOverflowAddress(address);
                                        ((ExternalNode) node).overFlowBlockCountIncrement();
                                        fileManger.write(overflowBlock, overFlowAddress, true);

                                        overflowBlock = new Block(this.overflowBlockFactor, record.getClass());
                                        overflowBlock.addRecord(record);
                                        ((ExternalNode) node).countIncrement();
                                        fileManger.write(overflowBlock, address, true);
                                        return;
                                    }
                                    overFlowAddress = overflowBlock.getOverflowAddress();
                                    overflowBlock = fileManger.read(overflowBlock.getOverflowAddress(), this.overflowBlockFactor, record, true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public T find(T record) throws NoSuchAlgorithmException, IOException {
        Node node = trie.getNodeForRecord(record);

        if (node == null || ((ExternalNode) node).getAddress() == -1) {
            return null;
        }

        Block block = fileManger.read(((ExternalNode) node).getAddress(), this.blockFactor, record, false);
        Record result = block.find(record);
        if (result != null) {
            return (T) result;
        }

        while (true) {
            if (block.getOverflowAddress() != -1) {
                block = fileManger.read(block.getOverflowAddress(), this.overflowBlockFactor, record, true);
                result = block.find(record);
                if (result != null) {
                    return (T) result;
                }
            } else {
                return null;
            }
        }
    }

    public boolean delete(T record) throws NoSuchAlgorithmException, IOException {
        Node node = trie.getNodeForRecord(record);

        if (node == null || ((ExternalNode) node).getAddress() == -1) {
            return false;
        }

        Block block = fileManger.read(((ExternalNode) node).getAddress(), this.blockFactor, record, false);
        Record result = block.find(record);
        boolean merged = false;

        if (result != null) {
            block.removeRecord(record);
            ((ExternalNode) node).countDecrement();
            fileManger.write(block, ((ExternalNode) node).getAddress(), false);
            int address = ((ExternalNode) node).getAddress();

            while(true) {
                //ide sa skusit skracovat strom
                Node brother = ((ExternalNode) node).getBrother();
                if (brother == null) {
                    break;
                }
                if (brother.getClass() == ExternalNode.class) {
                    Block block1 = fileManger.read(((ExternalNode) node).getAddress(), this.blockFactor, record, false);
                    //ak brat nema platnu adresu tak sa vytvori prazdny blok
                    Block block2 = new Block(this.blockFactor, record.getClass());
                    if (((ExternalNode) brother).getAddress() != -1) {
                        block2 = fileManger.read(((ExternalNode) brother).getAddress(), this.blockFactor, record, false);
                    }
                    if (block1.getValidCount() + block2.getValidCount() <= this.blockFactor && block2.getOverflowAddress() == -1 && block1.getOverflowAddress() == -1) {
                        //pouzije sa existujuca adresa
                        ExternalNode exNode = new ExternalNode(((ExternalNode) node).getAddress());

                        Block newBlock = new Block(this.blockFactor, record.getClass());
                        for (Record r : block1.getListOfRecords()) {
                            newBlock.addRecord(r);
                        }
                        for (Record r : block2.getListOfRecords()) {
                            newBlock.addRecord(r);
                        }
                        block1.removeAll();
                        block2.removeAll();
                        //((ExternalNode) node).setCount(0);
                        ((ExternalNode) brother).setCount(0);
                        //fileManger.write(block1, ((ExternalNode) node).getAddress(), false);


                        //brat sa uvolni
                        if (((ExternalNode) brother).getAddress() != -1) {
                            fileManger.write(block2, ((ExternalNode) brother).getAddress(), false);
                            fileManger.freeAdress(((ExternalNode) brother).getAddress(), this.blockFactor, record, false);
                        }

                        exNode.setCount(newBlock.getValidCount());
                        fileManger.write(newBlock, ((ExternalNode) node).getAddress(), false);

                        Node parent = node.getParent();
                        exNode.setLevel(parent.getLevel());
                        exNode.setParent(parent.getParent());
                        if (exNode.getParent() == null) {
                            this.trie.setRoot(exNode);
                        } else {
                            parent = exNode.getParent();
                            if (((InternalNode) parent).getLeftSon() == node.getParent()) {
                                ((InternalNode) parent).setLeftSon(exNode);
                            } else {
                                ((InternalNode) parent).setRightSon(exNode);
                            }
                        }
                        merged = true;
                        node = exNode;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }

            if (block.getValidCount() == 0 && !merged && block.getOverflowAddress() == -1) {
                fileManger.freeAdress(((ExternalNode) node).getAddress(), this.blockFactor, record, false);
                ((ExternalNode) node).setAddress(-1);
            }

            fileManger.freeEndBlock2(block, this.blockFactor, record, false);
            return true;
        }

        Block mainBlock = block;

        while (true) {
            if (block.getOverflowAddress() != -1) {
                int overflowAdress = block.getOverflowAddress();
                block = fileManger.read(block.getOverflowAddress(), this.overflowBlockFactor, record, true);
                result = block.find(record);
                if (result != null) {
                    block.removeRecord(record);
                    ((ExternalNode) node).countDecrement();
                    fileManger.write(block, overflowAdress, true);

//                    if (block.getValidCount() == 0) {
//                        fileManger.freeAdress(overflowAdress, this.overflowBlockFactor, record, true);
//                    }

                    //striasanie
                    if (((ExternalNode) node).getCount() <= (((ExternalNode) node).getOverFlowBlockCount() - 1) * this.overflowBlockFactor + this.blockFactor) {
                        shake(mainBlock, record, node);
                        ((ExternalNode) node).overFlowBlockCountDecrement();
                    }

                    fileManger.freeEndBlock2(block, this.overflowBlockFactor, record, true);
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    public void shake(Block block, Record record, Node node) throws IOException {
        ArrayList<Block> blocks = new ArrayList<>();
        blocks.add(block);
        Block lastBlock = new Block(this.overflowBlockFactor, record.getClass());
        while (block.getOverflowAddress() != -1) {
            block = fileManger.read(block.getOverflowAddress(), this.overflowBlockFactor, record, true);
            blocks.add(block);
            lastBlock = block;
        }

        for (Record r : lastBlock.getListOfRecords()) {
            //block v hlavnom subore
            if (blocks.get(0).getValidCount() < this.blockFactor) {
                blocks.get(0).addRecord(r);
                fileManger.write(blocks.get(0), ((ExternalNode) node).getAddress(), false);
            } else {
                //bloky v preplnujucom subore
                for (int i = 1; i < blocks.size(); i++) {
                    if (blocks.get(i).getValidCount() < this.overflowBlockFactor && blocks.get(i) != lastBlock) {
                        blocks.get(i).addRecord(r);
                        fileManger.write(blocks.get(i), blocks.get(i-1).getOverflowAddress(), true);
                        break;
                    }
                }
            }
        }

        lastBlock.removeAll();
        fileManger.write(blocks.get(blocks.size() - 1), blocks.get(blocks.size() - 2).getOverflowAddress(), true);
        fileManger.freeAdress(blocks.get(blocks.size() - 2).getOverflowAddress(), this.overflowBlockFactor, record, true);

        if (blocks.size() == 2) {
            blocks.get(0).setOverflowAddress(-1);
            fileManger.write(blocks.get(0), ((ExternalNode) node).getAddress(), false);
        } else {
            blocks.get(blocks.size() - 2).setOverflowAddress(-1);
            fileManger.write(blocks.get(blocks.size() - 2), blocks.get(blocks.size() - 3).getOverflowAddress(), true);
        }

    }

    public boolean edit(T objectToEdit, T newObject) throws NoSuchAlgorithmException, IOException {
        T object = this.find(objectToEdit);
        if (object == null) {
            System.out.println(objectToEdit + " is not present in DynamicHash.");
            return false;
        }

        if (object.getGps1().getX() != newObject.getGps1().getX() || object.getGps1().getY() != newObject.getGps1().getY()
                || object.getGps2().getX() != newObject.getGps2().getX() || object.getGps2().getY() != newObject.getGps2().getY()) {
            this.delete(objectToEdit);
            this.insert(newObject);
        } else {

            Node node = trie.getNodeForRecord(objectToEdit);

            if (node == null) {
                System.out.println(objectToEdit + " is not present in DynamicHash.");
                return false;
            }

            Block block = fileManger.read(((ExternalNode) node).getAddress(), this.blockFactor, objectToEdit, false);
            Record result = block.find(objectToEdit);
            if (result != null) {
                this.editObject(result, newObject, block, ((ExternalNode) node).getAddress(), false);
                return true;
            }

            while (true) {
                if (block.getOverflowAddress() != -1) {
                    int address = block.getOverflowAddress();
                    block = fileManger.read(block.getOverflowAddress(), this.overflowBlockFactor, objectToEdit, true);
                    result = block.find(objectToEdit);
                    if (result != null) {
                        this.editObject(result, newObject, block, address, true);
                        return true;
                    }
                } else {
                    System.out.println(objectToEdit + " is not present in DynamicHash.");
                    return false;
                }
            }
        }



        return true;
    }

    private void editObject(Record objectToEdit, T newObject, Block block, int address, boolean overflow) throws IOException {
        if (!objectToEdit.getDescription().equals(newObject.getDescription())) {
            objectToEdit.setDescription(newObject.getDescription());
        }
        if (objectToEdit.getList() != newObject.getList()) {
            objectToEdit.setList(newObject.getList());
        }
        if (objectToEdit.getClass() == Property.class && newObject.getClass() == Property.class
                && ((Property) objectToEdit).getRegisterNumber() != ((Property) newObject).getRegisterNumber()){
            ((Property) objectToEdit).setRegisterNumber(((Property) newObject).getRegisterNumber());
        }
        fileManger.write(block, address, overflow);
    }

    public void save(String filePath) {
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(String.valueOf(this.blockFactor));
            bufferedWriter.newLine();
            bufferedWriter.write(String.valueOf(this.overflowBlockFactor));
            bufferedWriter.newLine();
            bufferedWriter.write(String.valueOf(this.maxHeight));
            bufferedWriter.newLine();
            fileManger.save(bufferedWriter);
            trie.save(bufferedWriter);
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load(String filePath) {
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            this.blockFactor = Integer.parseInt(bufferedReader.readLine());
            this.overflowBlockFactor = Integer.parseInt(bufferedReader.readLine());
            this.maxHeight = Integer.parseInt(bufferedReader.readLine());
            fileManger.load(bufferedReader);
            trie.load(bufferedReader);
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String mainFileRecords(T record) throws IOException {
        return this.fileManger.mainFileRecords(record, this.blockFactor);
    }

    public String overflowFileRecords(T record) throws IOException {
        return this.fileManger.overflowFileRecords(record, this.overflowBlockFactor);
    }

    public int getNumberOfRecordsMainFile(T record) throws IOException {
        return this.fileManger.getNumberOfRecordsMainFile(record, this.blockFactor);
    }

    public int getNumberOfRecordsOverflowFile(T record) throws IOException {
        return this.fileManger.getNumberOfRecordsOverflowFile(record, this.overflowBlockFactor);
    }


    public String trieToString(Record record) throws IOException {
        StringBuilder result = new StringBuilder();
        iterateTrie(this.trie.getRoot(), result, record);
        return result.toString();
    }

    private void iterateTrie(Node node, StringBuilder string, Record record) throws IOException {
        if (node != null) {
            if (node instanceof ExternalNode) {
                if (((ExternalNode) node).getAddress() == -1) {
                    string.append("Node na urovni: " + node.getLevel() + " ma adressu -1.\n");
                } else {
                    Block block = fileManger.read(((ExternalNode) node).getAddress(), this.blockFactor, record ,false);
                    string.append("Node na urovni: " + node.getLevel() + " - ");
                    string.append(block.toString() + "\n");
                }
            } else {
                iterateTrie(((InternalNode) node).getLeftSon(), string, record);
                iterateTrie(((InternalNode) node).getRightSon(), string, record);
            }
        }
    }
}
