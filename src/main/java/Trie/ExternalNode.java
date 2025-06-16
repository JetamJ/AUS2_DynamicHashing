package Trie;

public class ExternalNode extends Node {

    private int address;
    private int count;
    private  int overFlowBlockCount;

    public ExternalNode(int address) {
        this.address = address;
        this.count = 0;
        this.overFlowBlockCount = 0;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Node getBrother() {
        InternalNode parent = (InternalNode) this.parent;
        if (parent == null) {
            return null;
        }
        if (parent.getLeftSon() == this) {
            return parent.getRightSon();
        } else {
            return parent.getLeftSon();
        }
    }

    public void countDecrement() {
        this.count--;
    }

    public void countIncrement() {
        this.count++;
    }

    public int getOverFlowBlockCount() {
        return overFlowBlockCount;
    }

    public void setOverFlowBlockCount(int overFlowBlockCount) {
        this.overFlowBlockCount = overFlowBlockCount;
    }

    public void overFlowBlockCountDecrement() {
        this.overFlowBlockCount--;
    }

    public void overFlowBlockCountIncrement() {
        this.overFlowBlockCount++;
    }
}
