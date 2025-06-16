package Data;

import java.io.*;

public class GPS {

    private char width;
    private double x;
    private char height;
    private double y;

    public GPS(char width, double x, char height, double y){
        this.width = width;
        this.x = x;
        this.height = height;
        this.y = y;
    }

    public GPS() {

    }

    public char getWidth() {
        return width;
    }

    public double getX() {
        return x;
    }

    public char getHeight() {
        return height;
    }

    public double getY() {
        return y;
    }

    public boolean equals(GPS gps) {
        if (this.getX() == gps.getX()
                && this.getY() == gps.getY()
                && this.width == gps.width
                && this.height == gps.height){
            return true;
        }
        return false;
    }

    public int getSize() {
        return 2*2 + 2*8;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
        try (DataOutputStream dataOS = new DataOutputStream(byteOS)) {
            dataOS.writeChar(this.width);
            dataOS.writeDouble(this.x);
            dataOS.writeChar(this.height);
            dataOS.writeDouble(this.y);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return byteOS.toByteArray();
    }

    public GPS fromByteArray(byte[] bytes) throws ClassNotFoundException {
        try (DataInputStream datatIS = new DataInputStream(new ByteArrayInputStream(bytes))) {
            this.width = datatIS.readChar();
            this.x = datatIS.readDouble();
            this.height = datatIS.readChar();
            this.y = datatIS.readDouble();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        return "GPS { width: " + this.width + ", X: " + this.x + ", height: " + this.height + ", Y: " + this.y + " }";
    }
}
