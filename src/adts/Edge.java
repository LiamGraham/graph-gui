package adts;

import java.util.ArrayList;
import java.util.List;

public class Edge<V, E> {

	// Element stored by edge
	private E element;
	// End vertices of this edge, first vertex is origin and second destination
	private List<Vertex<V, E>> endpoints;
	// Position in incidence sequence of origin vertex
	private int[] positions;

	/**
	 * Creates new empty Edge, storing the given element and having the given origin
	 * and destination vertices and the given positions in the origin vertex and
	 * destination vertex incidence sequences.
	 * 
	 * @param element
	 *            element to be stored
	 * @param origin
	 *            vertex of origin
	 * @param destination
	 *            vertex of destination
	 * @param originPosition
	 *            position in origin vertex incidence sequence
	 * @param destPosition
	 *            position in destination vertex incidence sequence
	 */
	public Edge(E element, Vertex<V, E> origin, Vertex<V, E> destination, int originPosition, int destPosition) {
		this.element = element;

		positions = new int[2];
		positions[0] = originPosition;
		positions[1] = destPosition;

		endpoints = new ArrayList<Vertex<V, E>>();
		endpoints.add(origin);
		endpoints.add(destination);
	}

	/**
	 * Returns element contained by this edge.
	 * 
	 * @return element contained by edge
	 */
	public E element() {
		return element;
	}

	/**
	 * Returns origin vertex of this edge.
	 * 
	 * @return origin vertex of this edge
	 */
	public List<Vertex<V, E>> endpoints() {
		return new ArrayList<Vertex<V, E>>(endpoints);
	}

	/**
	 * Returns position in incidence sequence of origin vertex.
	 * 
	 * @return position in incidence sequence of origin vertex
	 */
	public int[] incidentPositions() {
		return positions.clone();
	}

	/**
	 * Remove given end vertex from this edge.
	 * 
	 * @param vertex vertex to be removed
	 * @return true if vertex is endpoint of this edge ; otherwise false
	 */
	public boolean removeVertex(Vertex<V, E> vertex) {
		return endpoints.remove(vertex);
	}

	public String toString() {
		return "Edge[" + element.toString() + ", " + endpoints.get(0).toString() + ", " + endpoints.get(1).toString() + "]";
	}

}
