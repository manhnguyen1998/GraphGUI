package application;

import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.*;

public class SceneController implements Initializable, ChangeListener {
    @FXML
    private RadioButton directButton, undirectButton, weighButton, unweighButton;

    @FXML
    private Slider slider;
    @FXML
    private Line edgeLine;
    @FXML
    private Arrow arrow;
    @FXML
    private ToggleButton dfs_button, bfs_button, dijistra_button, add_node_button, add_edge_button;
    @FXML
    private Label weight, sourceText = new Label("Source");
    @FXML
    private Button clear_button, resetButton;

    @FXML
    private Pane canvasGroup, viewer;

    private boolean weighted, unweighted, directed, undirected,
            bfs = true, dfs = true, dijkstra = true;

    NodeFX selectedNode = null;


    List<NodeFX> circles = new ArrayList<>();
    List<Shape> edges = new ArrayList<>();
    List<Label> distances = new ArrayList<Label>(), visitTime = new ArrayList<Label>(), lowTime = new ArrayList<Label>();

    List<Edge> realEdges = new ArrayList<>();
    boolean menuBool = false, calculate = false, calculated = false, addNode = true, addEdge = false;
    int nNode = 0, time = 500;
    public SequentialTransition st;
    Algorithm algo = new Algorithm();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        directButton.setSelected(directed);
        undirectButton.setSelected(weighted);
        weighButton.setSelected(undirected);
        unweighButton.setSelected(unweighted);
        ResetHandle(null);
        directButton.setOnAction(e -> {
            directed = true;
            undirected = false;
            System.out.println("dButton");
        });
        undirectButton.setOnAction(e -> {
            directed = false;
            undirected = true;
            System.out.println("udButton");
        });
        weighButton.setOnAction(e -> {
            weighted = true;
            unweighted = false;
            System.out.println("wButton");
        });
        unweighButton.setOnAction(e -> {
            weighted = false;
            unweighted = true;
            System.out.println("uwButton");
        });
        if (weighted) {
            bfs_button.setDisable(true);
            dfs_button.setDisable(true);
        }

        if (unweighted) {
            dijistra_button.setDisable(true);
        }

        slider.setMin(10);
        slider.setMax(1000);
        slider.setValue(500);
        slider.setSnapToTicks(true);
        slider.setMinorTickCount(100);
        slider.setCursor(Cursor.CLOSED_HAND);
        slider.toFront();
        slider.valueProperty().addListener(this);

    }

    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        int temp = (int) slider.getValue();

        if (temp > 500) {
            int diff = temp - 500;
            temp = 500;
            temp -= diff;
            temp += 10;
        } else if (temp < 500) {
            int diff = 500 - temp;
            temp = 500;
            temp += diff;
            temp -= 10;
        }
        time = temp;
        System.out.println(time);
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
                            //Adds edge
                            if (undirected) {
                                edgeLine = new Line(selectedNode.point.x, selectedNode.point.y, circle.point.x, circle.point.y);
                                canvasGroup.getChildren().add(edgeLine);
                                edgeLine.setId("line");
                            } else if (directed) {
                                arrow = new Arrow(selectedNode.point.x, selectedNode.point.y, circle.point.x, circle.point.y);
                                canvasGroup.getChildren().add(arrow);
                                arrow.setId("arrow");
                            }

                            //Adds weight
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
//                                if (weighted) {
//                                    mstEdges.add(temp);
//                                }

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
//                          Select algorithm
                    if (calculate && !calculated) {
                        if (bfs) {
                            algo.newBFS(circle.node);
                        } else if (dfs) {
                            algo.newDFS(circle.node);
                        } else if (dijkstra) {
                            algo.newDijkstra(circle.node);
                        }

                        calculated = true;
                    } else if (calculate && calculated) {

                        for (NodeFX n : circles) {
                            n.isSelected = false;
                            FillTransition ft1 = new FillTransition(Duration.millis(300), n);
                            ft1.setToValue(Color.BLACK);
                            ft1.play();
                        }
                        List<Node> path = algo.getShortestPathTo(circle.node);
                        for (Node n : path) {
                            FillTransition ft1 = new FillTransition(Duration.millis(300), n.circle);
                            ft1.setToValue(Color.BLUE);
                            ft1.play();
                        }
                    }
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
    public void ClearHandle(ActionEvent event) {
        if (st != null && st.getStatus() != Animation.Status.STOPPED)
            st.stop();
        if (st != null) st.getChildren().clear();
        menuBool = false;
        selectedNode = null;
        calculated = false;
        System.out.println("IN CLEAR:" + circles.size());
        for (NodeFX n : circles) {
            n.isSelected = false;
            n.node.visited = false;
            n.node.previous = null;
            n.node.minDistance = Double.POSITIVE_INFINITY;
            n.node.DAGColor = 0;

            FillTransition ft1 = new FillTransition(Duration.millis(300), n);
            ft1.setToValue(Color.BLACK);
            ft1.play();
        }
        for (Shape x : edges) {
            if (undirected) {
                StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), x);
                ftEdge.setToValue(Color.BLACK);
                ftEdge.play();
            } else if (directed) {
                FillTransition ftEdge = new FillTransition(Duration.millis(time), x);
                ftEdge.setToValue(Color.BLACK);
                ftEdge.play();
            }
        }
        canvasGroup.getChildren().remove(sourceText);
        for (Label x : distances) {
            x.setText("Distance : INFINITY");
            canvasGroup.getChildren().remove(x);
        }
