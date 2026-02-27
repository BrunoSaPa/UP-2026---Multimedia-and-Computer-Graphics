package up.edu.cg.operations;

import up.edu.cg.model.ImageModel;
import up.edu.cg.model.ImageRegion;
import up.edu.cg.model.Pixel;

public class RotateOperation implements ImageOperation {

    private final ImageRegion region;
    private final int degrees;

    //constructor that checks if the arguments are correct before proceding creating the object itself, since i can only manage 90,180, and 270 flips
    public RotateOperation(ImageRegion region, int degrees) {
        if (degrees != 90 && degrees != 180 && degrees != 270)
            throw new IllegalArgumentException("Degrees must be 90, 180, or 270.");

        this.region = region;
        this.degrees = degrees;
    }

    @Override
    public ImageModel apply(ImageModel image) {
        ImageModel result = image.copy();


        int startX = region.getX1();
        int startY = region.getY1();
        int width  = region.getWidth();
        int height = region.getHeight();

        //copy original region into a temp
        Pixel[][] buffer = new Pixel[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                buffer[y][x] = result.getPixel(startX + x, startY + y).copy();

        //Rotate the buffer in memory as many times as needed
        int times = degrees / 90;
        for (int i = 0; i < times; i++)
            buffer = rotate90(buffer);

        // current dimensions after all rotations
        int newHeight = buffer.length;
        int newWidth  = buffer[0].length;

        //compute new top-left to preserve center, having in mind the center of the region aswell so i can position the new area in the correct place
        double centerX = startX + width  / 2.0;
        double centerY = startY + height / 2.0;
        int newStartX = (int) Math.round(centerX - newWidth  / 2.0);
        int newStartY = (int) Math.round(centerY - newHeight / 2.0);

        //fill original area with white
        Pixel white = new Pixel(255, 255, 255);
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                result.setPixel(startX + x, startY + y, white.copy());

        //paste final buffer into the image
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int targetX = newStartX + x;
                int targetY = newStartY + y;

                if (targetX >= 0 && targetX < result.getWidth() &&
                        targetY >= 0 && targetY < result.getHeight()) {
                    result.setPixel(targetX, targetY, buffer[y][x]);
                }
            }
        }

        return result;
    }

    private Pixel[][] rotate90(Pixel[][] src) {
        int height = src.length;
        int width  = src[0].length;

        // i use [width][height] to account for rectangles since it is rotated, height is now width and viceversa
        Pixel[][] rotated = new Pixel[width][height];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                //take the source pixel and place it in its rotated position eg; the [0][0] of the original is now [0][height-1] and it is in the rotated corner that it should be
                rotated[x][height - 1 - y] = src[y][x];

        return rotated;
    }
}