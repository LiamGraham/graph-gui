package gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.Math;

import adts.*;

public class GraphModel {

	// Graph being displayed
	private UndirectedGraph<String, String> graph;
	// X, Y coordinates of vertices of graph
	private Map<List<Double>, Vertex<String, String>> vertexCoords;
	// X, Y coordinates of edges of graph
	private Map<Edge<String, String>, List<Double>> edgeCoords;
	// Last vertex added to graph
	private List<Vertex<String, String>> lastAdded;

	private int GRID_SIZE = 100;

	public GraphModel() {
		graph = new UndirectedGraph<>();
		vertexCoords = new HashMap<>();
		edgeCoords = new HashMap<>();
		lastAdded = new ArrayList<>();
	}

	/**
	 * Add a vertex to the graph storing the given element and having the given x, y
	 * coordinates.
	 * 
	 * @param element
	 *            element to be stored
	 * @param x
	 *            x-coordinate of vertex
	 * @param y
	 *            y-coordinate of vertex
	 */
	public Vertex<String, String> addVertex(String element, double x, double y) {
		Vertex<String, String> vertex = graph.insertVertex(element);
		List<Double> coords = new ArrayList<>();
		coords.add(x);
		coords.add(y);
		vertexCoords.put(coords, vertex);
		if (lastAdded.size() == 2) {
			lastAdded.remove(0);
		}
		lastAdded.add(vertex);
		System.out.println("New vertex at [" + x + ", " + y + "]");
		return vertex;
	}

	/**
	 * Move vertex at given initial coordinates to final coordinates.
	 * 
	 * @param initialX
	 *            initial x-coordinate of vertex to be moved
	 * @param initialY
	 *            initial y-coordinate of vertex to be moved
	 * @param finalX
	 *            x-coordinate to which vertex will be moved
	 * @param finalY
	 *            y-coordinate to which vertex will be moved
	 * @return edges incident on removed vertex
	 */
	public List<Edge<String, String>> moveVertex(double initialX, double initialY, double finalX, double finalY) {
		List<Double> initialCoords = new ArrayList<>();
		initialCoords.add(initialX);
		initialCoords.add(initialY);

		List<Double> finalCoords = new ArrayList<>();
		finalCoords.add(finalX);
		finalCoords.add(finalY);

		Vertex<String, String> vertex = vertexCoords.remove(initialCoords);
		List<Edge<String, String>> edges = graph.incomingEdges(vertex);
		// Change x, y coordinate of end of each edge incident on vertex being moved
		for (Edge<String, String> edge : edges) {
			List<Double> coords = edgeCoords.get(edge);
			// Index corresponding to x-coordinate to be changed, first + 1 is y-coordinate
			int first = 0;
			if (coords.get(2) == initialX && coords.get(3) == initialY) {
				first = 2;
			}
			coords.set(first, finalX);
			coords.set(first + 1, finalY);
		}
		vertexCoords.put(finalCoords, vertex);
		return edges;
	}

	/**
	 * Adjust the coordinates of each vertex so that they are multiples of the
	 * defined grid size.
	 */
	public void alignVerticesToGrid() {
		Map<List<Double>, Vertex<String, String>> vertexCopy = new HashMap<>(vertexCoords);
		for (List<Double> coords : vertexCopy.keySet()) {
			List<Double> g1 = new ArrayList<>();
			List<Double> g2 = new ArrayList<>();
			List<Double> g3 = new ArrayList<>();
			List<Double> g4 = new ArrayList<>();
			double lowerX = (double) (Math.floor(coords.get(0)/GRID_SIZE) * GRID_SIZE);
			double upperX = (double) (Math.ceil(coords.get(0)/GRID_SIZE) * GRID_SIZE);
			double lowerY = (double) (Math.floor(coords.get(1)/GRID_SIZE) * GRID_SIZE);
			double upperY = (double) (Math.ceil(coords.get(1)/GRID_SIZE) * GRID_SIZE);
			g1.add(lowerX);
			g1.add(lowerY);
			g2.add(upperX);
			g2.add(lowerY);
			g3.add(lowerX);
			g3.add(upperY);
			g4.add(upperX);
			g4.add(upperY);
			
			FixedProximityComparator p = new FixedProximityComparator(coords);
			Map<Double, List<Double>> proximities = new HashMap<>();
			
			proximities.put(p.distanceToFixed(g1), g1);
			proximities.put(p.distanceToFixed(g2), g2);
			proximities.put(p.distanceToFixed(g3), g3);
			proximities.put(p.distanceToFixed(g4), g4);
			
			List<Double> newCoords = proximities.get(Collections.min(proximities.keySet()));
			moveVertex(coords.get(0), coords.get(1), newCoords.get(0), newCoords.get(1));
		}
	}
	
