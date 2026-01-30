package up.edu.cg.clock;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//image.setRGB(x,y, Color.blue.getRGB());

public class Main {
    public static void main(String[] args){
        //define img constraints
        int scalar = 500;
        int height = 3*scalar;
        int width = 4*scalar;
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);

        //define center of the image
        int centerX = (int)width/2;
        int centerY = (int)height/2;
        //define radius for the outer circle
        int outterCircle = (int) Math.round(height*0.4);
        //i need to define a tolerance, otherwise it will only draw points
        int tolerance = scalar;



        //draw outter circle
        for(int y=0 ; y < height; y++){
            for(int x=0; x< width; x++){
                int dx = x - centerX;
                int dy = y - centerY;
                if (Math.abs((dx * dx + dy * dy) - (outterCircle * outterCircle)) <= tolerance) {
                    image.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }

        //draw hour markers
        //define diameter for the hour markers
        int markersCircleRadius = (int) Math.round(outterCircle*0.8);
        double minuteStep = 2 * Math.PI / 60;
        double hourStep   = 2 * Math.PI / 12;

        //used the almost the same function to draw the hour markers but centered on each point
        for (int h = 0; h<12 ; h++){
            double currentAngle = h*hourStep;
            int centerMarkerX = centerX + (int)(markersCircleRadius * Math.cos(currentAngle));
            int centerMarkerY = centerY + (int)(markersCircleRadius * Math.sin(currentAngle));
            for(int y=0 ; y < height; y++){
                for(int x=0; x< width; x++){
                    int dx = x - centerMarkerX;
                    int dy = y - centerMarkerY;
                    // 1*1 is the radius of the circles for the hour markers
                    if (Math.abs((dx * dx + dy * dy) - (1)) <= tolerance) {
                        image.setRGB(x, y, Color.WHITE.getRGB());
                    }
                }
            }
        }

        //here we can adjust which hour/minute we want, we need to adjust 1 quarter of a turn because it starts at 3
        double hour = 2*hourStep - Math.PI / 2;
        double minute = 20*minuteStep - Math.PI / 2;

        int minuteMarkerCircleRadius = (int) Math.round(markersCircleRadius*0.8);
        int hourMarkerCircleRadius = (int) Math.round(minuteMarkerCircleRadius*0.7);


        //calculate where the (x,y) values are for each of the markers so i can do linear interpolation later
        int posMarkerHoursX = centerX + (int)(hourMarkerCircleRadius * Math.cos(hour));
        int posMarkerHoursY = centerY + (int)(hourMarkerCircleRadius * Math.sin(hour));
        int posMarkerMinutesX = centerX + (int)(minuteMarkerCircleRadius * Math.cos(minute));
        int posMarkerMinutesY = centerY + (int)(minuteMarkerCircleRadius * Math.sin(minute));

        //amount of times well make linear interpolation
        int steps = hourMarkerCircleRadius;

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            //minutes
            int x = (int)(centerX + t * (posMarkerMinutesX - centerX));
            int y = (int)(centerY + t * (posMarkerMinutesY - centerY));
            image.setRGB(x, y, Color.WHITE.getRGB());
            x = (int)(centerX + t * (posMarkerHoursX - centerX));
            y = (int)(centerY + t * (posMarkerHoursY - centerY));
            image.setRGB(x, y, Color.WHITE.getRGB());

        }

        File outputImage = new File("final.jpg");
        try{
            ImageIO.write(image,"jpg", outputImage);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