//

        distances = new ArrayList<>();

        add_node_button.setDisable(false);
        add_edge_button.setDisable(false);
        AddNodeHandle(null);
        bfs = false;
        dfs = false;
        dijkstra = false;
//
    }

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

    @FXML
    public void DijkstraHandle(ActionEvent event) {
        addNode = false;
        addEdge = false;
        add_node_button.setSelected(false);
        add_edge_button.setSelected(false);
        add_node_button.setDisable(true);
        add_edge_button.setDisable(true);
        calculate = true;
        clear_button.setDisable(false);
        bfs = false;
        dfs = false;
        dijkstra = true;
    }

    @FXML
    public void DFSHandle(ActionEvent event) {
        addNode = false;
        addEdge = false;
        add_node_button.setSelected(false);
        add_edge_button.setSelected(false);
        add_node_button.setDisable(true);
        add_edge_button.setDisable(true);
        calculate = true;
        clear_button.setDisable(false);
        bfs = false;
        dfs = true;
        dijkstra = false;
    }

    @FXML
    public void BFSHandle(ActionEvent event) {
        addNode = false;
        addEdge = false;
        add_node_button.setSelected(false);
        add_edge_button.setSelected(false);
        add_node_button.setDisable(true);
        add_edge_button.setDisable(true);
        calculate = true;
        clear_button.setDisable(false);
        bfs = true;
        dfs = false;
        dijkstra = false;
    }

    @FXML
    public void ResetHandle(ActionEvent event) {
        ClearHandle(null);
        nNode = 0;
        canvasGroup.getChildren().clear();
        canvasGroup.getChildren().addAll(viewer);
        selectedNode = null;
        circles = new ArrayList<NodeFX>();
        distances = new ArrayList<Label>();

        addNode = true;
        addEdge = false;
        calculate = false;
        calculated = false;
        unweighted = false;
        weighted = false;
        directed = false;
        undirected = false;
        directButton.setSelected(false);
        undirectButton.setSelected(false);
        weighButton.setSelected(false);
        unweighButton.setSelected(false);
        add_node_button.setSelected(true);
        add_edge_button.setSelected(false);
        add_edge_button.setDisable(true);
        add_node_button.setDisable(false);
        clear_button.setDisable(true);
        algo = new Algorithm();
        bfs_button.setDisable(true);

        dfs_button.setDisable(true);
        dijistra_button.setDisable(true);
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

            circles.add(this);
            System.out.println("Adding: " + circles.size());
        }
    }

    public class Algorithm {
        public void newDijkstra(Node source) {
            new Dijkstra(source);
        }

        class Dijkstra {
            Dijkstra(Node source) {
                for (NodeFX n : circles) {
                    distances.add(n.distance);
                    n.distance.setLayoutX(n.point.x + 20);
                    n.distance.setLayoutY(n.point.y);
                    canvasGroup.getChildren().add(n.distance);
                }
                sourceText.setLayoutX(source.circle.point.x + 20);
                sourceText.setLayoutY(source.circle.point.y + 10);
                canvasGroup.getChildren().add(sourceText);
                st = new SequentialTransition();
                source.circle.distance.setText("distance : " + 0);

                source.minDistance = 0;
                PriorityQueue<Node> pq = new PriorityQueue<Node>();
                pq.add(source);
                while (!pq.isEmpty()) {
                    Node u = pq.poll();
                    System.out.println(u.name);

                    FillTransition ft = new FillTransition(Duration.millis(time), u.circle);
                    ft.setToValue(Color.CHOCOLATE);
                    st.getChildren().add(ft);

                    for (Edge e : u.adjacents) {
                        if (e != null) {
                            Node v = e.target;
//                            System.out.println("HERE " + v.name);
                            if (u.minDistance + e.weight < v.minDistance) {
                                pq.remove(v);
                                v.minDistance = u.minDistance + e.weight;
                                v.previous = u;
                                pq.add(v);

                                if (undirected) {
                                    StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), e.line);
                                    ftEdge.setToValue(Color.FORESTGREEN);
                                    st.getChildren().add(ftEdge);
                                } else if (directed) {
                                    FillTransition ftEdge = new FillTransition(Duration.millis(time), e.line);
                                    ftEdge.setToValue(Color.FORESTGREEN);
                                    st.getChildren().add(ftEdge);
                                }

                                FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
                                ft1.setToValue(Color.FORESTGREEN);
                                ft1.setOnFinished(ev -> {
                                    v.circle.distance.setText("distance : " + v.minDistance);
                                });
                                ft1.onFinishedProperty();
                                st.getChildren().add(ft1);
                            }
                        }
                    }
                    FillTransition ft2 = new FillTransition(Duration.millis(time), u.circle);
                    ft2.setToValue(Color.BLUEVIOLET);
                    st.getChildren().add(ft2);
                }


                st.setOnFinished(ev -> {
                    for (NodeFX n : circles) {
                        FillTransition ft1 = new FillTransition(Duration.millis(time), n);
                        ft1.setToValue(Color.BLACK);
                        ft1.play();
                    }
                    if (directed) {
                        for (Shape n : edges) {
                            n.setFill(Color.BLACK);
                        }
                    } else if (undirected) {
                        for (Shape n : edges) {
                            n.setStroke(Color.BLACK);
                        }
                    }
                    FillTransition ft1 = new FillTransition(Duration.millis(time), source.circle);
                    ft1.setToValue(Color.RED);
                    ft1.play();

                });
                st.onFinishedProperty();
                st.play();


            }
        }

        public void newBFS(Node source) {
            new BFS(source);
        }

        class BFS {
            BFS(Node source) {
                for (NodeFX n : circles) {
                    distances.add(n.distance);
                    n.distance.setLayoutX(n.point.x + 20);
                    n.distance.setLayoutY(n.point.y);
                    canvasGroup.getChildren().add(n.distance);
                }
                sourceText.setLayoutX(source.circle.point.x + 20);
                sourceText.setLayoutY(source.circle.point.y + 10);
                canvasGroup.getChildren().add(sourceText);
                st = new SequentialTransition();
                source.circle.distance.setText("Dist. : " + 0);

                source.minDistance = 0;
                source.visited = true;
                LinkedList<Node> q = new LinkedList<Node>();
                q.push(source);
                while (!q.isEmpty()) {
                    Node u = q.removeLast();
                    FillTransition ft = new FillTransition(Duration.millis(time), u.circle);
                    if (u.circle.getFill() == Color.BLACK) {
                        ft.setToValue(Color.CHOCOLATE);
                    }
                    st.getChildren().add(ft);
                    System.out.println(u.name);
                    for (Edge e : u.adjacents) {
                        if (e != null) {
                            Node v = e.target;

                            if (!v.visited) {
                                v.minDistance = u.minDistance + 1;
                                v.visited = true;
                                q.push(v);
                                v.previous = u;


                                if (undirected) {
                                    StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), e.line);
                                    ftEdge.setToValue(Color.FORESTGREEN);
                                    st.getChildren().add(ftEdge);
                                } else if (directed) {
                                    FillTransition ftEdge = new FillTransition(Duration.millis(time), e.line);
                                    ftEdge.setToValue(Color.FORESTGREEN);
                                    st.getChildren().add(ftEdge);
                                }
                                //</editor-fold>
                                FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
                                ft1.setToValue(Color.FORESTGREEN);
                                ft1.setOnFinished(ev -> {
                                    v.circle.distance.setText("Dist. : " + v.minDistance);
                                });
                                ft1.onFinishedProperty();
                                st.getChildren().add(ft1);
                            }
                        }
                    }
                    FillTransition ft2 = new FillTransition(Duration.millis(time), u.circle);
                    ft2.setToValue(Color.BLUEVIOLET);
                    st.getChildren().add(ft2);
                }

                st.setOnFinished(ev -> {
                    for (NodeFX n : circles) {
                        FillTransition ft1 = new FillTransition(Duration.millis(time), n);
                        ft1.setToValue(Color.BLACK);
                        ft1.play();
                    }
                    if (directed) {
                        for (Shape n : edges) {
                            n.setFill(Color.BLACK);
                        }
                    } else if (undirected) {
                        for (Shape n : edges) {
                            n.setStroke(Color.BLACK);
                        }
                    }
                    FillTransition ft1 = new FillTransition(Duration.millis(time), source.circle);
                    ft1.setToValue(Color.RED);
                    ft1.play();
                });
                st.onFinishedProperty();
                st.play();
            }
        }

        public void newDFS(Node source) {
            new DFS(source);
        }

        class DFS {

            DFS(Node source) {

                for (NodeFX n : circles) {
                    distances.add(n.distance);
                    n.distance.setLayoutX(n.point.x + 20);
                    n.distance.setLayoutY(n.point.y);
                    canvasGroup.getChildren().add(n.distance);
                }
                sourceText.setLayoutX(source.circle.point.x + 20);
                sourceText.setLayoutY(source.circle.point.y + 10);
                canvasGroup.getChildren().add(sourceText);
                st = new SequentialTransition();
                source.circle.distance.setText("Dist. : " + 0);

                source.minDistance = 0;
                source.visited = true;
                DFSRecursion(source, 0);

                st.setOnFinished(ev -> {
                    for (NodeFX n : circles) {
                        FillTransition ft1 = new FillTransition(Duration.millis(time), n);
                        ft1.setToValue(Color.BLACK);
                        ft1.play();
                    }
                    if (directed) {
                        for (Shape n : edges) {
                            n.setFill(Color.BLACK);
                        }
                    } else if (undirected) {
                        for (Shape n : edges) {
                            n.setStroke(Color.BLACK);
                        }
                    }
                    FillTransition ft1 = new FillTransition(Duration.millis(time), source.circle);
                    ft1.setToValue(Color.RED);
                    ft1.play();

                });
                st.onFinishedProperty();
                st.play();

            }

            public void DFSRecursion(Node source, int level) {
                FillTransition ft = new FillTransition(Duration.millis(time), source.circle);
                if (source.circle.getFill() == Color.BLACK) {
                    ft.setToValue(Color.FORESTGREEN);
                }
                st.getChildren().add(ft);
                for (Edge e : source.adjacents) {
                    if (e != null) {
                        Node v = e.target;
                        if (!v.visited) {
                            v.minDistance = source.minDistance + 1;
                            v.visited = true;
                            v.previous = source;
//                        v.circle.distance.setText("Dist. : " + v.minDistance);
                            //<editor-fold defaultstate="collapsed" desc="Change Edge colors">
                            if (undirected) {
                                StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.FORESTGREEN);
                                st.getChildren().add(ftEdge);
                            } else if (directed) {
                                FillTransition ftEdge = new FillTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.FORESTGREEN);
                                st.getChildren().add(ftEdge);
                            }
                            //</editor-fold>
                            DFSRecursion(v, level + 1);
                            //<editor-fold defaultstate="collapsed" desc="Animation Control">
                            //<editor-fold defaultstate="collapsed" desc="Change Edge colors">
                            if (undirected) {
                                StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.BLUEVIOLET);
                                st.getChildren().add(ftEdge);
                            } else if (directed) {
                                FillTransition ftEdge = new FillTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.BLUEVIOLET);
                                st.getChildren().add(ftEdge);
                            }
                            //</editor-fold>
                            FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
                            ft1.setToValue(Color.BLUEVIOLET);
                            ft1.onFinishedProperty();
                            ft1.setOnFinished(ev -> {
                                v.circle.distance.setText("Dist. : " + v.minDistance);
                            });
                            st.getChildren().add(ft1);
                            //</editor-fold>
                        }
                    }
                }
            }
        }

        public List<Node> getShortestPathTo(Node target) {
            List<Node> path = new ArrayList<Node>();
            for (Node i = target; i != null; i = i.previous) {
                path.add(i);
            }
            Collections.reverse(path);
            return path;
        }
    }
}
	