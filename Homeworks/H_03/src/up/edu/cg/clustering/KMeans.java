package up.edu.cg.clustering;
import up.edu.cg.core.*;
import up.edu.cg.utils.*;
import java.util.*;

public class KMeans {

    public static Block[] run(Block[] data, int k, int iterations) {

        //added this so i know what is happening in runtime
        System.out.println("starting clustering");
        System.out.println("blocks: " + data.length);
        System.out.println("clusters (k): " + k);
        System.out.println("iterations: " + iterations);
        System.out.println();
        Random rand = new Random();

        //init clusters
        List<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            Cluster c = new Cluster();
            c.centroid = data[rand.nextInt(data.length)];
            clusters.add(c);
        }

        for (int it = 0; it < iterations; it++) {

            //clear members from previous iteration
            for (Cluster c : clusters) {
                c.members.clear();
            }

            //assign each block to a cluster
            for (Block b : data) {
                Cluster bestCluster = null;
                double bestDist = Double.MAX_VALUE;

                for (Cluster c : clusters) {
                    double d = Distance.l1(b, c.centroid);
                    if (d < bestDist) {
                        bestDist = d;
                        bestCluster = c;
                    }
                }
                bestCluster.members.add(b);
            }

            //uodate
            //this is just to keep track of empty clusters in runtime
            int emptyClusters = 0;
            for (Cluster c : clusters) {
                if (!c.members.isEmpty()) {
                    c.centroid = average(c.members);
                }else{
                    emptyClusters++;
                }
            }
            System.out.println("Iteration #" + (it+1) +" of " + iterations);
            System.out.print("empty clusters: " + emptyClusters + "\n");
        }


        System.out.println("finished clustering");
        //get centroids
        Block[] centroids = new Block[k];
        for (int i = 0; i < k; i++) {
            centroids[i] = clusters.get(i).centroid;
        }

        return centroids;
    }


    private static Block average(List<Block> blocks) {
        if (blocks.isEmpty()) return null;

        int len = blocks.get(0).length();
        int[] r = new int[len];
        int[] g = new int[len];
        int[] b = new int[len];

        for (Block block : blocks) {
            for (int i = 0; i < len; i++) {
                r[i] += block.getData()[i].r;
                g[i] += block.getData()[i].g;
                b[i] += block.getData()[i].b;
            }
        }

        Pixel[] avg = new Pixel[len];
        for (int i = 0; i < len; i++) {
            avg[i] = new Pixel(
                    r[i] / blocks.size(),
                    g[i] / blocks.size(),
                    b[i] / blocks.size()
            );
        }

        return new Block(avg);
    }
}
