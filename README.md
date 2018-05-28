# GraphGUI
A specialised drawing program allowing the creation of undirected graphs (i.e. collections of vertices joined by edges).

### Summary 

GraphGUI is a simple, mouse-driven drawing program with which one may create undirected graphs composed of vertices and edges. These graphs may be saved and loaded for later modification, and exported as .PNG images. 

![Screenshot](https://github.com/LiamGraham/graph-gui/blob/master/screenshots/GraphGUI%20Screenshot.png)

All actions are performed using the mouse. Vertices may be added using the left mouse button, and vertex pairs may be connected by selecting the two target vertices. Vertices may also be moved around the canvas by pressing with the left mouse button and dragging. Additional actions, including vertex and edge deletion, graph saving, exporting and loading, and mode changing, are performed using the contenxt menu (accessed by right-clicking the canvas or target vertex or edge).   

### Features

* Add and remove vertices
* Add edges between vertices
* Move vertices
* Connect selected vertex to all other vertices
* Connect all vertices
* Clear graph
* Save graph in re-loadable format
* Load saved graph
* Export graph as .PNG image
* Enable/disable graph statistics
* Align all vertices to grid
* Activate/deactivate draw mode (each new vertex is connected to previously added vertex)

### Planned Features

* Compute shortest distance between given vertices
* Change vertex and edge properties (e.g. colour, size)
* Allow creation of directed graphs
