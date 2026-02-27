package up.edu.cg.operations;

import up.edu.cg.model.ImageModel;
import up.edu.cg.model.ImageRegion;
import up.edu.cg.model.Pixel;

public class CropOperation implements ImageOperation {

    private final ImageRegion region;

    public CropOperation(ImageRegion region) {
        this.region = region;
    }

    @Override
    public ImageModel apply(ImageModel image) {

        //we are simply taking the current image, and creating a new one with the region given, and returning that cropped new image
        int width = region.getWidth();
        int height = region.getHeight();

        ImageModel result = new ImageModel(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Pixel p = image.getPixel(region.getX1() + x, region.getY1() + y);
                result.setPixel(x, y, p.copy());
            }
        }

        return result;
    }
}