	private List<Double> resolveAlignmentCollision(List<Double> oldCoords, List<Double> newCoords) {
		List<Double> coordsCopy = new ArrayList<>(oldCoords);
		while (vertexCoords.containsKey(newCoords) && vertexCoords.get(newCoords) != vertexCoords.get(oldCoords)) {
			double xDistance = newCoords.get(0) - oldCoords.get(0);
			double yDistance = newCoords.get(1) - oldCoords.get(1);
			System.out.println(Math.signum(xDistance) + " " + Math.signum(yDistance));
			if (xDistance > yDistance) {
				coordsCopy.set(1, coordsCopy.get(1) + (GRID_SIZE * Math.signum(xDistance)));
				newCoords.set(1, (double) (Math.round(coordsCopy.get(1) / GRID_SIZE) * GRID_SIZE));
			} else {
				coordsCopy.set(0, coordsCopy.get(0) + (GRID_SIZE * Math.signum(yDistance)));
				newCoords.set(0, (double) (Math.round(coordsCopy.get(0) / GRID_SIZE) * GRID_SIZE));
			}
		}
		return newCoords;
	}

	/**
	 * Adjust the coordinates of each vertex so that they are multiples of the
	 * defined grid size.
	 */
	@Deprecated
	public void alignAllVerticesToGrid() {
		Map<List<Double>, Vertex<String, String>> vertexCopy = new HashMap<>(vertexCoords);
		for (List<Double> coords : vertexCopy.keySet()) {
			List<Double> newCoords = new ArrayList<>();
			List<Double> coordsCopy = new ArrayList<>(coords);
			newCoords.add((double) (Math.round(coords.get(0) / GRID_SIZE) * GRID_SIZE));
			newCoords.add((double) (Math.round(coords.get(1) / GRID_SIZE) * GRID_SIZE));
			while (vertexCoords.containsKey(newCoords) && vertexCoords.get(newCoords) != vertexCoords.get(coords)) {
				double xDistance = newCoords.get(0) - coords.get(0);
				double yDistance = newCoords.get(1) - coords.get(1);
				System.out.println(Math.signum(xDistance) + " " + Math.signum(yDistance));
				if (xDistance > yDistance) {
					coordsCopy.set(1, coordsCopy.get(1) + (GRID_SIZE * Math.signum(xDistance)));
					newCoords.set(1, (double) (Math.round(coordsCopy.get(1) / GRID_SIZE) * GRID_SIZE));
				} else {
					coordsCopy.set(0, coordsCopy.get(0) + (GRID_SIZE * Math.signum(yDistance)));
					newCoords.set(0, (double) (Math.round(coordsCopy.get(0) / GRID_SIZE) * GRID_SIZE));
				}
			}
			moveVertex(coords.get(0), coords.get(1), newCoords.get(0), newCoords.get(1));
		}
	}

	/**
	 * Create a new edge with the most recently added vertex as its origin and the
	 * previously added vertex as its destination.
	 */
	public void connectLastAdded() {
		if (lastAdded.size() != 2) {
			return;
		}
		List<Double> initialCoords = null;
		for (List<Double> coords : vertexCoords.keySet()) {
			if (lastAdded.contains(vertexCoords.get(coords))) {
				if (initialCoords == null) {
					initialCoords = coords;
				} else {
					addEdge(initialCoords, coords, "NONE");
					break;
				}
			}
		}
	}

	/**
	 * Returns the coordinates of the previously added vertex.
	 * 
	 * @return coords of the previously added vertex
	 */
	public List<Double> getLastAddedCoords() {
		if (lastAdded.isEmpty()) {
			return null;
		}
		for (List<Double> coords : vertexCoords.keySet()) {
			if (vertexCoords.get(coords).equals(lastAdded.get(lastAdded.size() - 1))) {
				return coords;
			}
		}
		return null;
	}

	/**
	 * Clear list of last added vertices.
	 */
	public void clearLastAdded() {
		lastAdded.clear();
	}

