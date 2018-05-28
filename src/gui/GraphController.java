package gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GraphController {

	// Model of graph gui
	private GraphModel model;
	// View of graph gui
	private GraphView view;
	// True if mouse click is first of two
	private boolean firstClick = false;
	// x, y coordinates of circle of origin vertex for new edge
	private List<Double> originCoords;
	// Last x, y coordinates of vertex being dragged
	private List<Double> lastCoords;
	// True if canvas context menu is being shown
	private boolean canvasContextShown = false;
	// True if vertex context is being shown
	private boolean vertexContextShown = false;
	// True if edge context is being shown
	private boolean edgeContextShown = false;
	// True if draw mode activated, where each new vertex is connected to the last
	// added vertex
	private boolean drawMode = false;
	// File of graph currently open
	private File currentFile = null;
	// Stage of the graph gui
	private Stage stage;

	public GraphController(GraphModel model, GraphView view) {
		this.model = model;
		this.view = view;

		originCoords = new ArrayList<>();
		lastCoords = new ArrayList<>();

		view.addContextMenus(createCanvasContextMenu(), createVertexContextMenu(), createEdgeContextMenu());
		view.addCanvasGestures(new CanvasGestures());
		view.addGraphGestures(new VertexGestures(), new EdgeGestures());
		view.drawGraph(null);
	}

	/**
	 * Used to contain and assign mouse click, release and drag event handlers to
	 * graph vertices.
	 */
	public class VertexGestures {

		double orgSceneX, orgSceneY;
		double orgTranslateX, orgTranslateY;
		boolean dragged = false;

		public void addGestures(Node node) {
			node.setOnMousePressed(vertexOnMousePressedEventHandler);
			node.setOnMouseDragged(vertexOnMouseDraggedEventHandler);
			node.setOnMouseClicked(vertexOnMouseClickEventHandler);
		}

		EventHandler<MouseEvent> vertexOnMouseClickEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				if (event.getSource() instanceof Circle && !dragged) {
					Circle c = ((Circle) (event.getSource()));
					if (event.getButton().equals(MouseButton.PRIMARY)) {
						if (canvasContextShown || vertexContextShown || edgeContextShown) {
							hideContextMenus();
							return;
						}

						double centreX = c.getCenterX();
						double centreY = c.getCenterY();

						System.out.println(centreX + ", " + centreY);

						List<Double> destCoords = new ArrayList<>();
						destCoords.add(centreX);
						destCoords.add(centreY);

						// Connect last added vertex to selected vertex if draw mode is enabled
						if (drawMode) {
							List<Double> lastAdded = model.getLastAddedCoords();
							if (lastAdded != null) {
								firstClick = false;
								model.addEdge(lastAdded, destCoords, "");
							}
							view.drawGraph(null);
							model.clearLastAdded();
							return;
						}
						
						if (firstClick) {
							// Connect selected vertex to previously selected vertex
							firstClick = false;
							model.addEdge(originCoords, destCoords, "");
							view.modified = true;
							view.drawGraph(null);
						} else {
							// Set initially selected vertex
							firstClick = true;
							originCoords.clear();
							originCoords.add(centreX);
							originCoords.add(centreY);
						}
						view.enableVertexHighlight(c);
					} else if (event.getButton().equals(MouseButton.SECONDARY)) {
						view.showVertexContext(event.getScreenX(), event.getScreenY());
						view.setContextVertex(c);
					}
				} else if (dragged) {
					dragged = false;
				}
			}
		};

		EventHandler<MouseEvent> vertexOnMousePressedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				if (event.getSource() instanceof Circle && event.getButton().equals(MouseButton.PRIMARY)) {
					if (canvasContextShown || vertexContextShown || edgeContextShown) {
						hideContextMenus();
						return;
					}

					orgSceneX = event.getSceneX();
					orgSceneY = event.getSceneY();

					Circle c = ((Circle) (event.getSource()));

					orgTranslateX = c.getCenterX();
					orgTranslateY = c.getCenterY();

					lastCoords = new ArrayList<>();
					lastCoords.add(orgTranslateX);
					lastCoords.add(orgTranslateY);

					view.disableVertexHighlight(c);

				}
			}
		};

		EventHandler<MouseEvent> vertexOnMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				if (event.getSource() instanceof Circle && event.getButton().equals(MouseButton.PRIMARY)) {

					firstClick = false;
					dragged = true;

					double offsetX = event.getSceneX() - orgSceneX;
					double offsetY = event.getSceneY() - orgSceneY;

					double newTranslateX = orgTranslateX + offsetX;
					double newTranslateY = orgTranslateY + offsetY;

					Circle c = ((Circle) (event.getSource()));

					c.setCenterX(newTranslateX);
					c.setCenterY(newTranslateY);

					model.moveVertex(lastCoords.get(0), lastCoords.get(1), newTranslateX, newTranslateY);

					lastCoords = new ArrayList<>();
					lastCoords.add(newTranslateX);
					lastCoords.add(newTranslateY);
					view.modified = true;
					view.drawGraph(c);
				}
			}
		};
	}
	
	public class EdgeGestures {
		
		public void addGestures(Node node) {
			node.setOnMouseClicked(edgeOnMouseClickEventHandler);
		}
		
		EventHandler<MouseEvent> edgeOnMouseClickEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getButton().equals(MouseButton.SECONDARY)) {
					Line l = ((Line) (event.getSource()));
					view.showEdgeContext(event.getScreenX(), event.getScreenY());
					view.setContextEdge(l);
				}
			}
			
		};
	}

	/**
	 * Used to contain and assign mouse click, release and drag event handlers to
	 * the canvas.
	 */
	public class CanvasGestures {

		Canvas canvas;

		public void addGestures(Canvas canvas) {
			this.canvas = canvas;
			canvas.setOnMouseReleased(canvasOnMouseReleasedEventHandler);
		}

		EventHandler<MouseEvent> canvasOnMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getButton().equals(MouseButton.PRIMARY)) {
					if (canvasContextShown || vertexContextShown || edgeContextShown) {
						hideContextMenus();
						return;
					}
					double x = event.getX();
					double y = event.getY();
					if (!firstClick) {
						model.addVertex(Double.toString(x + y), x, y);
						view.modified = true;
						System.out.println(x + ", " + y);
					} else {
						firstClick = false;
					}
					if (drawMode) {
						model.connectLastAdded();
					}
					hideContextMenus();
					view.drawGraph(null);
				} else {
					hideContextMenus();
					view.showCanvasContext(event.getScreenX(), event.getScreenY());
				}
			}
		};

	}

	public ContextMenu createCanvasContextMenu() {
		ContextMenu contextMenu = new ContextMenu();
		List<MenuItem> menuItems = new ArrayList<>();

		contextMenu.setOnShown(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				canvasContextShown = true;
			}
		});

		contextMenu.setOnHidden(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				canvasContextShown = false;
			}

		});

		menuItems.add(new MenuItem("Clear graph"));
		;
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				model.clearGraph();
				view.drawGraph(null);
				view.modified = true;
			}
		});

		menuItems.add(new MenuItem("Connect graph"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				model.connectAllVertices();
				view.drawGraph(null);
				view.modified = true;
			}
		});

		menuItems.add(new MenuItem("Disconnect graph"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				model.removeAllEdges();
				view.drawGraph(null);
				view.modified = true;
			}
		});
		
		menuItems.add(new MenuItem("Align to grid"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				model.alignVerticesToGrid();
				view.drawGraph(null);
				view.modified = true;
			}
		});

		menuItems.add(new SeparatorMenuItem());

		menuItems.add(new RadioMenuItem("Draw mode"));
		((RadioMenuItem) menuItems.get(menuItems.size() - 1)).setSelected(false);
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				toggleDrawMode();
			}
		});

		menuItems.add(new SeparatorMenuItem());

		menuItems.add(new MenuItem("Save"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				File selectedFile = currentFile;
				if (currentFile == null) {
					selectedFile = view.graphFileChooser("Save As");
				}
				if (selectedFile == null) {
					return;
				}
				try {
					model.saveGraphToFile(selectedFile);
					currentFile = selectedFile;
					view.modified = false;
				} catch (IOException e) {
					view.showErrorDialog(e.getMessage());
				}
			}
		});

		menuItems.add(new MenuItem("Save as"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				File selectedFile = view.graphFileChooser("Save As");
				if (selectedFile == null) {
					return;
				}
				try {
					model.saveGraphToFile(selectedFile);
					currentFile = selectedFile;
					view.modified = false;
				} catch (IOException e) {
					view.showErrorDialog(e.getMessage());
				}
			}
		});

		menuItems.add(new MenuItem("Load"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				File selectedFile = view.graphFileChooser("Load");
				try {
					model.loadGraphFromFile(selectedFile);
					currentFile = selectedFile;
				} catch (IOException e) {
					view.showErrorDialog(e.getMessage());
					model.clearGraph();
				}
				view.modified = false;
				view.drawGraph(null);
			}
		});

		menuItems.add(new MenuItem("Export"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				File selectedFile = view.graphFileChooser("Export");
				try {
					view.saveGraphAsImage(selectedFile);
				} catch (IOException e) {
					view.showErrorDialog("Could not export image");
					model.clearGraph();
				}
			}
		});

		menuItems.add(new SeparatorMenuItem());

		menuItems.add(new RadioMenuItem("Hide statistics"));
		((RadioMenuItem) menuItems.get(menuItems.size() - 1)).setSelected(false);
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				view.toggleShowStatistics();
			}
		});

		menuItems.add(new SeparatorMenuItem());

		menuItems.add(new MenuItem("Properties"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {

			}
		});

		contextMenu.getItems().addAll(menuItems);
		return contextMenu;
	}

	public ContextMenu createVertexContextMenu() {
		ContextMenu contextMenu = new ContextMenu();
		List<MenuItem> menuItems = new ArrayList<>();

		contextMenu.setOnShown(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				vertexContextShown = true;
			}
		});

		contextMenu.setOnHidden(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				vertexContextShown = false;
			}

		});

		menuItems.add(new MenuItem("Delete vertex"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				List<Double> coords = view.deleteContextVertex();
				model.deleteVertex(coords.get(0), coords.get(1));
				view.modified = true;
				view.drawGraph(null);
			}
		});

		menuItems.add(new MenuItem("Connect vertex"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				model.connectVertex(view.getContextVertexCoords());
				view.modified = true;
				view.drawGraph(null);
			}
		});

		menuItems.add(new MenuItem("Disconnect vertex"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				model.disconnectVertex(view.getContextVertexCoords());
				view.modified = true;
				view.drawGraph(null);
			}
		});
		
		/*
		menuItems.add(new MenuItem("Merge vertex"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				ArrayList<ArrayList<Double>> opposite = model.getOppositeVertices(view.getContextVertexCoords());
				if (!opposite.isEmpty()) {
					model.connectVerticesInSequence(model.getOppositeVertices(view.getContextVertexCoords()));
					List<Double> coords = view.deleteContextVertex();
					model.deleteVertex(coords.get(0), coords.get(1));
					view.drawGraph(null);	
				}
			}
		});
		*/
		
		menuItems.add(new MenuItem("Connect to nearest"));
		menuItems.get(menuItems.size() - 1).setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				model.connectToNearest(view.getContextVertexCoords());
				view.drawGraph(null);	
				view.modified = true;
			}
		});

		contextMenu.getItems().addAll(menuItems);
		return contextMenu;
	}
	
	public ContextMenu createEdgeContextMenu() {
		ContextMenu contextMenu = new ContextMenu();
		List<MenuItem> menuItems = new ArrayList<>();

		contextMenu.setOnShown(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				edgeContextShown = true;
			}
		});

		contextMenu.setOnHidden(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				edgeContextShown = false;
			}

		});

		MenuItem item1 = new MenuItem("Delete edge");
		menuItems.add(item1);
		item1.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				List<Double> coords = view.deleteContextEdge();
				model.deleteEdge(coords);
				view.modified = true;
				view.drawGraph(null);
			}
		});
		
		contextMenu.getItems().addAll(menuItems);
		return contextMenu;
	}

	public void hideContextMenus() {
		view.hideContextMenus();
		canvasContextShown = false;
		vertexContextShown = false;
		edgeContextShown = false;
		view.setContextVertex(null);
		view.setContextEdge(null);
	}

	public void toggleDrawMode() {
		drawMode = !drawMode;
	}
	
	public void addStage(Stage stage) {
		this.stage = stage;
		
		stage.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldStageWidth, Number newStageWidth) {
				view.setCanvasWidth(((Double) newStageWidth).intValue());
			}			
		});
		
		stage.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldStageHeight, Number newStageHeight) {
				view.setCanvasWidth(((Double) newStageHeight).intValue());
			}			
		});
	}
}
