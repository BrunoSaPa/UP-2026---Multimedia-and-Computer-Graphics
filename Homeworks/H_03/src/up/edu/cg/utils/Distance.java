package up.edu.cg.utils;
import up.edu.cg.core.*;

public class Distance {

    public static double l1(Block a, Block b) {
        double sum = 0;
        //define the two points a amd b
        Pixel[] pa = a.getData();
        Pixel[] pb = b.getData();

        //i am using l1 distance or manhattan, i have more info where i sourced this in the readme
        for (int i = 0; i < pa.length; i++) {
            sum += Math.abs(pa[i].r - pb[i].r);
            sum += Math.abs(pa[i].g - pb[i].g);
            sum += Math.abs(pa[i].b - pb[i].b);
        }
        return sum;
    }
}