	/**
	 * Returns mapping of vertices to their corresponding x, y coordinates.
	 * 
	 * @return mapping of vertices to x, y coordinates
	 */
	public Map<List<Double>, Vertex<String, String>> getVertices() {
		return vertexCoords;
	}

	/**
	 * Returns mapping of edges to their corresponding x, y coordinates.
	 * 
	 * @return mapping of edges to x, y coordinates
	 */
	public Map<Edge<String, String>, List<Double>> getEdges() {
		return edgeCoords;
	}

	/**
	 * Add an edge to the graph storing given element and having the given origin
	 * and destination vertices. Parallel edges are not permitted.
	 * 
	 * @param origin
	 *            vertex of origin
	 * @param destination
	 *            vertex of destination
	 * @param element
	 *            element to be stored
	 */
	public void addEdge(List<Double> originCoords, List<Double> destCoords, String element) {
		// Prevent creation of edge between two already connected vertices
		if ((originCoords == null || destCoords == null)
				|| graph.getEdge(vertexCoords.get(originCoords), vertexCoords.get(destCoords)) != null) {
			return;
		}
		System.out.println("New edge from " + originCoords + " to " + destCoords);
		Edge<String, String> edge = graph.insertEdge(vertexCoords.get(originCoords), vertexCoords.get(destCoords),
				element);
		List<Double> coords = new ArrayList<>();
		coords.addAll(originCoords);
		coords.addAll(destCoords);
		edgeCoords.put(edge, coords);
	}

	/**
	 * Delete vertex at given x, y coordinates.
	 * 
	 * @param x
	 *            x-coordinate of vertex to be deleted
	 * @param y
	 *            y-coordinate of vertex to be deleted
	 */
	public void deleteVertex(double x, double y) {
		List<Double> coords = new ArrayList<>();
		coords.add(x);
		coords.add(y);
		Vertex<String, String> vertex = vertexCoords.remove(coords);
		List<Edge<String, String>> edges = graph.incomingEdges(vertex);
		for (Edge<String, String> edge : edges) {
			edgeCoords.remove(edge);
		}
		lastAdded.remove(vertex);
		graph.removeVertex(vertex);
	}

	/**
	 * Delete edge at given start and end x, y coordinates.
	 * 
	 * @param startX
	 *            start x-coordinate of edge to be deleted
	 * @param startY
	 *            start y-coordinate of edge to be deleted
	 * @param endX
	 *            end x-coordinate of edge to be deleted
	 * @param endY
	 *            end y-coordinate of edge to be deleted
	 */
	public void deleteEdge(List<Double> coords) {
		for (Edge<String, String> edge : edgeCoords.keySet()) {
			if (edgeCoords.get(edge).equals(coords)) {
				edgeCoords.remove(edge);
				graph.removeEdge(edge);
				break;
			}
		}
	}

