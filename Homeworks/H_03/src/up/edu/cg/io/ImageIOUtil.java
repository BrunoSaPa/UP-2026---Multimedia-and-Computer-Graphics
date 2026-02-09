package up.edu.cg.io;
import up.edu.cg.core.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class ImageIOUtil {

    public static ImageData load(String path) throws IOException {
        //System.out.println("Trying to read file");
        BufferedImage img = ImageIO.read(new File(path));
        System.out.println("Success file read");

        int w = img.getWidth();
        int h = img.getHeight();
        ImageData image = new ImageData(w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                image.setPixel(x, y, new Pixel(r, g, b));
            }
        }
        return image;
    }

    public static void save(ImageData image, String path) throws IOException {
        BufferedImage img = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Pixel p = image.getPixel(x, y);
                int rgb = (p.r << 16) | (p.g << 8) | p.b;
                img.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(img, "png", new File(path));
    }
}
