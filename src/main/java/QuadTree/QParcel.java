package QuadTree;


import java.util.ArrayList;
import Data.GPS;

public class QParcel implements QuadTreeObject, Comparable<QuadTreeObject> {

    private int id;
    private GPS gps1;
    private GPS gps2;

    public QParcel(int id, GPS gps1, GPS gps2){
        this.id = id;
        this.gps1 = gps1;
        this.gps2 = gps2;
    }

    public QParcel() {};


    @Override
    public GPS getGps1() {
        return gps1;
    }

    @Override
    public GPS getGps2() {
        return gps2;
    }

    @Override
    public String toString() {
        return "Parcela  " + this.id + " GPS1: " + gps1.getX() + " , " + gps1.getY() + " GPS2: " + gps2.getX() + " , " + gps2.getY();
    }
    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toFileString() {
        String line = "QuadTree.QParcel;" + this.id + ";"
                + this.gps1.getWidth() + ";" + this.gps1.getX() + ";"
                + this.gps1.getHeight() + ";" + this.gps1.getY() + ";"
                + this.gps2.getWidth() + ";" + this.gps2.getX() + ";"
                + this.gps2.getHeight() + ";" + this.gps2.getY();
        return line;
    }

    @Override
    public void parseFileLine(String line) {
        String[] variables = line.split(";");
        this.id = Integer.parseInt(variables[1]);
        this.gps1 = new GPS(variables[2].toCharArray()[0], Double.valueOf(variables[3]), variables[4].toCharArray()[0], Double.valueOf(variables[5]));
        this.gps2 = new GPS(variables[6].toCharArray()[0], Double.valueOf(variables[7]), variables[8].toCharArray()[0], Double.valueOf(variables[9]));
    }

    @Override
    public int compareTo(QuadTreeObject o) {
        if (this.id == o.getId()
//                && this.gps1.equals(o.getGps1())
//                && this.gps2.equals(o.getGps2())
        ){
            return 0;
        }
        return -1;
    }
}
