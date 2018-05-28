package gui;

import gui.GraphController.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import adts.*;

public class GraphView {
	/**
	 * Features:
	 * 
	 * - (Done) Add vertices
	 * 
	 * - (Done) Add edges connecting existing vertices
	 * 
	 * - (Done) Change settings/states with context menu
	 * 
	 * - Undo/redo vertex and edge additions
	 * 
	 * - (Done) Move vertices around (mouse drag)
	 * 
	 * - Change properties (vertex and edge colours, vertex radius, edge radius)
	 * 
	 * - (Done) Draw mode (new vertex connects to last added vertex)
	 * 
	 * - Select and move multiple vertices at once
	 * 
	 * - (Done) Remove vertices
	 * 
	 * - (Done) Remove selected edge
	 * 
	 * - Zoom in/out graph area
	 * 
	 * - (Done) Show graph statistics
	 * 
	 * - Compute shortest path between two given vertices
	 * 
	 * - (Done) Load graph from text file
	 * 
	 * - (Done) Save graph to text file
	 * 
	 * - (Done) Save graph as image file
	 * 
	 * - (Done) Save graph to same file if it has previously been saved
	 * 
	 * - Connect two vertices with shared vertex and delete shared vertex
	 * 
	 * Technical:
	 * 
	 * - Redraw only those edges that have been moved
	 * 
	 * - Prompt user if they attempt to exit window or load graph when current is
	 * unsaved
	 * 
	 * - Use regex to provide more appropriate error message if file being loaded if
	 * incorrectly formatted
	 */

	// Graph gui main stage
	private Stage mainStage;
	// Graph gui properties stage
	private Stage propertiesStage;
	// Model of graph gui
	private GraphModel model;
	// Underlying canvas
	private Canvas canvas;
	// Overlay to which vertices and edges are added
	private Pane overlay;
	// GUI root object
	private Group root;
	// Graphics context of canvas
	private GraphicsContext gc;
	// Context menu used to change program states
	private ContextMenu canvasContext;
	// Context menu used to interact with vertices
	private ContextMenu vertexContext;
	// Context menu used to interact with edges
	private ContextMenu edgeContext;
	// Text displaying graph statistics
	private Text statsText;
	// Display graph statistics if true, toggled with menu
	private boolean showStats = true;

	// Mouse gestures associated with vertices
	private VertexGestures vertexGestures;
	// Mouse gestures associated with edges
	private EdgeGestures edgeGestures;

	// Vertex circles of graph
	private List<Circle> vertices;
	// Edge lines of graph
	private List<Line> edges;

	// Height of canvas
	private int CANVAS_HEIGHT = 600;
	// Width of canvas
	private int CANVAS_WIDTH = 600;
	// Width of each edge line stroke
	private int EDGE_WIDTH = 3;
	// Radius of each vertex circle
	private int VERTEX_RADIUS = 15;

	// Vertex for which context menu was created
	private Circle contextVertex;
	// Edge for which context menu was created
	private Line contextEdge;
	
	public boolean modified = false;

	/**
	 * Create blank graph view.
	 * 
	 * @param model
	 *            model of graph gui
	 */
	public GraphView(GraphModel model) {
		this.model = model;
		root = new Group();
		canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
		overlay = new Pane();
		overlay.setPickOnBounds(false);
		gc = canvas.getGraphicsContext2D();

		vertices = new ArrayList<>();
		edges = new ArrayList<>();

		contextVertex = new Circle();
		contextEdge = new Line();

		Font font = new Font("Calibri", 15);
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

		statsText = new Text();
		statsText.setFont(font);
		statsText.setX(CANVAS_WIDTH - 115);
		statsText.setY(CANVAS_HEIGHT - 55);
		statsText.setMouseTransparent(true);
		updateStatisticsText();

		root.getChildren().add(canvas);
		root.getChildren().add(overlay);
	}

	/**
	 * Returns the scene for the graph gui application.
	 * 
	 * @return returns the scene for the application
	 */
	public Scene getScene() {
		return new Scene(root, CANVAS_HEIGHT, CANVAS_WIDTH);
	}

	/**
	 * Remove and draw all vertices and edges of graph.
	 */
	public void drawGraph(Circle exclude) {
		drawEdges();
		drawVertices(exclude);
		updateStatisticsText();
	}

	/**
	 * Draw all vertices lines in graph. All vertices in graph are removed and then
	 * redrawn using model coordinates.
	 */
	private void drawVertices(Circle exclude) {
		// Mapping of vertices to their corresponding x, y coordinates
		Map<List<Double>, Vertex<String, String>> vertexCoords = model.getVertices();

		vertices.remove(exclude);
		overlay.getChildren().removeAll(vertices);
		vertices.clear();

		// Create and set properties of vertices
		for (List<Double> coords : vertexCoords.keySet()) {
			// Prevent creation of excluded circle
			if (exclude != null && coords.get(0) == exclude.getCenterX() && coords.get(1) == exclude.getCenterY()) {
				continue;
			}
			Circle circle = new Circle(VERTEX_RADIUS);
			circle.setStroke(Color.BLACK);
			circle.setStrokeWidth(2);
			circle.setFill(Color.WHITE);
			circle.setCenterX(coords.get(0));
			circle.setCenterY(coords.get(1));
			vertices.add(circle);
			vertexGestures.addGestures(circle);
		}
		overlay.getChildren().addAll(vertices);
		if (exclude != null) {
			exclude.toFront();
			vertices.add(exclude);
		}
	}

