package up.edu.cg.operations;

import up.edu.cg.model.ImageModel;
import up.edu.cg.model.ImageRegion;

public class InvertOperation implements ImageOperation {

    private final ImageRegion region;

    // Whole image
    public InvertOperation() {
        this.region = null;
    }

    // Selected region only
    public InvertOperation(ImageRegion region) {
        this.region = region;
    }


    //this apply method is a little different since i will have to check wheter an area is selected or not, so i apply the invert to the whole image or just the region, either way the operation is the same, just the area is different
    @Override
    public ImageModel apply(ImageModel image) {
        ImageModel result = image.copy();

        if (region == null) {
            // invert everything
            for (int y = 0; y < result.getHeight(); y++)
                for (int x = 0; x < result.getWidth(); x++)
                    result.getPixel(x, y).invert();
        } else {
            // invert only within the region
            for (int y = region.getY1(); y < region.getY2(); y++)
                for (int x = region.getX1(); x < region.getX2(); x++)
                    result.getPixel(x, y).invert();
        }

        return result;
    }
}