package kth.csc.inda;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An undirected graph with a fixed number of vertices implemented using
 * adjacency lists. Space complexity is O(V + E) where V is the number
 * of vertices and E the number of edges.
 * 
 * @author [Name]
 * @version [Date]
 * source: pastebin
 */
public class ListGraph implements UndirectedGraph
{
    /** Number of vertices in the graph. */
    private final int numVertices;

    /** Number of edges in the graph. */
    private int numEdges;

    /**
     * All vertices adjacent to v are stored in adjacentVertices[v].
     * No set is allocated if there are no adjacent vertices.
     */
    private final Set<Integer>[] adjacentVertices;

    /**
     * Edge costs are stored in a hash map. The key is an
     * Edge(v, w)-object and the cost is an Integer object.
     */
    private final Map<Edge, Integer> edgeCosts;

    /**
     * Constructs a ListGraph with v vertices and no edges.
     * Time complexity: O(v)
     * 
     * @throws IllegalArgumentException if v < 0
     */
    public ListGraph(int v)
    {
        numVertices = v;
        numEdges = 0;
        
        // The array will contain only Set<Integer> instances created
        // in addEdge(). This is sufficient to ensure type safety.
        @SuppressWarnings("unchecked")
        Set<Integer>[] a = new HashSet[numVertices];
        adjacentVertices = a;
        
        edgeCosts = new HashMap<Edge, Integer>();
    }

    /** An undirected edge between v and w. */
    private static class Edge
    {
        // Invariant: v <= w
        final int v;
        final int w;

        Edge(int v, int w) 
        {
            if (v <= w)
            {
                this.v = v;
                this.w = w;
            }
            else 
            {
                this.v = w;
                this.w = v;
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof Edge))
                return false;
            Edge e = (Edge) o;
            return v == e.v && w == e.w;
        }

        @Override
        public int hashCode() 
        {
            return 31*v + w;
        }

        @Override
        public String toString()
        {
            return "(" + v + ", " + w + ")";
        }
    }

    /**
     * {@inheritDoc UndirectedGraph}
     */
    @Override
    public int numVertices() 
    {
        return numVertices;
    }

    /**
     * {@inheritDoc UndirectedGraph}
     */
    @Override
    public int numEdges()
    { 
        return numEdges;
    }

    /**
     * {@inheritDoc UndirectedGraph}
     */
    @Override
    public int degree(int v) throws IllegalArgumentException 
    {
        return adjacentVertices[v].size();
    }

    /**
     * {@inheritDoc UndirectedGraph}
     */
    @Override
    public VertexIterator adjacentVertices(int v)
    { // TODO
        return new adjacentIterator(v);
    }
    
    private class adjacentIterator implements VertexIterator
    {
    	private final int vertex;
    	private int nextPos;
    	
    	adjacentIterator(int v)
    	{
    		vertex = v;
    		nextPos = -1;
    	}
    	
        private void findNext() 
        {
            nextPos++;
            while (nextPos < numVertices && ! edgeCosts.isEmpty())
                nextPos++;
        }
        
		@Override
		public boolean hasNext()
		{
            return nextPos < numVertices;
		}

		@Override
		public int next() throws NoSuchElementException
		{
            int pos = nextPos;
            if (pos < numVertices) {
                findNext();
                return pos;
            }
            throw new NoSuchElementException(
            "This iterator has no more elements.");
		}
    	
    }

    /**
     * {@inheritDoc UndirectedGraph}
     */
    @Override
    public boolean areAdjacent(int v, int w) 
    {
    	if(adjacentVertices[v].contains(w))
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }

    /**
     * {@inheritDoc UndirectedGraph}
     */
    @Override
    public int edgeCost(int v, int w) throws IllegalArgumentException
    {
    	if(areAdjacent(v, w))
    	{
    		Edge e = new Edge(v, w);
    		return edgeCosts.get(e);
    	}
    	else
    	{
    		return -1;
    	}
    }

    /**
     * {@inheritDoc UndirectedGraph}
     */
    @Override
    public void addEdge(int v, int w)
    {
    	adjacentVertices[v].add(w);
    	adjacentVertices[w].add(v);
    	numEdges++;
    }

    /**
     * {@inheritDoc UndirectedGraph}
     */
    @Override
    public void addEdge(int v, int w, int c)
    {
    	Edge e = new Edge(v, w);
    	adjacentVertices[v].add(w);
    	adjacentVertices[w].add(v);
    	edgeCosts.put(e, c);
    	numEdges++;
    }

    /**
     * {@inheritDoc UndirectedGraph}
     */
    @Override
    public void removeEdge(int v, int w)
    {
    	Edge e = new Edge(v, w);
    	adjacentVertices[v].remove(w);
    	adjacentVertices[w].remove(v);
    	edgeCosts.remove(e);
    	numEdges--;
    }

    /**
     * Returns a string representation of this graph.
     * 
     * @return a String representation of this graph
     */
    @Override
    public String toString()
    { // TODO
        return null;
    }
}