	/**
	 * Draw all edge lines in graph. All edges in graph are removed and then redrawn
	 * using model coordinates.
	 */
	private void drawEdges() {
		// Mapping of edges to their corresponding start and end x, y coordinates
		Map<Edge<String, String>, List<Double>> edgeCoords = model.getEdges();

		overlay.getChildren().removeAll(edges);
		edges.clear();

		// Create and set properties of edges
		for (Edge<String, String> edge : edgeCoords.keySet()) {
			List<Double> coords = edgeCoords.get(edge);
			Line line = new Line();
			line.setStartX(coords.get(0));
			line.setStartY(coords.get(1));
			line.setEndX(coords.get(2));
			line.setEndY(coords.get(3));
			line.setStrokeWidth(EDGE_WIDTH);
			edges.add(line);
			edgeGestures.addGestures(line);
		}
		overlay.getChildren().addAll(edges);
	}

	/**
	 * Highlight given vertex by changing stroke color from black to blue.
	 * 
	 * @param vertex
	 *            vertex circle for which stroke colour will be changed
	 */
	public void enableVertexHighlight(Circle vertex) {
		vertex.setStroke(Color.RED);
	}

	/**
	 * Disable highlight for given vertex by changing stroke color from blue to
	 * black.
	 * 
	 * @param vertex
	 *            vertex circle for which stroke colour will be changed
	 */
	public void disableVertexHighlight(Circle vertex) {
		vertex.setStroke(Color.BLACK);
	}

	/**
	 * Update statistics text, displaying the number of vertices and edges and
	 * whether or not the graph is complete and connected.
	 */
	public void updateStatisticsText() {
		root.getChildren().remove(statsText);
		if (showStats) {
			// Change case of initial letter of isConnected and isComplete
			String isConnected = Boolean.toString(model.graphIsConnected());
			isConnected = isConnected.substring(0, 1).toUpperCase() + isConnected.substring(1);
			String isComplete = Boolean.toString(model.graphIsComplete());
			isComplete = isComplete.substring(0, 1).toUpperCase() + isComplete.substring(1);
			statsText.setText("Vertices: " + vertices.size() + "\nEdges: " + edges.size() + "\nConnected: "
					+ isConnected + "\nComplete: " + isComplete);

			root.getChildren().add(statsText);
		}
	}

	/**
	 * Set context vertex to the given vertex.
	 * 
	 * @param vertex
	 *            vertex to which context vertex will be set
	 */
	public void setContextVertex(Circle vertex) {
		if (vertex != null) {
			contextVertex = vertex;
		} else {
			contextVertex = new Circle();
		}
	}

	/**
	 * Set context edge to the given edge.
	 * 
	 * @param edge
	 *            edge to which context edge will be set
	 */
	public void setContextEdge(Line edge) {
		if (edge != null) {
			contextEdge = edge;
		} else {
			contextEdge = new Line();
		}
	}

	/**
	 * Delete the stored context vertex.
	 * 
	 * @return x, y coordinates of deleted context vertex
	 */
	public List<Double> deleteContextVertex() {
		List<Double> coords = getContextVertexCoords();
		overlay.getChildren().remove(contextVertex);
		return coords;
	}

	/**
	 * Delete the stored context edge.
	 * 
	 * @return start and end x, y coordinates of deleted context edge
	 */
	public List<Double> deleteContextEdge() {
		List<Double> coords = getContextEdgeCoords();
		overlay.getChildren().remove(contextEdge);
		return coords;
	}

	/**
	 * Returns the x, y coordinates of the context vertex.
	 * 
	 * @return x, y coordinates of context vertex
	 */
	public List<Double> getContextVertexCoords() {
		List<Double> coords = new ArrayList<>();
		coords.add(contextVertex.getCenterX());
		coords.add(contextVertex.getCenterY());
		return coords;
	}

	/**
	 * Returns the start and end x, y coordinates of the context edge.
	 * 
	 * @return start and end x, y coordinates of context edge
	 */
	public List<Double> getContextEdgeCoords() {
		List<Double> coords = new ArrayList<>();
		coords.add(contextEdge.getStartX());
		coords.add(contextEdge.getStartY());
		coords.add(contextEdge.getEndX());
		coords.add(contextEdge.getEndY());
		return coords;
	}

	/**
	 * Toggle whether the statistics are being displayed.
	 */
	public void toggleShowStatistics() {
		showStats = !showStats;
		updateStatisticsText();
	}

