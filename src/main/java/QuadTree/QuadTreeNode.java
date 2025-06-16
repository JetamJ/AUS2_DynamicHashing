package QuadTree;

import java.util.ArrayList;
import Data.Record;

public class QuadTreeNode<T extends QuadTreeObject> {

    private boolean root = false;
    private ArrayList<T> list;
    private QuadTreeNode<T>[] sons;
    private QuadTreeNode<T> parent;
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    private int height = 0;


    public QuadTreeNode(double x1, double y1, double x2, double y2, QuadTreeNode<T> parent) {
        this.list = new ArrayList<T>();
        this.sons = new QuadTreeNode[4];
        this.parent = parent;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        if (parent != null){
            this.height = parent.getHeight() + 1;
        } else {
            this.height = 1;
        }
    }

    public void createSons() {
        double width = this.x2 - this.x1;
        double height = this.y2 - this.y1;

        this.sons[0] = new QuadTreeNode<T>(this.x1, this.y1, this.x1 + width / 2, this.y1 + height / 2, this);
        this.sons[1] = new QuadTreeNode<T>(this.x1 + width /2, this.y1, this.x2, this.y1 + height / 2, this);
        this.sons[2] = new QuadTreeNode<T>(this.x1 + width /2, this.y1 + height /2, this.x2, this.y2, this);
        this.sons[3] = new QuadTreeNode<T>(this.x1, this.y1 + height /2, this.x1 + width /2, this.y2, this);

        ArrayList<T> itemsToDelete = new ArrayList<>();
        for (T object : this.list){
            for (QuadTreeNode<T> n : this.sons) {
                if(n.contains(object)) {
                    n.getList().add(object);
                    itemsToDelete.add(object);
                }
            }
        }

        for (T object : itemsToDelete){
            this.list.remove(object);
        }
    }

    public void createSonsWithDifferentSize(double x, double y) {

        this.sons[0] = new QuadTreeNode<T>(this.x1, this.y1, x, y, this);
        this.sons[1] = new QuadTreeNode<T>(x, this.y1, this.x2, y, this);
        this.sons[2] = new QuadTreeNode<T>(x,  y, this.x2, this.y2, this);
        this.sons[3] = new QuadTreeNode<T>(this.x1, y, x, this.y2, this);

        ArrayList<T> itemsToDelete = new ArrayList<>();
        for (T object : this.list){
            for (QuadTreeNode<T> n : this.sons) {
                if(n.contains(object)) {
                    n.getList().add(object);
                    itemsToDelete.add(object);
                }
            }
        }

        for (T object : itemsToDelete){
            this.list.remove(object);
        }
    }

