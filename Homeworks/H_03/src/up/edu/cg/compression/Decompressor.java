package up.edu.cg.compression;
import up.edu.cg.core.*;

public class Decompressor {

    public static ImageData reconstruct(
            int width, int height,
            int blockSize,
            Block[] codebook,
            byte[] indices) {

        ImageData img = new ImageData(width, height);

        int idx = 0;
        for (int by = 0; by < height / blockSize; by++) {
            for (int bx = 0; bx < width / blockSize; bx++) {
                // the 0xFF is to make sure it is not bigger than 255
                Block b = codebook[indices[idx++] & 0xFF];
                writeBlock(img, b, bx, by, blockSize);
            }
        }
        return img;
    }

    private static void writeBlock(ImageData img, Block b, int bx, int by, int blockSize) {

        int k = 0;
        for (int y = 0; y < blockSize; y++) {
            for (int x = 0; x < blockSize; x++) {
                img.setPixel(
                        bx * blockSize + x,
                        by * blockSize + y,
                        b.getData()[k++].copy()
                );
            }
        }
    }
}
