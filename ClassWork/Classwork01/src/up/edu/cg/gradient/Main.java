package up.edu.cg.gradient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class Main {
    public static void main(String[] args){
        //define and initialize dimention/aspect ratio of the image
        int scalar = 500;
        int height = 3*scalar;
        int width = 4*scalar;
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);

        //gradient background
        //this is the middle of the gradient.
        int axis = (int)(height - height * 0.1);
        // how big is the gradient
        int amplitude = (int)Math.round(height*0.35);

        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){

                int red = 255;
                int green;
                int blue = 0;

                if (y > axis) {
                    green = 0;
                } else {
                    float t = (axis - y) / (float) amplitude;
                    if (t < 0) t = 0;
                    if (t > 1) t = 1;
                    //t closer to 1 is yellow and closer to 0 is red. because red = (255,0,0) in rgb
                    green = (int)(255 * t);
                }

                int rgb = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, rgb);
            }
        }

        //draw center girl
        DrawGirl(image, (int)Math.round(width*0.5), height, width);
        //draw left girl
        DrawGirl(image, (int)Math.round(width*0.25), height, width);
        //draw right girl
        DrawGirl(image, (int)Math.round(width*0.75), height, width);


        File outputImage = new File("test.jpg");
        try{
            ImageIO.write(image,"jpg", outputImage);
        }catch (
                IOException e){
            throw new RuntimeException(e);
        }
    }

    static void DrawGirl(BufferedImage image, int xPos, int height, int width){

        //define parameters like centers of the body parts and sizes
        int radiusHead = (int)Math.round(height*0.05);
        int yHeadPos = (int)Math.round(height*0.35);
        int bunRadius = (int)Math.round(radiusHead*0.5);
        //offset the bun
        int yBunPos = yHeadPos - (int)Math.round(width*0.03);
        int xBunPos = xPos + (int)Math.round(width*0.03);
        //triangel
        int yTopTrianglePos = (int)Math.round(height*0.45);
        int halfWidthAtBottom = (int)Math.round(height*0.1);
        int triangleHeight = (int)Math.round(height*0.2);
        //lower triangle
        int yTopTrianglePosLower = (int)Math.round(height*0.55);
        int halfWidthAtBottomLower = (int)Math.round(height*0.1);
        int triangleHeightLower = (int)Math.round(height*0.15);
        //legs
        int legTop = (int)(height * 0.65);
        int legBottom = (int)(height * 0.85);

        int legWidth = (int)(width * 0.02);
        int legGap = (int)(width * 0.015);

        //neck
        int neckTop = yHeadPos + radiusHead;
        int neckHeight = (int)(height * 0.05);
        int neckHalfWidth = (int)(radiusHead * 0.2);

        for(int y=0 ; y < height; y++){
            for(int x=0; x< width; x++){
                //draw head
                int dx = x - xPos;
                int dy = y - yHeadPos;
                if ((dx * dx + dy * dy) < (radiusHead * radiusHead)) {
                    image.setRGB(x, y, Color.black.getRGB());
                }
                //draw octagon
                dx = Math.abs(x - xBunPos);
                dy = Math.abs(y - yBunPos);
                if (dx <= bunRadius && dy <= bunRadius && (dx + dy) <= bunRadius * 1.4) {
                    image.setRGB(x, y, Color.black.getRGB());
                }

                //draw inverted triangle for the upper body
                dx = Math.abs(x - xPos);
                if (y >= yTopTrianglePos && y <= yTopTrianglePos + triangleHeight) {
                    float t = (y - yTopTrianglePos) / (float) triangleHeight;
                    float halfWidth = (1-t) * halfWidthAtBottom;

                    if (dx <= halfWidth) {
                        image.setRGB(x, y, Color.black.getRGB());
                    }
                }

                //draw triangle lower body
                dx = Math.abs(x - xPos);
                if (y >= yTopTrianglePosLower && y <= yTopTrianglePosLower + triangleHeightLower) {
                    float t = (y - yTopTrianglePosLower) / (float) triangleHeightLower;
                    float halfWidth = t * halfWidthAtBottomLower;

                    if (dx <= halfWidth) {
                        image.setRGB(x, y, Color.black.getRGB());
                    }
                }

                //legs
                dx = Math.abs(x - xPos);
                if (y >= legTop && y <= legBottom) {
                    if (dx >= legGap && dx <= legGap + legWidth) {
                        image.setRGB(x, y, Color.black.getRGB());
                    }
                }

                //neck
                if (y >= neckTop && y <= neckTop + neckHeight) {
                    if (x >= xPos - neckHalfWidth && x <= xPos + neckHalfWidth) {
                        image.setRGB(x, y, Color.black.getRGB());
                    }
                }
            }
        }


    }
}
