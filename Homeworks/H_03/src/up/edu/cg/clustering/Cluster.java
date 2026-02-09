package up.edu.cg.clustering;
import up.edu.cg.core.*;
import java.util.ArrayList;
import java.util.List;

public class Cluster {
    //a centroid is just a block which is the mean of the members in that cluster
    public Block centroid;
    public List<Block> members = new ArrayList<>();
}