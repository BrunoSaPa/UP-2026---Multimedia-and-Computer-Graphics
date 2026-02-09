package up.edu.cg.core;

public class ImageData {
    private final int width, height;
    private final Pixel[][] pixels;

    public ImageData(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new Pixel[height][width];
    }

    public Pixel getPixel(int x, int y) {
        return pixels[y][x];
    }

    public void setPixel(int x, int y, Pixel p) {
        pixels[y][x] = p;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }


    //pass an image to blocks so we can later convert them to groups in clusters
    public Block[] toBlocks(int blockSize) {
        int blocksX = width / blockSize;
        int blocksY = height / blockSize;

        Block[] blocks = new Block[blocksX * blocksY];
        int index = 0;

        for (int by = 0; by < blocksY; by++) {
            for (int bx = 0; bx < blocksX; bx++) {
                Pixel[] blockPixels = new Pixel[blockSize * blockSize];
                int k = 0;

                for (int y = 0; y < blockSize; y++) {
                    for (int x = 0; x < blockSize; x++) {
                        Pixel p = getPixel(bx * blockSize + x, by * blockSize + y);
                        blockPixels[k++] = p.copy();
                    }
                }
                blocks[index++] = new Block(blockPixels);
            }
        }
        return blocks;
    }
}

