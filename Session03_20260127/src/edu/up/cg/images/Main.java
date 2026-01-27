package edu.up.cg.images;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args){
        int scalar = 4;
        int height = 300*scalar;
        int width = 400*scalar;
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);

        //lower half
        for(int y=0 ; y < height; y++){
            for(int x=0; x<width; x++){
                //calculate the middle line which is the slope
                boolean isLowerTriangle =  y >=  (double)height / width * x;
                if(isLowerTriangle){
                    image.setRGB(x,y, Color.blue.getRGB());
                }else{
                    image.setRGB(x,y, Color.red.getRGB());
                }
            }
        }


        File outputImage = new File("image.jpg");
        try{
            ImageIO.write(image,"jpg", outputImage);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
