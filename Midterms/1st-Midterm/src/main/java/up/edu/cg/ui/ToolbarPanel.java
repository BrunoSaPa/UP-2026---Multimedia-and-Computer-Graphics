package up.edu.cg.ui;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Separator;

public class ToolbarPanel extends ToolBar {

    private final Button cropButton;
    private final Button invertButton;
    private final Button rotate90Button;
    private final Button rotate180Button;
    private final Button rotate270Button;
    private final Button clearSelectionButton;
    private final Button undoButton;

    public ToolbarPanel() {

        cropButton= new Button("Crop");
        invertButton = new Button("Invert");
        rotate90Button = new Button("Rotate 90°");
        rotate180Button= new Button("Rotate 180°");
        rotate270Button = new Button("Rotate 270°");
        clearSelectionButton = new Button("Clear Selection");
        undoButton = new Button("Undo");

        // buttons that require a selection or image start disabled, since most of the operations require a selected regin, this will be handled in the controller
        cropButton.setDisable(true);
        rotate90Button.setDisable(true);
        rotate180Button.setDisable(true);
        rotate270Button.setDisable(true);
        invertButton.setDisable(true);
        undoButton.setDisable(true);

        getItems().addAll(undoButton, new Separator(), invertButton, new Separator(), cropButton, new Separator(), rotate90Button, rotate180Button, rotate270Button, new Separator(), clearSelectionButton
        );
    }

    public Button getCropButton(){ return cropButton; }
    public Button getInvertButton(){ return invertButton; }
    public Button getRotate90Button(){ return rotate90Button; }
    public Button getRotate180Button(){ return rotate180Button; }
    public Button getRotate270Button(){ return rotate270Button; }
    public Button getClearSelectionButton(){ return clearSelectionButton; }
    public Button getUndoButton(){ return undoButton; }
}