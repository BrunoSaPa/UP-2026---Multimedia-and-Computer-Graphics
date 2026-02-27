package up.edu.cg.service;

import up.edu.cg.model.ImageModel;
import up.edu.cg.operations.ImageOperation;

public class ImageProcessor {

    //here i hold the image state
    private ImageModel currentImage;

    public ImageModel applyOperation(ImageOperation op) {
        currentImage = op.apply(currentImage);
        return currentImage;
    }

    public ImageModel getCurrentImage() {
        return currentImage;
    }

    public void setCurrentImage(ImageModel image) {
        this.currentImage = image;
    }
}