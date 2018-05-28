package adts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class UndirectedGraph<V, E> implements Graph<V, E> {

	// Vertices of graph
	private LinkedList<Vertex<V, E>> vertices;
	// Number of vertices of graph
	private int numVertices;
	// Number of edges of graph
	private int numEdges;

	/**
	 * Creates empty undirected graph, having no edges or vertices.
	 */
	public UndirectedGraph() {
		numVertices = 0;
		numEdges = 0;
		vertices = new LinkedList<>();
	}

	@Override
	public int numVertices() {
		return numVertices;
	}

	@Override
	public List<Vertex<V, E>> vertices() {
		return new ArrayList<Vertex<V, E>>(vertices);
	}

	@Override
	public int numEdges() {
		return numEdges;
	}

	@Override
	public List<Edge<V, E>> edges() {
		Set<Edge<V, E>> edges = new HashSet<Edge<V, E>>();
		for (Vertex<V, E> vertex : vertices) {
			for (Edge<V, E> edge : vertex.incidentEdges()) {
				edges.add(edge);
			}
		}
		return new ArrayList<Edge<V, E>>(edges);
	}

	@Override
	public Edge<V, E> getEdge(Vertex<V, E> origin, Vertex<V, E> destination) {
		List<Vertex<V, E>> vertices = new ArrayList<>();
		if (origin.degree() < destination.degree()) {
			vertices.add(origin);
			vertices.add(destination);
		} else {
			vertices.add(destination);
			vertices.add(origin);
		}
		for (Edge<V, E> edge : vertices.get(0).incidentEdges()) {
			if (edge.endpoints().get(0) == vertices.get(1) || edge.endpoints().get(1) == vertices.get(1)) {
				return edge;
			}
		}
		return null;
	}
	
	@Override
	public Vertex<V, E> getVertex(V element) {
		for (Vertex<V, E> vertex: vertices) {
			if (vertex.element().equals(element)) {
				return vertex;
			}
		}
		return null;
	}

	@Override
	public Vertex<V, E>[] endVertices(Edge<V, E> edge) {
		return (Vertex<V, E>[]) edge.endpoints().toArray();
	}

	@Override
	public Vertex<V, E> opposite(Vertex<V, E> vertex, Edge<V, E> edge) throws NoSuchElementException {
		if (!vertex.incidentEdges().contains(edge)) {
			throw new NoSuchElementException();
		}
		int index = edge.endpoints().indexOf(vertex) ^ 1;
		return edge.endpoints().get(index);
	}

	@Override
	public int outDegree(Vertex<V, E> vertex) {
		return vertex.degree();
	}

	@Override
	public int inDegree(Vertex<V, E> vertex) {
		return vertex.degree();
	}

	@Override
	public List<Edge<V, E>> outgoingEdges(Vertex<V, E> vertex) {
		return vertex.incidentEdges();
	}

	@Override
	public List<Edge<V, E>> incomingEdges(Vertex<V, E> vertex) {
		return vertex.incidentEdges();
	}

	@Override
	public Vertex<V, E> insertVertex(V element) {
		Vertex<V, E> vertex = new Vertex<>(element, numVertices);
		vertices.add(vertex);
		numVertices++;
		return vertex;
	}

	@Override
	public Edge<V, E> insertEdge(Vertex<V, E> origin, Vertex<V, E> destination, E element) {
		Edge<V, E> edge = new Edge<>(element, origin, destination, origin.degree(), destination.degree());
		origin.addEdge(edge);
		destination.addEdge(edge);
		numEdges++;
		return edge;
	}

	@Override
	public Vertex<V, E> removeVertex(Vertex<V, E> vertex) {
		if (!vertices.contains(vertex)) {
			return null;
		}
		for (Edge<V, E> edge : vertex.incidentEdges()) {
			edge.endpoints().get(0).removeEdge(edge);
			edge.endpoints().get(1).removeEdge(edge);
			numEdges--;
		}
		vertices.remove(vertex);
		numVertices--;
		return vertex;
	}

	@Override
	public Edge<V, E> removeEdge(Edge<V, E> edge) {
		List<Vertex<V, E>> endpoints = edge.endpoints();
		endpoints.get(0).removeEdge(edge);
		endpoints.get(1).removeEdge(edge);
		numEdges--;
		return edge;
	}

	/**
	 * Traverses graph depth-first and modifies given map to contain vertices
	 * reachable from the given vertex and their discovery edges.
	 * 
	 * @param u
	 *            vertex reached by traversal
	 * @param known
	 *            all vertices reached in traversal
	 * @param forest
	 *            vertices reachable from u and their discovery vertices
	 * @param back
	 *            vertices reachable from u and their back edges
	 */
	public void depthFirstTraversal(Vertex<V, E> u, Set<Vertex<V, E>> known, Map<Vertex<V, E>, Edge<V, E>> forest,
			Map<Vertex<V, E>, List<Edge<V, E>>> back) {
		known.add(u);
		for (Edge<V, E> e : this.outgoingEdges(u)) {
			Vertex<V, E> v = this.opposite(u, e);
			if (!known.contains(v)) {
				forest.put(v, e);
				this.depthFirstTraversal(v, known, forest, back);
			} else {
				if (back.containsKey(v)) {
					List<Edge<V, E>> backEdges = back.get(v);
					backEdges.add(e);
					back.put(v, backEdges);
				} else {
					List<Edge<V, E>> backEdges = new ArrayList<>();
					backEdges.add(e);
					back.put(v, backEdges);
				}
			}
		}
	}

	/**
	 * Returns a string representation of the graph. The first element of each line
	 * is a vertex. The following elements are the edges incident on that vertex.
	 * 
	 * @return string representation of graph
	 */
	public String toString() {
		String output = "";
		for (Vertex<V, E> vertex : vertices) {
			output += vertex.toString() + ": " + vertex.incidentEdges().toString();
			if (vertices.indexOf(vertex) < vertices.size() - 1) {
				output += System.lineSeparator();
			}
		}
		return output;
	}

	@Override
	public void clear() {
		numVertices = 0;		
		numEdges = 0;
		vertices.clear();
	}
}