	/**
	 * Hide all context menus.
	 */
	public void hideContextMenus() {
		canvasContext.hide();
		vertexContext.hide();
		edgeContext.hide();
	}

	/**
	 * Show canvas context menu at given x, y screen coordinates.
	 * 
	 * @param screenX
	 *            screen x-coordinate
	 * @param screenY
	 *            screen y-coordinate
	 */
	public void showCanvasContext(double screenX, double screenY) {
		canvasContext.show(canvas, screenX, screenY);
	}

	/**
	 * Show vertex context menu at given x, y screen coordinates.
	 * 
	 * @param screenX
	 *            screen x-coordinate
	 * @param screenY
	 *            screen y-coordinate
	 */
	public void showVertexContext(double screenX, double screenY) {
		vertexContext.show(canvas, screenX, screenY);
	}

	/**
	 * Show edge context menu at given x, y screen coordinates.
	 * 
	 * @param screenX
	 *            screen x-coordinate
	 * @param screenY
	 *            screen y-coordinate
	 */
	public void showEdgeContext(double screenX, double screenY) {
		edgeContext.show(canvas, screenX, screenY);
	}

	/**
	 * Set the radius of all vertices.
	 * 
	 * @param radius
	 *            new radius of all vertices
	 */
	public void setVertexRadius(int radius) {
		VERTEX_RADIUS = radius;
	}

	/**
	 * Set the width of the canvas.
	 * 
	 * @param width
	 *            new width of canvas
	 */
	public void setCanvasWidth(int width) {
		gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
		CANVAS_WIDTH = width;
		canvas.setWidth(CANVAS_WIDTH);
		statsText.setX(CANVAS_WIDTH - 150);
		gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
		updateStatisticsText();
	}

	/**
	 * Set the height of the canvas.
	 * 
	 * @param height
	 *            new height of canvas
	 */
	public void setCanvasHeight(int height) {
		gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
		CANVAS_HEIGHT = height;
		canvas.setHeight(CANVAS_HEIGHT);
		statsText.setY(CANVAS_HEIGHT - 65);
		gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
		updateStatisticsText();
	}

	/**
	 * Returns file selected using file chooser dialog.
	 * 
	 * @param title
	 *            title of file chooser dialog
	 * @return file selected using file chooser
	 */
	public File graphFileChooser(String title) {
		if (!(title.equals("Save As") || title.equals("Load") || title.equals("Export"))) {
			return null;
		}
		FileChooser fileChooser = new FileChooser();
		title.trim();
		fileChooser.setTitle(title);

		File selectedFile = null;

		if (title.equals("Export")) {
			fileChooser.getExtensionFilters().add(new ExtensionFilter("Image Files", "*.PNG"));
		} else {
			fileChooser.getExtensionFilters().add(new ExtensionFilter("Text Files", "*.txt"));
		}

		if (title.equals("Load")) {
			selectedFile = fileChooser.showOpenDialog(mainStage);
		} else {
			selectedFile = fileChooser.showSaveDialog(mainStage);
		}
		return selectedFile;
	}

	/**
	 * Write the graph vertices and edges to a .PNG image file.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void saveGraphAsImage(File file) throws IOException {
		if (file == null) {
			return;
		}
		WritableImage writableImage = new WritableImage(CANVAS_HEIGHT, CANVAS_WIDTH);
		overlay.snapshot(null, writableImage);
		RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
		ImageIO.write(renderedImage, "png", file);
	}

	public void showErrorDialog(String errorMessage) {
		Alert alert = new Alert(AlertType.ERROR, errorMessage);
		alert.showAndWait();
		// Optional<ButtonType> result = alert.showAndWait();
	}
	
	public void showCloseConfirmationDialog(String confirmMessage) {
		Alert alert = new Alert(AlertType.CONFIRMATION, confirmMessage);
		alert.setHeaderText("GraphGUI");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			System.exit(0);
		}
	}

	/**
	 * Add mouse event handlers to the canvas.
	 * 
	 * @param gestures
	 *            canvas gestures object
	 */
	public void addCanvasGestures(CanvasGestures gestures) {
		gestures.addGestures(canvas);
	}

	/**
	 * Add content menu to the canvas.
	 * 
	 * @param contextMenu
	 *            context menu to be added
	 */
	public void addContextMenus(ContextMenu canvasContext, ContextMenu vertexContext, ContextMenu edgeContext) {
		this.canvasContext = canvasContext;
		this.vertexContext = vertexContext;
		this.edgeContext = edgeContext;
	}

	/**
	 * Store vertex gestures object, permitting the binding of vertex objects to
	 * mouse events.
	 * 
	 * @param gestures
	 *            vertex gestures object
	 */
	public void addGraphGestures(VertexGestures vertexGestures, EdgeGestures edgeGestures) {
		this.vertexGestures = vertexGestures;
		this.edgeGestures = edgeGestures;
	}

	public void addMainStage(Stage mainStage) {
		this.mainStage = mainStage;
	}
	
	public void addPropertiesStage(Stage propertiesStage) {
		this.propertiesStage = propertiesStage;
	}
}
