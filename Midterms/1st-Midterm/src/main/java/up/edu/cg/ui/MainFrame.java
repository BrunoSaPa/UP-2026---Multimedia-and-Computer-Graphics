package up.edu.cg.ui;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class MainFrame extends BorderPane {

    private final ImagePanel imagePanel;
    private final ToolbarPanel toolbarPanel;

    //items to upload and save images
    private final MenuItem openItem;
    private final MenuItem saveItem;

    public MainFrame() {

        imagePanel = new ImagePanel();
        toolbarPanel = new ToolbarPanel();

        //menu
        Menu fileMenu = new Menu("File");

        openItem = new MenuItem("Open PNG");
        saveItem = new MenuItem("Save PNG");

        fileMenu.getItems().addAll(openItem, saveItem);

        MenuBar menuBar = new MenuBar(fileMenu);

        //layout
        VBox topContainer = new VBox(menuBar, toolbarPanel);

        setTop(topContainer);
        setCenter(imagePanel);
    }


    public ImagePanel getImagePanel() {
        return imagePanel;
    }

    public ToolbarPanel getToolbarPanel() {
        return toolbarPanel;
    }

    public MenuItem getOpenItem() {
        return openItem;
    }

    public MenuItem getSaveItem() {
        return saveItem;
    }
}