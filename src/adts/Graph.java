package adts;

import java.util.NoSuchElementException;

public interface Graph<V, E> {

	/**
	 * Returns the number of vertices of the graph.
	 * 
	 * @return number of vertices of the graph
	 */
	int numVertices();

	/**
	 * Returns an iterable of vertices of the graph.
	 * 
	 * @return the list of vertices of the graph
	 */
	Iterable<Vertex<V, E>> vertices();

	/**
	 * Returns the number of edges of the graph.
	 * 
	 * @return the number of edges of the graph.
	 */
	int numEdges();

	/**
	 * Returns an iterable of edges of the graph.
	 * 
	 * @return a list of edges of the graph
	 */
	Iterable<Edge<V, E>> edges();

	/**
	 * Returns the edge from given origin vertex to given destination vertex, if one
	 * exists; otherwise return null. getEdge(u, v) indistinct from getEdge(v, u)
	 * for undirected graph.
	 * 
	 * @param origin
	 *            vertex of origin
	 * @param destination
	 *            vertex of destination
	 * @return the edge from given origin vertex to given destination vertex
	 */
	Edge<V, E> getEdge(Vertex<V, E> origin, Vertex<V, E> destination);

	/**
	 * Returns the first vertex storing the given element if vertex is in graph;
	 * otherwise null.
	 * 
	 * @param element
	 *            element of vertex to be retrieved
	 * @return vertex storing given element if present; otherwise null.
	 */
	Vertex<V, E> getVertex(V element);

	/**
	 * Returns an array containing the two endpoints vertices of the given edge. For
	 * a directed graph, the first vertex is the origin and the second the
	 * destination.
	 * 
	 * @param edge
	 *            edge whose endpoints will be returned
	 * @return array containing endpoints of the given edge in origin-destination
	 *         order
	 */
	Vertex<V, E>[] endVertices(Edge<V, E> edge);

	/**
	 * Returns the other vertex of the given edge incident to the given vertex.
	 * 
	 * @param vertex
	 *            initial vertex
	 * @param edge
	 *            edge incident to given vertex
	 * @return other vertex of edge e incident to vertex v
	 */
	Vertex<V, E> opposite(Vertex<V, E> vertex, Edge<V, E> edge) throws NoSuchElementException;

	/**
	 * Returns number of outgoing edges from given vertex. Indistinct from
	 * inDegree(v) for undirected graphs.
	 * 
	 * @param vertex
	 *            vertex for which number of outgoing edges will be returned
	 * @return number of outgoing edges from given vertex
	 */
	int outDegree(Vertex<V, E> vertex);

	/**
	 * Returns number of incoming edges to given vertex. Indistinct from
	 * outDegree(v) for undirected graphs.
	 * 
	 * @param vertex
	 *            vertex for which number of incoming edges will be returned
	 * @return number of incoming edges to given vertex
	 */
	int inDegree(Vertex<V, E> vertex);

	/**
	 * Returns an iterable of all outgoing edges from the given vertex.
	 * 
	 * @param vertex
	 *            vertex whose outgoing edges will be returned
	 * @return iterable of all outgoing edges from given vertex
	 */
	Iterable<Edge<V, E>> outgoingEdges(Vertex<V, E> vertex);

	/**
	 * Returns an iterable of all incoming edges to the given vertex.
	 * 
	 * @param vertex
	 *            vertex whose incoming edges will be returned
	 * @return iterable of all incoming edges to given vertex
	 */
	Iterable<Edge<V, E>> incomingEdges(Vertex<V, E> vertex);

	/**
	 * Creates and returns a new Vertex storing the given element.
	 * 
	 * @param element
	 *            element to be stored in new vertex
	 * @return newly created vertex storing the given element
	 */
	Vertex<V, E> insertVertex(V element);

	/**
	 * Creates and returns a new Edge storing the given element and having the given
	 * origin and destination vertices.
	 * 
	 * @param origin
	 *            vertex of origin of new edge
	 * @param destination
	 *            vertex of destination of new edge
	 * @param element
	 *            element to be stored in new edge
	 * @return newly created edge storing the given element and having the given
	 *         origin and destination vertices
	 */
	Edge<V, E> insertEdge(Vertex<V, E> origin, Vertex<V, E> destination, E element);

	/**
	 * Removes and returns the given vertex.
	 * 
	 * @param vertex
	 *            vertex to be removed
	 * @return removed vertex if present; otherwise null
	 */
	Vertex<V, E> removeVertex(Vertex<V, E> vertex);

	/**
	 * Removes and returns the given edge.
	 * 
	 * @param edge
	 *            edge to be removed
	 * @return removed edge if present; otherwise null
	 */
	Edge<V, E> removeEdge(Edge<V, E> edge);

	/**
	 * Remove all vertices and edges of graph.
	 */
	void clear();
}