    public boolean shouldCreateSons2(T object) {
        if (!this.hasSons()) {
            ArrayList<QuadTreeNode<T>> nodes = new ArrayList<>();
            nodes.add(new QuadTreeNode<T>(this.x1, this.y1, object.getGps1().getX(), object.getGps1().getY(), this));
            nodes.add(new QuadTreeNode<T>(object.getGps1().getX(), this.y1, this.x2, object.getGps1().getY(), this));
            nodes.add(new QuadTreeNode<T>(object.getGps1().getX(),  object.getGps1().getY(), this.x2, this.y2, this));
            nodes.add(new QuadTreeNode<T>(this.x1, object.getGps1().getY(), object.getGps1().getX(), this.y2, this));
            for (QuadTreeNode<T> node : nodes) {
                if (node.contains(object)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean shouldCreateSons() {
        if (!this.hasSons()) {
            ArrayList<QuadTreeNode<T>> nodes = new ArrayList<>();
            double width = this.x2 - this.x1;
            double height = this.y2 - this.y1;
            nodes.add(new QuadTreeNode<T>(this.x1, this.y1, this.x1 + width / 2, this.y1 + height / 2, this));
            nodes.add(new QuadTreeNode<T>(this.x1 + width /2, this.y1, this.x2, this.y1 + height / 2, this));
            nodes.add(new QuadTreeNode<T>(this.x1 + width /2, this.y1 + height /2, this.x2, this.y2, this));
            nodes.add(new QuadTreeNode<T>(this.x1, this.y1 + height /2, this.x1 + width /2, this.y2, this));
            for (QuadTreeNode<T> node : nodes) {
                for (T object: this.list) {
                    if (node.contains(object)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void deleteSons() {
        for (int i = 0; i < 4; i++) {
            this.sons[i] = null;
        }
    }

    public QuadTreeNode<T>[] getSons(){
        return sons;
    }

    public boolean hasSons() {
        if (this.sons[0] != null
                && this.sons[1] != null
                && this.sons[2] != null
                && this.sons[3] != null) {
            return true;
        }
        return false;
    }

    public boolean areSonsLists() {
        for (int i = 0; i < 4; i++) {
            if (this.sons[i].hasSons()) {
                return false;
            }
        }
        return true;
    }

    public void pointsToString(){
        System.out.println(this.x1 + " , " + this.y1 + " -- " + this.x2 + " , " + this.y2);
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public ArrayList<T> getList() {
        return this.list;
    }

    public QuadTreeNode<T> getParent() {
        return this.parent;
    }

    public boolean contains(T object) {
        if (object.getGps1().getX() >= this.x1
                && object.getGps1().getY() >= this.y1
                && object.getGps2().getX() <= this.x2
                && object.getGps2().getY() <= this.y2)
        {
            return true;
        }
        return false;
    }

    public boolean contains(double x1, double y1, double x2, double y2) {
        if (x1 >= this.x1
                && y1 >= this.y1
                && x2 <= this.x2
                && y2 <= this.y2)
        {
            return true;
        }
        return false;
    }

    public boolean containsOut(double x1, double y1, double x2, double y2) {
        if (x1 <= this.x1
                && y1 <= this.y1
                && x2 >= this.x2
                && y2 >= this.y2)
        {
            return true;
        }
        return false;
    }

    public boolean isConflict(T object) {
        double width = this.x2 - this.x1;
        double height = this.y2 - this.y1;
        double conflcitX = this.x1 + width / 2;
        double conflictY = this.y1 + height / 2;

        if ((object.getGps1().getX() < conflcitX
                && object.getGps2().getX() > conflcitX)
                || (object.getGps1().getY() < conflictY
                && object.getGps2().getY() > conflictY)) {
            return true;
        }

        //ked su rovnake

        return false;
    }

    public boolean isConflict2(T object) {
        if (this.hasSons()) {
            if ((object.getGps1().getX() < this.sons[0].getX2()
                    && object.getGps2().getX() > this.sons[0].getX2())
                    || (object.getGps1().getY() < this.sons[0].getY2()
                    && object.getGps2().getY() > this.sons[0].getY2())) {
                return true;
            }
        }
        //ked su rovnake

        return false;
    }

    public ArrayList<QuadTreeNode<T>> whereToSearch(double x1, double y1, double x2, double y2) {
        ArrayList<QuadTreeNode<T>> nodesToSearch = new ArrayList<>();

        for (QuadTreeNode<T> node : this.sons) {
            if (node.contains(x1,y1,x1,y1)
                    || node.contains(x2,y1,x2,y1)
                    || node.contains(x2,y2,x2,y2)
                    || node.contains(x1,y2,x1,y2)
                    || (node.getX1() >= x1
                    && node.getY1() >= y1
                    && node.getX2() <= x2
                    && node.getY2() <= y2)){
                nodesToSearch.add(node);
            }
        }
        return nodesToSearch;
    }

    public ArrayList<T> getObjectsOfSons(){
        ArrayList<T> objects = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (this.sons[i] != null) {
                objects.addAll(sons[i].getList());
            }
        }
        return objects;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("Node vo vrstve: " + getHeight() + " Hranice: " + x1 + " , " + y1 + " , " + x2 + " , " + y2 + " , " + " obsahuje: ");
        //String result = "Node vo vrstve: " + getHeightOfNode() + " Hranice: " + x1 + " , " + y1 + " , " + x2 + " , " + y2 + " , " + " obsahuje: ";
        for (T object : getList()) {
            result.append(object.toString());
        }
        return result.toString();
    }

    public int getHeight() {
        return height;
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }
}
