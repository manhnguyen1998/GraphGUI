package application;

import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.awt.Point;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SceneController implements Initializable {
    @FXML
    private Pane side_pane;

    @FXML
    private Line edgeLine;
    @FXML
    private Arrow arrow;
    @FXML
    private ToggleButton dfs_button, bfs_button, dijistra_button, add_node_button, add_edge_button;
    @FXML
    private Label weight;

    @FXML
    private Button clear_button, backButton;
    @FXML
    private ImageView start_button;
    @FXML
    private Pane canvasGroup;
    @FXML
    private NodeFX nodeList;
    private boolean weighted = Panel1Controller.weighted, unweighted = Panel1Controller.unweighted,
            directed = Panel1Controller.directed, undirected = Panel1Controller.undirected,
            bfs = true, dfs = true, dijkstra = true, articulationPoint = true, mst = true, topSortBool = true;

    NodeFX selectedNode = null;
    ContextMenu globalMenu;
    ToggleGroup toggleGroup = new ToggleGroup();
    List<NodeFX> circles = new ArrayList<>();
    List<Shape> edges = new ArrayList<>();
    List<Edge> mstEdges = new ArrayList<>(), realEdges = new ArrayList<>();
    boolean menuBool = false,calculate=false,calculated=false;
    int nNode = 0;
    Boolean addNode = true, addEdge = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        backButton.setOnAction(e -> {
            try {
//                ResetHandle(null);
                Parent root = FXMLLoader.load(getClass().getResource("ex.fxml"));

                Scene scene = new Scene(root);
                Main.primaryStage.setScene(scene);
            } catch (IOException ex) {
                Logger.getLogger(SceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        Line line = new Line();
        line.setStartX(100.0f);
        line.setStartY(200.0f);
        line.setEndX(300.0f);
        line.setEndY(70.0f);
        line.setStrokeWidth(10);
        line.setStroke(Color.PINK);
        canvasGroup.getChildren().add(line);

    }

    @FXML
    public void handle(MouseEvent mouseEvent) {
//        System.out.println("mouse click detected! " + mouseEvent.getSource());
        if (addNode) {
            if (nNode == 1) {
                add_node_button.setDisable(false);
            }
            if (nNode == 2) {
                add_edge_button.setDisable(false);
                AddNodeHandle(null);
            }
            if (!mouseEvent.getSource().equals(canvasGroup)) {
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED && mouseEvent.getButton() == MouseButton.PRIMARY) {
                    if (menuBool == true) {
                        System.out.println("here" + mouseEvent.getEventType());
                        menuBool = false;
                        return;
                    }
                    nNode++;
                    NodeFX circle = new NodeFX(mouseEvent.getX(), mouseEvent.getY(), 1.2, String.valueOf(nNode));

                    canvasGroup.getChildren().add(circle);
                    circle.setOnMousePressed(mouseHandler);
                    circle.setOnMouseReleased(mouseHandler);
                    circle.setOnMouseDragged(mouseHandler);
                    circle.setOnMouseExited(mouseHandler);
                    circle.setOnMouseEntered(mouseHandler);
                    ScaleTransition tr = new ScaleTransition(Duration.millis(100), circle);
                    tr.setByX(10f);
                    tr.setByY(10f);
                    tr.setInterpolator(Interpolator.EASE_OUT);
                    tr.play();
                }
            }
        }
    }

    boolean edgeExists(NodeFX u, NodeFX v) {
        for (Edge e : realEdges) {
            if (e.source == u.node && e.target == v.node) {
                return true;
            }
        }
        return false;
    }

    EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {

                NodeFX circle = (NodeFX) mouseEvent.getSource();
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED && mouseEvent.getButton() == MouseButton.PRIMARY) {

                    if (!circle.isSelected) {
                        if (selectedNode != null) {
                            if (addEdge && !edgeExists(selectedNode, circle)) {
                                weight = new Label();
                                System.out.println("Adding Edge");
                                //Adds the edge between two selected nodes
                                if (undirected) {
                                    edgeLine = new Line(selectedNode.point.x, selectedNode.point.y, circle.point.x, circle.point.y);
                                    canvasGroup.getChildren().add(edgeLine);
                                    edgeLine.setId("line");
                                } else if (directed) {
                                    arrow = new Arrow(selectedNode.point.x, selectedNode.point.y, circle.point.x, circle.point.y);
                                    canvasGroup.getChildren().add(arrow);
                                    arrow.setId("arrow");
                                }

                                //Adds weight between two selected nodes
                                if (weighted) {
                                    weight.setLayoutX(((selectedNode.point.x) + (circle.point.x)) / 2);
                                    weight.setLayoutY(((selectedNode.point.y) + (circle.point.y)) / 2);

                                    TextInputDialog dialog = new TextInputDialog("0");
                                    dialog.setTitle(null);
                                    dialog.setHeaderText("Enter Weight of the Edge :");
                                    dialog.setContentText(null);

                                    Optional<String> result = dialog.showAndWait();
                                    if (result.isPresent()) {
                                        weight.setText(result.get());
                                    } else {
                                        weight.setText("0");
                                    }
                                    canvasGroup.getChildren().add(weight);
                                } else if (unweighted) {
                                    weight.setText("1");
                                }
                                Shape line_arrow = null;
                                Edge temp = null;
                                if (undirected) {
                                    temp = new Edge(selectedNode.node, circle.node, Integer.valueOf(weight.getText()), edgeLine, weight);
                                    if (weighted) {
                                        mstEdges.add(temp);
                                    }

                                    selectedNode.node.adjacents.add(new Edge(selectedNode.node, circle.node, Double.valueOf(weight.getText()), edgeLine, weight));
                                    circle.node.adjacents.add(new Edge(circle.node, selectedNode.node, Double.valueOf(weight.getText()), edgeLine, weight));
                                    edges.add(edgeLine);
                                    realEdges.add(selectedNode.node.adjacents.get(selectedNode.node.adjacents.size() - 1));
                                    realEdges.add(circle.node.adjacents.get(circle.node.adjacents.size() - 1));
                                    line_arrow = edgeLine;

                                } else if (directed) {
                                    temp = new Edge(selectedNode.node, circle.node, Double.valueOf(weight.getText()), arrow, weight);
                                    selectedNode.node.adjacents.add(temp);
                                    edges.add(arrow);
                                    line_arrow = arrow;
                                    realEdges.add(temp);
                                }

//                                RightClickMenu rt = new RightClickMenu(temp);
//                                ContextMenu menu = rt.getMenu();
//                                if (weighted) {
//                                    rt.changeId.setText("Change Weight");
//                                } else if (unweighted) {
//                                    rt.changeId.setDisable(true);
//                                }
//                                final Shape la = line_arrow;
//                                line_arrow.setOnContextMenuRequested(e -> {
//                                    System.out.println("In Edge Menu :" + menuBool);
//
//                                    if (menuBool == true) {
//                                        globalMenu.hide();
//                                        menuBool = false;
//                                    }
//                                    if (addEdge || addNode) {
//                                        globalMenu = menu;
//                                        menu.show(la, e.getScreenX(), e.getScreenY());
//                                        menuBool = true;
//                                    }
//                                });
//                                menu.setOnAction(e -> {
//                                    menuBool = false;
//                                });
                            }
                            if (addNode || (calculate && !calculated) || addEdge) {
                                selectedNode.isSelected = false;
                                FillTransition ft1 = new FillTransition(Duration.millis(300), selectedNode, Color.RED, Color.BLACK);
                                ft1.play();
                            }
                            selectedNode = null;
                            return;
                        }

                        FillTransition ft = new FillTransition(Duration.millis(300), circle, Color.BLACK, Color.RED);
                        ft.play();
                        circle.isSelected = true;
                        selectedNode = circle;

                        // WHAT TO DO WHEN SELECTED ON ACTIVE ALGORITHM
//                        if (calculate && !calculated) {
//                            if (bfs) {
//                                algo.newBFS(circle.node);
//                            } else if (dfs) {
//                                algo.newDFS(circle.node);
//                            } else if (dijkstra) {
//                                algo.newDijkstra(circle.node);
//                            }
//
//                            calculated = true;
//                        } else if (calculate && calculated && !articulationPoint & !mst && !topSortBool) {
//
//                            for (NodeFX n : circles) {
//                                n.isSelected = false;
//                                FillTransition ft1 = new FillTransition(Duration.millis(300), n);
//                                ft1.setToValue(Color.BLACK);
//                                ft1.play();
//                            }
//                            List<Node> path = algo.getShortestPathTo(circle.node);
//                            for (Node n : path) {
//                                FillTransition ft1 = new FillTransition(Duration.millis(300), n.circle);
//                                ft1.setToValue(Color.BLUE);
//                                ft1.play();
//                            }
//                        }
                    } else {
                        circle.isSelected = false;
                        FillTransition ft1 = new FillTransition(Duration.millis(300), circle, Color.RED, Color.BLACK);
                        ft1.play();
                        selectedNode = null;
                    }

                }
            }

        };


    @FXML
    public void AddNodeHandle(javafx.event.ActionEvent event) {
        addNode = true;
        addEdge = false;
        add_node_button.setSelected(true);
        add_edge_button.setSelected(false);
        selectedNode = null;
        if (unweighted) {
            bfs_button.setDisable(false);
            bfs_button.setSelected(false);
            dfs_button.setDisable(false);
            dfs_button.setSelected(false);
        }
        if (weighted) {
            dijistra_button.setDisable(false);
            dijistra_button.setSelected(false);
        }
    }

    @FXML
    public void AddEdgeHandle(ActionEvent event) {
        addNode = false;
        addEdge = true;
        add_node_button.setSelected(false);
        add_edge_button.setSelected(true);
        if (unweighted) {
            bfs_button.setDisable(false);
            bfs_button.setSelected(false);
            dfs_button.setDisable(false);
            dfs_button.setSelected(false);
        }
        if (weighted) {
            dijistra_button.setDisable(false);
            dijistra_button.setSelected(false);
        }
    }

    public void deleteNode(NodeFX sourceFX) {
        selectedNode = null;
        System.out.println("Before--------");
        for (NodeFX u : circles) {
            System.out.println(u.node.name + " - ");
            for (Edge v : u.node.adjacents) {
                System.out.println(v.source.name + " " + v.target.name);
            }
        }
        Node source = sourceFX.node;
        circles.remove(sourceFX);
        List<Edge> tempEdges = new ArrayList<Edge>();
        List<Node> tempNodes = new ArrayList<Node>();
        for (Edge e : source.adjacents) {
            Node u = e.target;
            for (Edge x : u.adjacents) {
                if (x.target == source) {
                    x.target = null;
                    tempNodes.add(u);
                    tempEdges.add(x);
                }
            }
            edges.remove(e.getLine());
            canvasGroup.getChildren().remove(e.getLine());
            mstEdges.remove(e);
        }
        for (Node q : tempNodes) {
            q.adjacents.removeAll(tempEdges);
        }
        List<Edge> tempEdges2 = new ArrayList<>();
        List<Shape> tempArrows = new ArrayList<>();
        List<Node> tempNodes2 = new ArrayList<>();
        for (NodeFX z : circles) {
            for (Edge s : z.node.adjacents) {
                if (s.target == source) {
                    tempEdges2.add(s);
                    tempArrows.add(s.line);
                    tempNodes2.add(z.node);
                    canvasGroup.getChildren().remove(s.line);
                }
            }
        }
        for (Node z : tempNodes2) {
            z.adjacents.removeAll(tempEdges2);
        }
        realEdges.removeAll(tempEdges);
        realEdges.removeAll(tempEdges2);
        canvasGroup.getChildren().remove(sourceFX.id);
        canvasGroup.getChildren().remove(sourceFX);
        System.out.println("AFTER----------");
        for (NodeFX u : circles) {
            System.out.println(u.node.name + " - ");
            for (Edge v : u.node.adjacents) {
                System.out.println(v.source.name + " " + v.target.name);
            }
        }
    }

    public void changeID(NodeFX source) {
        System.out.println("Before-------");
        for (NodeFX u : circles) {
            System.out.println(u.node.name + " - ");
            for (Edge v : u.node.adjacents) {
                System.out.println(v.source.name + " " + v.target.name);
            }
        }
        selectedNode = null;

        TextInputDialog dialog = new TextInputDialog(Integer.toString(nNode));
        dialog.setTitle(null);
        dialog.setHeaderText("Enter Node ID :");
        dialog.setContentText(null);

        String res = null;
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            res = result.get();
        }

        circles.get(circles.indexOf(source)).id.setText(res);
        circles.get(circles.indexOf(source)).node.name = res;

        System.out.println("AFTER----------");
        for (NodeFX u : circles) {
            System.out.println(u.node.name + " - ");
            for (Edge v : u.node.adjacents) {
                System.out.println(v.source.name + " " + v.target.name);
            }
        }
    }

    public void deleteEdge(Edge sourceEdge) {
        System.out.println("Before-------");
        for (NodeFX u : circles) {
            System.out.println(u.node.name + " - ");
            for (Edge v : u.node.adjacents) {
                System.out.println(v.source.name + " " + v.target.name);
            }
        }

        System.out.println(sourceEdge.source.name + " -- " + sourceEdge.target.name);
        List<Edge> ls1 = new ArrayList<>();
        List<Shape> lshape2 = new ArrayList<>();
        for (Edge e : sourceEdge.source.adjacents) {
            if (e.target == sourceEdge.target) {
                ls1.add(e);
                lshape2.add(e.line);
            }
        }
        for (Edge e : sourceEdge.target.adjacents) {
            if (e.target == sourceEdge.source) {
                ls1.add(e);
                lshape2.add(e.line);
            }
        }
        System.out.println("sdsdsd  " + ls1.size());
        sourceEdge.source.adjacents.removeAll(ls1);
        sourceEdge.target.adjacents.removeAll(ls1);
        realEdges.removeAll(ls1);

        edges.removeAll(lshape2);
        canvasGroup.getChildren().removeAll(lshape2);

        System.out.println("AFTER----------");
        for (NodeFX p : circles) {
            System.out.println(p.node.name + " - ");
            for (Edge q : p.node.adjacents) {
                System.out.println(q.source.name + " " + q.target.name);
            }
        }
    }

    public void changeWeight(Edge sourceEdge) {
        System.out.println("Before-------");
        for (NodeFX u : circles) {
            System.out.println(u.node.name + " - ");
            for (Edge v : u.node.adjacents) {
                System.out.println(v.source.name + " " + v.target.name + " weight: " + v.weight);
            }
        }

        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle(null);
        dialog.setHeaderText("Enter Weight of the Edge :");
        dialog.setContentText(null);

        String res = null;
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            res = result.get();
        }

        for (Edge e : sourceEdge.source.adjacents) {
            if (e.target == sourceEdge.target) {
                e.weight = Double.valueOf(res);
                e.weightLabel.setText(res);
            }
        }
        for (Edge e : sourceEdge.target.adjacents) {
            if (e.target == sourceEdge.source) {
                e.weight = Double.valueOf(res);
            }
        }
        for (Edge e : mstEdges) {
            if (e.source == sourceEdge.source && e.target == sourceEdge.target) {
                e.weight = Double.valueOf(res);
            }
        }

        System.out.println("AFTER----------");
        for (NodeFX p : circles) {
            System.out.println(p.node.name + " - ");
            for (Edge q : p.node.adjacents) {
                System.out.println(q.source.name + " " + q.target.name + " weigh: " + q.weight);
            }
        }
    }

    public class NodeFX extends Circle {

        Node node;
        Point point;
        Label distance = new Label("Dist. : INFINITY");
        Label visitTime = new Label("Visit : 0");
        Label lowTime = new Label("Low : 0");
        Label id;
        boolean isSelected = false;

        public NodeFX(double x, double y, double rad, String name) {
            super(x, y, rad);
            node = new Node(name, this);
            point = new Point((int) x, (int) y);
            id = new Label(name);
            canvasGroup.getChildren().add(id);
            id.setLayoutX(x - 18);
            id.setLayoutY(y - 18);
            this.setOpacity(0.5);
            this.setBlendMode(BlendMode.MULTIPLY);
            this.setId("node");

//            RightClickMenu rt = new RightClickMenu(this);
//            ContextMenu menu = rt.getMenu();
//            globalMenu = menu;
//            this.setOnContextMenuRequested(e -> {
//                if (addEdge || addNode) {
//                    menu.show(this, e.getScreenX(), e.getScreenY());
//                    menuBool = true;
//                }
//            });
//            menu.setOnAction(e -> {
//                menuBool = false;
//            });

            circles.add(this);
            System.out.println("Adding: " + circles.size());


        }
    }
}
	