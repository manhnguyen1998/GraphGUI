package application;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class RightClickMenu {
    ContextMenu menu;
    SceneController.NodeFX sourceNode;
    Edge sourceEdge;
    MenuItem delete, changeId;

    public RightClickMenu() {
        menu = new ContextMenu();
        delete = new MenuItem("Delete");
        changeId = new MenuItem("Change ID");

        Image openIcon = new Image(getClass().getResourceAsStream("/delete_img.png"));
        ImageView openView = new ImageView(openIcon);
        delete.setGraphic(openView);

        Image textIcon = new Image(getClass().getResourceAsStream("/rename_img.png"));
        ImageView textIconView = new ImageView(textIcon);
        changeId.setGraphic(textIconView);

        menu.getItems().addAll(delete, changeId);
        menu.setOpacity(0.9);
    }

    /**
     * Constructor for the context menu on node
     *
     * @param node
     */
    public RightClickMenu(SceneController.NodeFX node) {
        this();
        sourceNode = node;
        delete.setOnAction(e -> {
            Panel1Controller.cref.deleteNode(sourceNode);
        });
        changeId.setOnAction(e -> {
            Panel1Controller.cref.changeID(node);
        });
    }

    /**
     * Constructor for the context menu on edge
     *
     * @param edge
     */
    public RightClickMenu(Edge edge) {
        this();
        sourceEdge = edge;
        delete.setOnAction(e -> {
            Panel1Controller.cref.deleteEdge(sourceEdge);
        });
        changeId.setOnAction(e -> {
            Panel1Controller.cref.changeWeight(sourceEdge);
        });
    }

    public ContextMenu getMenu() {
        return menu;
    }
}
