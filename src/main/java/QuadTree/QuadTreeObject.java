package QuadTree;

import Data.GPS;

public interface QuadTreeObject {

    public GPS getGps1();
    public GPS getGps2();
    public String toString();
    public int getId();
    public void setId(int id);
    public String toFileString();
    public void parseFileLine(String line);
}
