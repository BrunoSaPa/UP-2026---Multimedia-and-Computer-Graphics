package up.edu.cg.model;

public class Pixel {
    private int red;
    private int green;
    private int blue;

    public Pixel(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void invert(){
        red = 255 - red;
        green = 255 - green;
        blue = 255 - blue;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public Pixel copy() {
        return new Pixel(red, green, blue);
    }

}