	/**
	 * Returns true if the graph is connected, that is, every unordered pair of
	 * vertices (x, y) has a path that leads from x to y.
	 * 
	 * @return true if graph is connected
	 */
	public boolean graphIsConnected() {
		if (vertexCoords.isEmpty()) {
			return false;
		}
		Map<Vertex<String, String>, Edge<String, String>> forest = new HashMap<>();
		Map<Vertex<String, String>, List<Edge<String, String>>> back = new HashMap<>();
		Set<Vertex<String, String>> known = new HashSet<>();
		Vertex<String, String> vertex = vertexCoords.values().iterator().next();
		graph.depthFirstTraversal(vertex, known, forest, back);
		if (forest.size() == vertexCoords.size() - 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Connect each vertex of the graph to every other vertex, producing a connected
	 * graph.
	 */
	public void connectAllVertices() {
		for (List<Double> c1 : vertexCoords.keySet()) {
			for (List<Double> c2 : vertexCoords.keySet()) {
				if (c1 != c2) {
					this.addEdge(c1, c2, "NONE");
				}
			}
		}
	}

	/**
	 * Connect each of the given vertices to every other given vertex.
	 * 
	 * @param vertices
	 *            vertices to be connected
	 */
	public void connectVertices(ArrayList<ArrayList<Double>> vertices) {
		for (List<Double> c1 : vertexCoords.keySet()) {
			for (List<Double> c2 : vertexCoords.keySet()) {
				if (c1 != c2) {
					this.addEdge(c1, c2, "NONE");
				}
			}
		}
	}

	/**
	 * Connect each of the given in a line, where each vertex has, at most, a degree
	 * of two (excluding connections to any vertices not included in the given
	 * list), and is connected to the two closest vertices.
	 * 
	 * @param vertices
	 *            vertices to be connected
	 */
	public void connectVerticesInSequence(ArrayList<ArrayList<Double>> vertices) {
		ArrayList<Double> minX = vertices.get(0);
		ArrayList<Double> minY = minX;
		ArrayList<Double> maxX = minX;
		ArrayList<Double> maxY = minX;

		ArrayList<Double> first = null;

		for (ArrayList<Double> c : vertices) {
			if (c.get(0) < minX.get(0)) {
				minX = c;
			}
			if (c.get(1) < minY.get(1)) {
				minY = c;
			}
			if (c.get(0) > maxX.get(0)) {
				maxX = c;
			}
			if (c.get(1) > maxY.get(1)) {
				maxY = c;
			}
		}
		if ((maxX.get(0) - minX.get(0)) > (maxY.get(1) - minY.get(1))) {
			first = minX;
		} else {
			first = minY;
		}

		ArrayList<ArrayList<Double>> vCopy = (ArrayList<ArrayList<Double>>) vertices.clone();
		for (List<Double> c1 : vertices) {
			if (vertices.indexOf(c1) == (vertices.size() - 1)) {
				break;
			}
			vCopy.sort(new FixedProximityComparator(c1));
			System.out.println("Vertices: " + c1 + " " + vCopy.get(1));
			for (ArrayList<Double> c2 : vCopy) {
				if (c1 != c2 && graph.getEdge(vertexCoords.get(c1), vertexCoords.get(c2)) == null) {
					this.addEdge(c1, c2, "NONE");
					break;
				}
			}
		}

		/*
		 * vertices.sort(new FixedProximityComparator(first)); Map<List<Double>, Double>
		 * proximity = new HashMap<>(); FixedProximityComparator p = new
		 * FixedProximityComparator(first); for (ArrayList<Double> v : vertices) {
		 * proximity.put(v, p.distanceToFixed(v)); } System.out.println("Proximities: "
		 * + proximity); List<Double> previous = null; for (List<Double> c : vertices) {
		 * if (previous != null) { this.addEdge(previous, c, "NONE"); } previous = c; }
		 */
	}

	/**
	 * Connect the given vertex to the vertex closest in proximity.
	 * 
	 * @param vertex
	 *            vertex to be connected
	 */
	public void connectToNearest(List<Double> vertex) {
		ArrayList<ArrayList<Double>> vertices = new ArrayList<ArrayList<Double>>();
		for (List<Double> c : vertexCoords.keySet()) {
			if (!c.equals(vertex)) {
				vertices.add((ArrayList<Double>) c);
			}
		}
		if (!vertices.isEmpty()) {
			vertices.sort(new FixedProximityComparator(vertex));
			for (ArrayList<Double> c : vertices) {
				if (graph.getEdge(vertexCoords.get(vertex), vertexCoords.get(c)) == null) {
					this.addEdge(vertex, c, "NONE");
					break;
				}
			}
		}
	}

	/**
	 * Remove all edges of graph.
	 */
	public void removeAllEdges() {
		for (Edge<String, String> edge : edgeCoords.keySet()) {
			graph.removeEdge(edge);
		}
		edgeCoords.clear();
	}

	/**
	 * Connect vertex with given x, y coordinates to all other vertices.
	 * 
	 * @param coords
	 */
	public void connectVertex(List<Double> coords) {
		for (List<Double> c : vertexCoords.keySet()) {
			if (!c.equals(coords)) {
				this.addEdge(coords, c, "NONE");
			}
		}
	}

	/**
	 * Remove all edges incident on the vertex with the given x, y coordinates.
	 * 
	 * @param coords
	 *            coordinates of vertex for which incident edges will be removed
	 */
	public void disconnectVertex(List<Double> coords) {
		Vertex<String, String> vertex = vertexCoords.get(coords);
		List<Edge<String, String>> incidentEdges = graph.incomingEdges(vertex);
		for (Edge<String, String> edge : incidentEdges) {
			graph.removeEdge(edge);
			edgeCoords.remove(edge);
		}
	}

	public ArrayList<ArrayList<Double>> getOppositeVertices(List<Double> startCoords) {
		Vertex<String, String> vertex = vertexCoords.get(startCoords);
		List<Edge<String, String>> incident = graph.incomingEdges(vertex);
		List<Vertex<String, String>> opposite = new ArrayList<>();
		for (Edge<String, String> edge : incident) {
			opposite.add(graph.opposite(vertex, edge));
		}
		ArrayList<ArrayList<Double>> oppositeCoords = new ArrayList<ArrayList<Double>>();
		for (List<Double> coords : vertexCoords.keySet()) {
			if (opposite.contains(vertexCoords.get(coords))) {
				oppositeCoords.add((ArrayList) coords);
			}
		}
		return oppositeCoords;
	}

	/**
	 * Delete all vertices and edges of graph.
	 */
	public void clearGraph() {
		graph.clear();
		vertexCoords.clear();
		edgeCoords.clear();
	}

	/**
	 * Returns true if the graph is complete, that is, each pair of vertices of the
	 * graph is joined by an edge.
	 * 
	 * @return true if graph is complete
	 */
	public boolean graphIsComplete() {
		if (vertexCoords.isEmpty()) {
			return false;
		}
		for (Vertex<String, String> v1 : vertexCoords.values()) {
			for (Vertex<String, String> v2 : vertexCoords.values()) {
				if (v1 != v2 && graph.getEdge(v1, v2) == null) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Load vertices and edge from text file.
	 * 
	 * @param fileName
	 *            name of file from which edges and vertices will be loaded
	 * @throws IOException
	 */
	public void loadGraphFromFile(File file) throws IOException {
		if (file == null) {
			return;
		}
		graph = new UndirectedGraph<>();
		vertexCoords = new HashMap<>();
		edgeCoords = new HashMap<>();

		int stage = 0;
		List<Double> newCoords = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for (String line; (line = br.readLine()) != null;) {
				if (line.equals("")) {
					stage++;
					continue;
				}
				String[] components = line.split(" ");
				String element = components[0];
				List<Double> values = new ArrayList<>();
				values.add(Double.parseDouble(components[1]));
				values.add(Double.parseDouble(components[2]));

				if (stage == 0) {
					addVertex(element, values.get(0), values.get(1));
					newCoords.addAll(values);
				} else {
					int originIndex = values.get(0).intValue();
					int destIndex = values.get(1).intValue();
					addEdge(newCoords.subList(originIndex * 2, (originIndex * 2) + 2),
							newCoords.subList(destIndex * 2, (destIndex * 2) + 2), element);
				}
			}
		} catch (IOException | NumberFormatException e) {
			throw new IOException("Error reading file " + file.getName());
		}
	}

	/**
	 * Save graph to text file.
	 * 
	 * @param file
	 *            file to which graph will be saved
	 * @throws IOException
	 */
	public void saveGraphToFile(File file) throws IOException {
		List<Vertex<String, String>> vertices = new ArrayList<>();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			for (List<Double> coords : vertexCoords.keySet()) {
				Vertex<String, String> vertex = vertexCoords.get(coords);
				vertices.add(vertex);
				String line = vertex.element() + " " + coords.get(0) + " " + coords.get(1);
				bw.write(line);
				bw.newLine();
			}
			bw.newLine();
			List<Edge<String, String>> edges = graph.edges();
			for (Edge<String, String> edge : edges) {
				List<Vertex<String, String>> endpoints = edge.endpoints();
				String line = edge.element() + " " + vertices.indexOf(endpoints.get(0)) + " "
						+ vertices.indexOf(endpoints.get(1));
				bw.write(line);
				bw.newLine();
			}
		} catch (IOException e) {
			throw new IOException("Error writing to file " + file.getName());
		}

	}

	private double distanceBetweenPoints(List<Double> p1, List<Double> p2) {
		return Math.sqrt(Math.pow((p1.get(0) - p2.get(0)), 2) + Math.pow((p1.get(1) - p2.get(1)), 2));
	}

	private class FixedProximityComparator implements Comparator<List<Double>> {

		// Fixed point to which points will be compared according to proximity
		List<Double> fixedPoint;

		public FixedProximityComparator(List<Double> fixedPoint) {
			this.fixedPoint = fixedPoint;
		}

		@Override
		public int compare(List<Double> arg0, List<Double> arg1) {
			return (int) (distanceToFixed(arg0) - distanceToFixed(arg1));
		}

		public double distanceToFixed(List<Double> point) {
			return Math.sqrt(
					Math.pow((fixedPoint.get(0) - point.get(0)), 2) + Math.pow((fixedPoint.get(1) - point.get(1)), 2));
		}

	}
}
