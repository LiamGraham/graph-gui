package adts;

import java.util.ArrayList;
import java.util.List;

public class Vertex<V, E> {

	// Element stored by vertex
	private V element;
	// Sequence of edges incident on vertex
	private List<Edge<V, E>> incidentEdges;
	// Position in vertex sequence of graph containing this vertex
	private int position;
	// Number of edges incident on this vertex
	private int degree;

	/**
	 * Creates new vertex storing the given element and having given position in
	 * vertex sequence of the graph containing this vertex.
	 * 
	 * @param element
	 *            element to be stored
	 * @param position
	 *            position in graph vertex sequence
	 */
	public Vertex(V element, int position) {
		this.element = element;
		this.position = position;
		incidentEdges = new ArrayList<Edge<V, E>>();
		degree = 0;
	}

	public V element() {
		return element;
	}

	public ArrayList<Edge<V, E>> incidentEdges() {
		return new ArrayList<Edge<V, E>>(incidentEdges);
	}

	public int position() {
		return position;
	}
	
	public void addEdge(Edge<V, E> edge) {
		incidentEdges.add(edge);
		degree++;
	}
	
	public boolean removeEdge(Edge<V, E> edge) {
		return incidentEdges.remove(edge);
	}
	
	public int degree() {
		return degree;
	}
	
	public String toString() {
		return "Vertex[" + element.toString() + "]";
	}
}
