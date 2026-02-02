package up.edu.cg.waves;

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

        //white background
        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }

        //drawing sun
        //define center of the circle
        int centerX = (int)Math.round(width * 0.2);
        int centerY = (int)Math.round(width * 0.2);
        //define radius
        int radius = (int)Math.round(width * 0.1);

        //draw rays
        DrawRay(4, centerX, centerY, (int)Math.round(radius * 1.5), image, 0);
        DrawRay(4, centerX, centerY, (int)Math.round(radius * 1.3),image, Math.PI /4);

        //draw sun
        for(int y=0 ; y < height; y++){
            for(int x=0; x< width; x++){
                int dx = x - centerX;
                int dy = y - centerY;
                if ((dx * dx + dy * dy) < (radius * radius)) {
                    image.setRGB(x, y, Color.yellow.getRGB());
                }
            }
        }

        //draw grass (area bellow a function)
        //midplane for the function
        int axis = (int)(height - height * 0.2);
        double amplitude = height * 0.05;
        double frequency = 15 * Math.PI / width;

        for (int x = 0; x < width; x++) {

            int ySin = (int)(
                    axis - amplitude * Math.sin(frequency * x)
            );

            for (int y = ySin; y < height; y++) {
                image.setRGB(x, y, Color.green.getRGB());
            }
        }

        File outputImage = new File("final.jpg");
        try{
            ImageIO.write(image,"jpg", outputImage);
        }catch (
                IOException e){
            throw new RuntimeException(e);
        }
    }

    static void DrawRay(int amountRays, int centerX, int centerY, int radius , BufferedImage image, double offset){
        int deg = 360;
        //steps is the amount of dots drawn in between points
        int steps = radius;
        //convert the amount of rays ill have to deg
        int rayDeg = 360/amountRays;

        for (int i = 0; i < amountRays; i++) {
            //calculate outermost point, i need a point in the outside and a point in the center to do linear interpolation
            double currentDegree = (((i*rayDeg)*Math.PI)/180) - offset ;
            int currentPosX = centerX + (int)(radius * Math.cos(currentDegree));
            int currentPosY = centerY + (int)(radius * Math.sin(currentDegree));
            for (int j=0; j<=steps; j++){
                double t = j / (double) steps;
                int x = (int)(centerX + t * (currentPosX - centerX));
                int y = (int)(centerY + t * (currentPosY - centerY));
                image.setRGB(x, y, Color.red.getRGB());
            }
        }
    }
}
