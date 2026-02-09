package up.edu.cg.compression;
import up.edu.cg.core.*;
import up.edu.cg.clustering.KMeans;
import up.edu.cg.utils.Distance;

public class VectorQuantizer {

    //the codebook is what we will use as the lookup for every block in the image, so we now where to clasify each block
    private Block[] codebook;


    //this is just to get hte best representative blocks for an specific image which are my centroids
    public void train(Block[] blocks, int codebookSize) {
        System.out.println("training started");
        System.out.println("codebook size: " + codebookSize);
        codebook = KMeans.run(blocks, codebookSize, 10);
        System.out.println("training finished");
    }

    //sice i am using byte the biggest codebook size is 255
    public byte[] encode(Block[] blocks) {
        byte[] indices = new byte[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            indices[i] = (byte) findClosest(blocks[i]);
        }
        return indices;
    }

    private int findClosest(Block b) {
        int best = 0;
        double bestDist = Double.MAX_VALUE;

        for (int i = 0; i < codebook.length; i++) {
            double d = Distance.l1(b, codebook[i]);
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return best;
    }

    public Block[] getCodebook() {
        return codebook;
    }
}
