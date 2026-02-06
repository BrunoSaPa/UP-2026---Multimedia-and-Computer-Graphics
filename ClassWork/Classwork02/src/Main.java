import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//image.setRGB(x,y, Color.blue.getRGB());

public class Main{
    public static void main(String[] args){
        int scalar = 10000;
        int height = 1*scalar;
        int width = 1*scalar;
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);


        int r,g,b;

        //points for blue corner
        int xB = width/2;
        int yB = 0;
        int rB=0,gB=0,bB=255;

        //points for red corner
        int xR = 0;
        int yR= height;
        int rR=255,gR=0,bR=0;

        //points for green corner
        int xG = width;
        int yG = height;
        int rG=0,gG=255,bG=0;




        for(int x = 0; x < width; x++){
            for(int y=0; y<height; y++){
                //deltas
                double areaMainTriangle = ((yG - yB) * (xR - xB)) + ((xB - xG) * (yR - yB));
                double d1 = (double) (((yG - yB) * (x - xB)) + ((xB - xG) * (y - yB))) /areaMainTriangle;
                double d2 = (double) (((yB - yR) * (x - xB)) + ((xR - xB) * (y - yB))) /areaMainTriangle;
                double d3 = 1 - d1 - d2;

                if (d1 >= 0 && d2 >= 0 && d3 >= 0){
                    r= (int)(d1*rR + d2*rG + d3*rB);
                    g= (int)(d1*gR + d2*gG + d3*gB);
                    b= (int)(d1*bR + d2*bG + d3*bB);
                    int rgb = (255 << 24) | (r   << 16) | (g   << 8)  | b;
                    image.setRGB(x,y,rgb);
                }
            }
        }

        File outputImage = new File("triangle.jpg");
        try{
            ImageIO.write(image,"jpg", outputImage);
        }catch (
                IOException e){
            throw new RuntimeException(e);
        }

    }
}