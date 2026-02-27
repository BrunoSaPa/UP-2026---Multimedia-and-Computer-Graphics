package up.edu.cg.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;

import up.edu.cg.model.ImageModel;
import up.edu.cg.model.ImageRegion;
import up.edu.cg.model.Pixel;
import up.edu.cg.model.Point;

import java.util.Optional;

public class ImagePanel extends StackPane {

    private final Canvas canvas;
    private final GraphicsContext gc;

    private ImageModel image;
    private Runnable selectionChangedListener;

    private double startX, startY, endX, endY;
    private boolean hasSelection = false;
    private double scale = 1.0;

    public ImagePanel() {
        canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
        initializeMouseHandlers();
    }

    private void initializeMouseHandlers() {


        //events for the 3 operation needed, when the mouse is pressed (start of the region selected)
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            startX = e.getX() / scale;
            startY = e.getY() / scale;
            endX = startX;
            endY = startY;
            hasSelection = false;
            redraw();
        });

        //when the mouse is dragged (defines the area of the region selected)
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            endX = e.getX() / scale;
            endY = e.getY() / scale;
            redraw();
        });

        //when the mouse is released (defines the second point of the region selected)
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            endX = e.getX() / scale;
            endY = e.getY() / scale;
            hasSelection = Math.abs(endX - startX) > 1 && Math.abs(endY - startY) > 1;
            redraw();
            notifySelectionChanged();
        });
    }


    //we get an image and resize it according to its size with the scale factor
    public void setImage(ImageModel image) {
        this.image = image;
        clearSelection();

        double scaleX = getWidth()  / image.getWidth();
        double scaleY = getHeight() / image.getHeight();
        scale = Math.min(1.0, Math.min(scaleX, scaleY));

        canvas.setWidth(image.getWidth()  * scale);
        canvas.setHeight(image.getHeight() * scale);

        redraw();
    }

    //region selected, its optional because invert operation can be made with a region selected or not, and its needed to enable/disable buttons so operations can be made only when they can be made, more here https://www.arquitecturajava.com/que-es-un-java-optional/
    public Optional<ImageRegion> getSelection() {
        if (!hasSelection) return Optional.empty();

        return Optional.of(new ImageRegion(
                new Point((int) Math.min(startX, endX), (int) Math.min(startY, endY)),
                new Point((int) Math.max(startX, endX), (int) Math.max(startY, endY))
        ));
    }

    public boolean hasSelection() {
        return hasSelection;
    }

    public void clearSelection() {
        hasSelection = false;
        startX = startY = endX = endY = 0;
        redraw();
    }


    //i needed a way to inform the controller when the region has changed, so i needed a function to notify it
    public void setOnSelectionChanged(Runnable listener) {
        this.selectionChangedListener = listener;
    }

    //super important method, i get the current image(modified or not) and redraw the canvas with it
    public void redraw() {
        if (image == null) return;

        WritableImage writableImage = new WritableImage(image.getWidth(), image.getHeight());
        PixelWriter writer = writableImage.getPixelWriter();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Pixel p = image.getPixel(x, y);
                writer.setColor(x, y, Color.rgb(p.getRed(), p.getGreen(), p.getBlue()));
            }
        }

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.save();
        gc.scale(scale, scale);
        gc.drawImage(writableImage, 0, 0);
        gc.restore();

        drawSelection();
    }


//methods used inside this class

    //the purpose for this method is to draw the rectangle of the area selected (only if it exist of course)
    private void drawSelection() {
        if (!hasSelection && !(Math.abs(endX - startX) > 1)) return;

        double x = Math.min(startX, endX) * scale;
        double y = Math.min(startY, endY) * scale;
        double width  = Math.abs(endX - startX) * scale;
        double height = Math.abs(endY - startY) * scale;

        //here we define the parameters for the selection area
        gc.setStroke(Color.RED);
        gc.setLineDashes(6);
        gc.setLineWidth(1.5);
        gc.strokeRect(x, y, width, height);
        gc.setLineDashes(0);
    }

    private void notifySelectionChanged() {
        if (selectionChangedListener != null)
            selectionChangedListener.run();
    }


}