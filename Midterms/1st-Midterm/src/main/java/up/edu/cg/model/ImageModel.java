package up.edu.cg.model;

public class ImageModel {

    private Pixel[][] pixels;
    private int height;
    private int width;

    public ImageModel(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new Pixel[height][width];
    }

    public Pixel getPixel(int x, int y) {
        return pixels[y][x];
    }

    public void setPixel(int x, int y, Pixel pixel) {
        pixels[y][x] = pixel;
    }

    public int getHeight() { return height; }
    public int getWidth() { return width; }

    //needed to deep copy so i dont modify the original, so changes to one image dont affect the other
    public ImageModel copy() {

        ImageModel copy = new ImageModel(this.width, this.height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                copy.pixels[y][x] = this.pixels[y][x].copy();
            }
        }

        return copy;
    }

}

