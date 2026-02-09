package up.edu.cg.core;

public class Pixel {
    public int r, g, b;

    public Pixel(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Pixel copy() {
        return new Pixel(r, g, b);
    }
}