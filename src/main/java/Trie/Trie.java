package Trie;

import Data.Record;

import javax.naming.ldap.ExtendedRequest;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Trie {

    private Node root;
    private int level;
    public Trie(Node root) {
        this.root = root;
    }

    public Trie () {

    }

    public Node getNodeForRecord(Record record) throws NoSuchAlgorithmException {
        if (this.root == null) {
            return null;
        }
        Node node = this.root;

        while(true) {
            if (node instanceof ExternalNode) {
                return node;
            }
            if (record.getHash().get(node.getLevel())) {
                node = ((InternalNode) node).getRightSon();
            } else {
                node = ((InternalNode) node).getLeftSon();
            }
        }
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void save(BufferedWriter bufferedWriter) throws IOException {
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(this.root);

        while(!nodes.isEmpty()) {
            Node node = nodes.get(nodes.size() - 1);
            if (node instanceof InternalNode) {
                bufferedWriter.write("Internal;" +  node.getLevel());
                bufferedWriter.newLine();
                if (((InternalNode) node).getLeftSon() != null) {
                    nodes.add(((InternalNode) node).getLeftSon());
                }
                if (((InternalNode) node).getRightSon() != null) {
                    nodes.add(((InternalNode) node).getRightSon());
                }
            } else {
                ExternalNode exNode = (ExternalNode) node;
                bufferedWriter.write(exNode.getAddress() + ";" + exNode.getCount() + ";" + exNode.getOverFlowBlockCount() + ";" + exNode.getLevel());
                bufferedWriter.newLine();
            }
            nodes.remove(node);
        }
    }

    public void load(BufferedReader bufferedReader) throws IOException {
        Node node;
        InternalNode prevNode = null;
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] paramas = line.split(";");
            if (!paramas[0].equals("Internal")) {
                //vytvara externy
                node = new ExternalNode(Integer.parseInt(paramas[0]));
                ((ExternalNode) node).setCount(Integer.parseInt(paramas[1]));
                ((ExternalNode) node).setOverFlowBlockCount(Integer.parseInt(paramas[2]));
                node.setLevel(Integer.parseInt(paramas[3]));
            } else {
                //vytvara interny
                node = new InternalNode();
                node.setLevel(Integer.parseInt(paramas[1]));
            }

            if (prevNode != null) {
                while (true) {
                    if (prevNode.getRightSon() == null) {
                        prevNode.setRightSon(node);
                        node.setParent(prevNode);
                        break;
                    }
                    if (prevNode.getLeftSon() == null) {
                        prevNode.setLeftSon(node);
                        node.setParent(prevNode);
                        break;
                    }
                    prevNode = (InternalNode) prevNode.getParent();
                }
            } else {
                this.root = node;
            }

            if (node instanceof InternalNode) {
                prevNode = (InternalNode) node;
            }
        }
    }
}
