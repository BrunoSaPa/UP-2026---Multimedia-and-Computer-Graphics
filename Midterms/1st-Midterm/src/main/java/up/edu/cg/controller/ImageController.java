package up.edu.cg.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import up.edu.cg.model.ImageModel;
import up.edu.cg.model.ImageRegion;
import up.edu.cg.model.Pixel;
import up.edu.cg.operations.CropOperation;
import up.edu.cg.operations.ImageOperation;
import up.edu.cg.operations.InvertOperation;
import up.edu.cg.operations.RotateOperation;
import up.edu.cg.service.ImageHistory;
import up.edu.cg.service.ImageProcessor;
import up.edu.cg.ui.MainFrame;
import up.edu.cg.ui.ToolbarPanel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ImageController {

    private final MainFrame mainFrame;
    //image processor is used to store the current state of the image
    private final ImageProcessor processor;
    private final ImageHistory history;

    public ImageController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.processor = new ImageProcessor();
        this.history = new ImageHistory();
        initializeHandlers();
    }

    private void initializeHandlers() {
        mainFrame.getOpenItem().setOnAction(e -> handleOpen());
        mainFrame.getSaveItem().setOnAction(e -> handleSave());
        mainFrame.getImagePanel().setOnSelectionChanged(this::updateToolbarState);

        ToolbarPanel toolbar = mainFrame.getToolbarPanel();
        toolbar.getCropButton().setOnAction(e -> handleCrop());
        toolbar.getInvertButton().setOnAction(e -> handleInvert());
        toolbar.getRotate90Button().setOnAction(e -> handleRotate(90));
        toolbar.getRotate180Button().setOnAction(e -> handleRotate(180));
        toolbar.getRotate270Button().setOnAction(e -> handleRotate(270));
        toolbar.getClearSelectionButton().setOnAction(e -> clearSelection());
        toolbar.getUndoButton().setOnAction(e -> handleUndo());
    }


    //open files
    private void handleOpen() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        File file = chooser.showOpenDialog(getStage());
        if (file == null) return;

        Image fxImage = new Image(file.toURI().toString());
        ImageModel model = convertToModel(fxImage);

        processor.setCurrentImage(model);
        history.clear();

        refreshView();
    }

    //gets the image from the user and converts it to my model so i can work with it
    private ImageModel convertToModel(Image fxImage) {
        int width = (int) fxImage.getWidth();
        int height = (int) fxImage.getHeight();

        ImageModel model = new ImageModel(width, height);
        PixelReader reader = fxImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                javafx.scene.paint.Color c = reader.getColor(x, y);
                model.setPixel(x, y, new Pixel(
                        (int) (c.getRed()   * 255),
                        (int) (c.getGreen() * 255),
                        (int) (c.getBlue()  * 255)
                ));
            }
        }
        return model;
    }



    //save images
    private void handleSave() {
        if (processor.getCurrentImage() == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );

        File file = fileChooser.showSaveDialog(getStage());
        if (file == null) return;

        try {
            ImageModel image = processor.getCurrentImage();
            WritableImage writableImage = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter writer = writableImage.getPixelWriter();

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    Pixel p = image.getPixel(x, y);
                    writer.setColor(x, y, Color.rgb(p.getRed(), p.getGreen(), p.getBlue()));
                }
            }

            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
            ImageIO.write(bufferedImage, "png", file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //here is where i have all my operations, operation logic is in the operations package
    private void handleInvert() {
        if (processor.getCurrentImage() == null) return;
        pushHistory();

        Optional<ImageRegion> selection = mainFrame.getImagePanel().getSelection();
        //since invert operation can be done with or whithout an area selected, i check if a region is selected first, then i know how to send the parameters correctly
        ImageOperation op = selection.isPresent() ? new InvertOperation(selection.get()) : new InvertOperation();
        processor.applyOperation(op);
        refreshView();
    }
    private void handleRotate(int degrees) {
        if (processor.getCurrentImage() == null) return;

        Optional<ImageRegion> selection = mainFrame.getImagePanel().getSelection();
        if (selection.isEmpty()) return;

        pushHistory();
        processor.applyOperation(new RotateOperation(selection.get(), degrees));
        refreshView();
    }

    private void handleCrop() {
        if (processor.getCurrentImage() == null) return;

        Optional<ImageRegion> selection = mainFrame.getImagePanel().getSelection();
        if (selection.isEmpty()) return;

        pushHistory();
        processor.applyOperation(new CropOperation(selection.get()));
        refreshView();
    }

    private void handleUndo() {
        ImageModel previous = history.undo();
        if (previous == null) return;

        processor.setCurrentImage(previous);
        refreshView();
    }

    private void clearSelection() {
        mainFrame.getImagePanel().clearSelection();
        updateToolbarState();
    }



    //private methods
    private void pushHistory() {
        history.push(processor.getCurrentImage().copy());
    }

    //function to update the view after a change, and the toolbar based in the selected region
    private void refreshView() {
        mainFrame.getImagePanel().setImage(processor.getCurrentImage());
        updateToolbarState();
    }

    //some operations cant happen if a region isnt selected
    private void updateToolbarState() {
        ToolbarPanel toolbar = mainFrame.getToolbarPanel();
        boolean hasImage = processor.getCurrentImage() != null;
        boolean hasSelection = mainFrame.getImagePanel().hasSelection();

        toolbar.getCropButton().setDisable(!hasSelection);
        toolbar.getRotate90Button().setDisable(!hasSelection);
        toolbar.getRotate180Button().setDisable(!hasSelection);
        toolbar.getRotate270Button().setDisable(!hasSelection);
        toolbar.getInvertButton().setDisable(!hasImage);
        toolbar.getUndoButton().setDisable(!history.canUndo());
    }

    private Stage getStage() {
        return (Stage) mainFrame.getScene().getWindow();
    }
}