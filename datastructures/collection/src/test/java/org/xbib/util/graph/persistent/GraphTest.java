package org.xbib.util.graph.persistent;

import static org.xbib.util.graph.persistent.Assert2.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xbib.util.graph.persistent.impl.PersistentHashGraphFactory;
import org.xbib.util.graph.persistent.impl.PersistentTreeGraphFactory;
import org.xbib.util.graph.persistent.internal.InternalGraph;

@RunWith(Parameterized.class)
public class GraphTest {

    private final InternalGraph<String, String> emptyGraph;
    
    public GraphTest(GraphFactory<String, String> factory) {
        this.emptyGraph = (InternalGraph<String, String>) factory.of();
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
            new Object[][] {
                    { new PersistentHashGraphFactory<String, String>() },
                    { new PersistentTreeGraphFactory<String, String>() }
            }
        );
    }

    @Test public void empty() {
        final Graph<String, String> graph = this.emptyGraph;
        assertTrue(graph.isEmpty());
    }
    
    @Test public void getNonExistantNode() throws IllegalStateException {
        final Graph<String, String> graph = this.emptyGraph;
        assertNull(graph.get(0));
    }


    @Test public void addNodes() throws IllegalStateException {
        final int node1 = 0;
        final int node2 = 1;
        final String label1 = "node-label-1";
        final String label2 = "node-label-2";

        final Graph<String, String> graph1 = this.emptyGraph;
        final Graph<String, String> graph2 = graph1.addNode(node1, label1);
        final Graph<String, String> graph3 = graph2.addNode(node2, label2);

        assertTrue(graph1.isEmpty());
        assertNotContains(node1, graph1.nodeIds());
        assertNotContains(node2, graph1.nodeIds());
        assertFalse(graph1.containsNode(node1));
        assertFalse(graph1.containsNode(node1, label1));
        assertFalse(graph1.containsNode(node1, label2));
        assertFalse(graph1.containsNode(node2));
        assertFalse(graph1.containsNode(node2, label1));
        assertFalse(graph1.containsNode(node2, label2));
        assertNull(graph1.get(node1));
        assertNull(graph1.get(node2));

        assertFalse(graph2.isEmpty());
        assertContains(node1, graph2.nodeIds());
        assertNotContains(node2, graph2.nodeIds());
        assertTrue(graph2.containsNode(node1));
        assertTrue(graph2.containsNode(node1, label1));
        assertFalse(graph2.containsNode(node1, label2));
        assertFalse(graph2.containsNode(node2));
        assertFalse(graph2.containsNode(node2, label1));
        assertFalse(graph2.containsNode(node2, label2));
        final GraphNode<String, String> node1Context1 = graph2.get(node1);
        assertNotNull(node1Context1);
        assertContains(node1Context1, graph2.nodes());
        assertNull(graph1.get(node2));
        assertEquals(node1, node1Context1.node());
        assertEquals(label1, node1Context1.label());

        assertFalse(graph3.isEmpty());
        assertContains(node1, graph3.nodeIds());
        assertContains(node2, graph3.nodeIds());
        assertTrue(graph3.containsNode(node1));
        assertTrue(graph3.containsNode(node1, label1));
        assertFalse(graph3.containsNode(node1, label2));
        final String bogusLabel = "bogus";
        assertFalse(graph3.containsNode(node1, bogusLabel));
        assertTrue(graph3.containsNode(node2));
        assertFalse(graph3.containsNode(node2, label1));
        assertTrue(graph3.containsNode(node2, label2));
        assertFalse(graph3.containsNode(node2, bogusLabel));
        final GraphNode<String, String> node1Context2 = graph3.get(node1);
        final GraphNode<String, String> node2Context = graph3.get(node2);
        assertNotNull(node1Context2);
        assertContains(node1Context2, graph3.nodes());
        assertNotNull(node2Context);
        assertContains(node2Context, graph3.nodes());
        assertEquals(label1, node1Context2.label());
        assertEquals(node1, node1Context2.node());
        assertEquals(label2, node2Context.label());
        assertEquals(node2, node2Context.node());
        assertEquals(node1Context1, node1Context2);
        assertContains(node1Context1, graph3.nodes());
    }

    @Test(expected = IllegalStateException.class) public void addAlreadyExistingNode() throws IllegalStateException {
        final int node = 0;

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        graph.addNode(node, null);
    }

    @Test public void addNullNodeLabel() throws IllegalStateException {
        final int node = 0;

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        final GraphNode<String, String> context = graph.get(node);
        assertNotNull(context);
        assertNull(context.label());
    }

    @Test public void removeNodes() throws IllegalStateException {
        final int node1 = 0;
        final int node2 = 1;
        final String nodeLabel1 = null;
        final String nodeLabel2 = "node-label-2";
        final String edgeLabel1 = "edge-label-1";
        final String edgeLabel2 = "edge-label-2";

        final Graph<String, String> graph1 = this.emptyGraph;
        final Graph<String, String> graph2 = graph1.addNode(node1, nodeLabel1);
        final Graph<String, String> graph3 = graph2.addNode(node2, nodeLabel2);
        final Graph<String, String> graph4 = graph3.addEdge(node1, node2, edgeLabel1);
        final Graph<String, String> graph5 = graph4.addEdge(node2, node1, edgeLabel2);
        final Graph<String, String> graph6 = graph5.removeNode(node1);
        final Graph<String, String> graph7 = graph6.removeNode(node2);

        assertFalse(graph1.containsNode(node1, nodeLabel1));
        assertFalse(graph1.containsNode(node2, nodeLabel2));
        assertFalse(graph1.containsEdge(node1, node2, edgeLabel1));
        assertFalse(graph1.containsEdge(node2, node1, edgeLabel2));

        assertTrue(graph2.containsNode(node1, nodeLabel1));
        assertFalse(graph2.containsNode(node2, nodeLabel2));
        assertFalse(graph2.containsEdge(node1, node2, edgeLabel1));
        assertFalse(graph2.containsEdge(node2, node1, edgeLabel2));

        assertTrue(graph3.containsNode(node1, nodeLabel1));
        assertTrue(graph3.containsNode(node2, nodeLabel2));
        assertFalse(graph3.containsEdge(node1, node2, edgeLabel1));
        assertFalse(graph3.containsEdge(node2, node1, edgeLabel2));

        assertTrue(graph4.containsNode(node1, nodeLabel1));
        assertTrue(graph4.containsNode(node2, nodeLabel2));
        assertTrue(graph4.containsEdge(node1, node2, edgeLabel1));
        assertFalse(graph4.containsEdge(node2, node1, edgeLabel2));

        assertTrue(graph5.containsNode(node1, nodeLabel1));
        assertTrue(graph5.containsNode(node2, nodeLabel2));
        assertTrue(graph5.containsEdge(node1, node2, edgeLabel1));
        assertTrue(graph5.containsEdge(node2, node1, edgeLabel2));

        assertFalse(graph6.containsNode(node1, nodeLabel1));
        assertTrue(graph6.containsNode(node2, nodeLabel2));

        assertFalse(graph7.containsNode(node1, nodeLabel1));
        assertFalse(graph7.containsNode(node2, nodeLabel2));

        assertEquals(graph1, graph7);
    }

    @Test(expected = IllegalStateException.class) public void removeNonExistantNode() throws IllegalStateException {
        this.emptyGraph.removeNode(0);
    }


    @Test public void addEdges() throws IllegalStateException {
        final int node1 = 0;
        final int node2 = 1;
        final String label1 = "edge-label-1";
        final String label2 = "edge-label-2";

        final Graph<String, String> graph1 = this.emptyGraph;
        final Graph<String, String> graph2 = graph1.addNode(node1, null);
        final Graph<String, String> graph3 = graph2.addNode(node2, null);
        final Graph<String, String> graph4 = graph3.addEdge(node1, node2, label1);
        final Graph<String, String> graph5 = graph4.addEdge(node2, node1, label2);

        assertFalse(graph1.containsEdge(node1, node2));
        assertFalse(graph1.containsEdge(node2, node1));
        assertFalse(graph1.containsEdge(node1, node2, label1));
        assertFalse(graph1.containsEdge(node2, node1, label2));

        assertFalse(graph2.containsEdge(node1, node2));
        assertFalse(graph2.containsEdge(node2, node1));
        assertFalse(graph2.containsEdge(node1, node2, label1));
        assertFalse(graph2.containsEdge(node2, node1, label2));

        assertFalse(graph3.containsEdge(node1, node2));
        assertFalse(graph3.containsEdge(node2, node1));
        assertFalse(graph3.containsEdge(node1, node2, label1));
        assertFalse(graph3.containsEdge(node2, node1, label2));

        assertTrue(graph4.containsEdge(node1, node2));
        assertFalse(graph4.containsEdge(node2, node1));
        assertTrue(graph4.containsEdge(node1, node2, label1));
        assertFalse(graph4.containsEdge(node1, node2, label2));
        assertFalse(graph4.containsEdge(node2, node1, label1));
        assertFalse(graph4.containsEdge(node2, node1, label2));
        final String bogusLabel = "bogus";
        assertFalse(graph4.containsEdge(node1, node2, bogusLabel));
        assertFalse(graph4.containsEdge(node2, node1, bogusLabel));
        final GraphNode<String, String> node1Context1 = graph4.get(node1);
        final GraphNode<String, String> node2Context1 = graph4.get(node2);
        assertNotNull(node1Context1);
        assertNotNull(node2Context1);
        assertEmpty(node1Context1.incoming().nodes());
        assertContains(node2, node1Context1.outgoing().nodes());
        assertNotContains(node1, node1Context1.outgoing().nodes());
        assertContains(node1, node2Context1.incoming().nodes());
        assertNotContains(node2, node2Context1.incoming().nodes());
        assertEmpty(node2Context1.outgoing().nodes());

        assertTrue(graph5.containsEdge(node1, node2));
        assertTrue(graph5.containsEdge(node2, node1));
        assertTrue(graph5.containsEdge(node1, node2, label1));
        assertFalse(graph5.containsEdge(node1, node2, label2));
        assertFalse(graph5.containsEdge(node2, node1, label1));
        assertTrue(graph5.containsEdge(node2, node1, label2));
        assertFalse(graph5.containsEdge(node1, node2, bogusLabel));
        assertFalse(graph5.containsEdge(node2, node1, bogusLabel));
        final GraphNode<String, String> node1Context2 = graph5.get(node1);
        final GraphNode<String, String> node2Context2 = graph5.get(node2);
        assertNotNull(node1Context2);
        assertNotNull(node2Context2);
        assertNotEquals(node1Context1, node1Context2);
        assertNotEquals(node2Context1, node2Context2);
        assertContains(node2, node1Context2.incoming().nodes());
        assertContains(node2, node1Context2.outgoing().nodes());
        assertNotContains(node1, node1Context2.incoming().nodes());
        assertNotContains(node1, node1Context2.outgoing().nodes());
        assertContains(node1, node2Context2.incoming().nodes());
        assertContains(node1, node2Context2.outgoing().nodes());
        assertNotContains(node2, node2Context2.incoming().nodes());
        assertNotContains(node2, node2Context2.outgoing().nodes());
    }

    @Test public void addSelfEdge1() throws IllegalStateException {
        final int node = 0;
        final String label = "edge-label";
        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        graph = graph.addSelfEdge(node, label);
    }

    @Test public void addSelfEdge2() throws IllegalStateException {
        final int node = 0;
        final String label = "edge-label";
        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        graph = graph.addEdge(node, node, label);
    }

    @Test(expected = IllegalStateException.class) public void addAlreadyExistingEdge() throws IllegalStateException {
        final int node1 = 0;
        final int node2 = 1;
        final String label = "edge-label";
        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node1, null);
        graph = graph.addNode(node2, null);
        graph = graph.addEdge(node1, node2, label);
        graph = graph.addEdge(node1, node2, label);
    }

    @Test(expected = IllegalStateException.class) public void addAlreadyExistingSelfEdge() throws IllegalStateException {
        final int node = 0;
        final String label = "edge-label";

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        graph = graph.addSelfEdge(node, label);
        graph = graph.addSelfEdge(node, label);
    }

    @Test(expected = IllegalStateException.class) public void addEdgeNonExistantSrcNode() throws IllegalStateException {
        final int node = 0;
        final String label = "edge-label";

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        final int bogusNode = 1;
        graph = graph.addEdge(bogusNode, node, label);
    }

    @Test(expected = IllegalStateException.class) public void addEdgeNonExistantDstNode() throws IllegalStateException {
        final int node = 0;
        final String label = "edge-label";

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        final int bogusNode = 1;
        graph = graph.addEdge(node, bogusNode, label);
    }

    @Test(expected = IllegalStateException.class) public void addSelfEdgeNonExistantNode() throws IllegalStateException {
        final int node = 0;
        final String label = "edge-label";

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        final int bogusNode = 1;
        graph = graph.addSelfEdge(bogusNode, label);
    }

    @Test(expected = IllegalStateException.class) public void addEdgeNullLabel() throws IllegalStateException {
        final int node1 = 0;
        final int node2 = 1;

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node1, null);
        graph = graph.addNode(node2, null);
        graph = graph.addEdge(node1, node2, null);
    }

    @Test(expected = IllegalStateException.class) public void addSelfEdgeNullLabel() throws IllegalStateException {
        final int node = 0;
        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        graph = graph.addEdge(node, node, null);
    }

    @Test
    public void removeEdge() throws IllegalStateException {
        final int node1 = 0;
        final int node2 = 1;
        final String nodeLabel1 = "node-label-1";
        final String nodeLabel2 = null;
        final String edgeLabel1 = "edge-label-1";
        final String edgeLabel2 = "edge-label-2";

        final Graph<String, String> graph1 = this.emptyGraph;
        final Graph<String, String> graph2 = graph1.addNode(node1, nodeLabel1);
        final Graph<String, String> graph3 = graph2.addNode(node2, nodeLabel2);
        final Graph<String, String> graph4 = graph3.addEdge(node1, node2, edgeLabel1);
        final Graph<String, String> graph5 = graph4.addEdge(node2, node1, edgeLabel2);
        final Graph<String, String> graph6a = graph5.removeEdge(node1, node2, edgeLabel1);
        final Graph<String, String> graph6b = graph5.removeEdge(node2, node1, edgeLabel2);
        final Graph<String, String> graph7a = graph6b.removeEdge(node1, node2, edgeLabel1);
        final Graph<String, String> graph7b = graph6a.removeEdge(node2, node1, edgeLabel2);

        assertFalse(graph1.containsEdge(node1, node2));
        assertFalse(graph1.containsEdge(node1, node2, edgeLabel1));
        assertFalse(graph1.containsEdge(node2, node1));
        assertFalse(graph1.containsEdge(node2, node1, edgeLabel2));

        assertFalse(graph2.containsEdge(node1, node2));
        assertFalse(graph2.containsEdge(node1, node2, edgeLabel1));
        assertFalse(graph2.containsEdge(node2, node1));
        assertFalse(graph2.containsEdge(node2, node1, edgeLabel2));

        assertFalse(graph3.containsEdge(node1, node2));
        assertFalse(graph3.containsEdge(node1, node2, edgeLabel1));
        assertFalse(graph3.containsEdge(node2, node1));
        assertFalse(graph3.containsEdge(node2, node1, edgeLabel2));

        assertTrue(graph4.containsEdge(node1, node2));
        assertTrue(graph4.containsEdge(node1, node2, edgeLabel1));
        final String bogusLabel = "bogus";
        assertFalse(graph4.containsEdge(node1, node2, bogusLabel));
        assertFalse(graph4.containsEdge(node2, node1));
        assertFalse(graph4.containsEdge(node2, node1, edgeLabel2));
        assertFalse(graph4.containsEdge(node2, node1, bogusLabel));

        assertTrue(graph5.containsEdge(node1, node2));
        assertTrue(graph5.containsEdge(node1, node2, edgeLabel1));
        assertFalse(graph5.containsEdge(node1, node2, bogusLabel));
        assertTrue(graph5.containsEdge(node2, node1));
        assertTrue(graph5.containsEdge(node2, node1, edgeLabel2));
        assertFalse(graph5.containsEdge(node2, node1, bogusLabel));

        assertFalse(graph6a.containsEdge(node1, node2));
        assertFalse(graph6a.containsEdge(node1, node2, edgeLabel1));
        assertFalse(graph6a.containsEdge(node1, node2, bogusLabel));
        assertTrue(graph6a.containsEdge(node2, node1));
        assertTrue(graph6a.containsEdge(node2, node1, edgeLabel2));
        assertFalse(graph6a.containsEdge(node2, node1, bogusLabel));

        assertTrue(graph6b.containsEdge(node1, node2));
        assertTrue(graph6b.containsEdge(node1, node2, edgeLabel1));
        assertFalse(graph6b.containsEdge(node1, node2, bogusLabel));
        assertFalse(graph6b.containsEdge(node2, node1));
        assertFalse(graph6b.containsEdge(node2, node1, edgeLabel2));
        assertFalse(graph6b.containsEdge(node2, node1, bogusLabel));

        assertNotEquals(graph6a, graph6b);

        assertFalse(graph7a.containsEdge(node1, node2));
        assertFalse(graph7a.containsEdge(node1, node2, edgeLabel1));
        assertFalse(graph7a.containsEdge(node2, node1));
        assertFalse(graph7a.containsEdge(node2, node1, edgeLabel2));

        assertFalse(graph7b.containsEdge(node1, node2));
        assertFalse(graph7b.containsEdge(node1, node2, edgeLabel1));
        assertFalse(graph7b.containsEdge(node2, node1));
        assertFalse(graph7b.containsEdge(node2, node1, edgeLabel2));

        assertEquals(graph7a, graph7a);
        assertEquals(graph7b, graph7b);
        assertEquals(graph3, graph7a);
        assertEquals(graph3, graph7b);
    }

    @Test
    public void removeSelfEdge1() throws IllegalStateException {
        final int node = 0;
        final String label = "edge-label";

        final Graph<String, String> graph1 = this.emptyGraph;
        final Graph<String, String> graph2 = graph1.addNode(node, null);
        final Graph<String, String> graph3 = graph2.addEdge(node, node, label);
        final Graph<String, String> graph4 = graph3.removeSelfEdge(node, label);

        assertTrue(graph3.containsEdge(node, node, label));
        assertFalse(graph4.containsEdge(node, node, label));
    }

    @Test
    public void removeMultiSelfEdge1() throws IllegalStateException {
        final int node = 0;
        final String label1 = "edge-label-1";
        final String label2 = "edge-label-2";
        final String label3 = "edge-label-3";

        final Graph<String, String> graph1 = this.emptyGraph;
        final Graph<String, String> graph2 = graph1.addNode(node, null);
        final Graph<String, String> graph3 = graph2.addSelfEdge(node, label1);
        final Graph<String, String> graph4 = graph3.addSelfEdge(node, label2);
        final Graph<String, String> graph5 = graph4.addSelfEdge(node, label3);
        final Graph<String, String> graph6 = graph5.removeSelfEdge(node, label1);
        final Graph<String, String> graph7 = graph6.removeAllSelfEdges(node);

        assertTrue(graph5.containsEdge(node, node));
        assertTrue(graph5.containsEdge(node, node, label1));
        assertTrue(graph5.containsEdge(node, node, label2));
        assertTrue(graph5.containsEdge(node, node, label3));

        assertTrue(graph6.containsEdge(node, node));
        assertFalse(graph6.containsEdge(node, node, label1));
        assertTrue(graph6.containsEdge(node, node, label2));
        assertTrue(graph6.containsEdge(node, node, label3));

        assertFalse(graph7.containsEdge(node, node));
        assertFalse(graph7.containsEdge(node, node, label1));
        assertFalse(graph7.containsEdge(node, node, label2));
        assertFalse(graph7.containsEdge(node, node, label3));
    }

    @Test
    public void removeSelfEdge2() throws IllegalStateException {
        final int node = 0;
        final String label = "edge-label";
        final Graph<String, String> graph1 = this.emptyGraph;
        final Graph<String, String> graph2 = graph1.addNode(node, null);
        final Graph<String, String> graph3 = graph2.addEdge(node, node, label);
        final Graph<String, String> graph4 = graph3.removeEdge(node, node, label);

        assertTrue(graph3.containsEdge(node, node, label));
        assertFalse(graph4.containsEdge(node, node, label));
    }

    @Test
    public void removeMultiSelfEdge2() throws IllegalStateException {
        final int node = 0;
        final String label1 = "edge-label-1";
        final String label2 = "edge-label-2";
        final String label3 = "edge-label-3";

        final Graph<String, String> graph1 = this.emptyGraph;
        final Graph<String, String> graph2 = graph1.addNode(node, null);
        final Graph<String, String> graph3 = graph2.addEdge(node, node, label1);
        final Graph<String, String> graph4 = graph3.addEdge(node, node, label2);
        final Graph<String, String> graph5 = graph4.addEdge(node, node, label3);
        final Graph<String, String> graph6 = graph5.removeEdge(node, node, label1);
        final Graph<String, String> graph7 = graph6.removeAllEdges(node, node);

        assertTrue(graph5.containsEdge(node, node));
        assertTrue(graph5.containsEdge(node, node, label1));
        assertTrue(graph5.containsEdge(node, node, label2));
        assertTrue(graph5.containsEdge(node, node, label3));

        assertTrue(graph6.containsEdge(node, node));
        assertFalse(graph6.containsEdge(node, node, label1));
        assertTrue(graph6.containsEdge(node, node, label2));
        assertTrue(graph6.containsEdge(node, node, label3));

        assertFalse(graph7.containsEdge(node, node));
        assertFalse(graph7.containsEdge(node, node, label1));
        assertFalse(graph7.containsEdge(node, node, label2));
        assertFalse(graph7.containsEdge(node, node, label3));
    }

    @Test(expected = IllegalStateException.class) public void removeNonExistantEdge() throws IllegalStateException {
        final int node1 = 0;
        final int node2 = 1;
        final String edgeLabel = "edge-label";

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node1, null);
        graph = graph.addNode(node2, null);
        graph = graph.addEdge(node1, node2, edgeLabel);
        graph.removeEdge(node2, node1, edgeLabel);
    }

    @Test(expected = IllegalStateException.class) public void removeNonExistantMultiEdge() throws IllegalStateException {
        final int node1 = 0;
        final int node2 = 1;
        final String edgeLabel = "edge-label";

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node1, null);
        graph = graph.addNode(node2, null);
        graph = graph.addEdge(node1, node2, edgeLabel);
        graph.removeAllEdges(node2, node1);
    }

    @Test(expected = IllegalStateException.class) public void removeNonExistantSelfEdge() throws IllegalStateException {
        final int node = 0;
        final String edgeLabel = "edge-label";

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        graph.removeSelfEdge(node, edgeLabel);
    }

    @Test(expected = IllegalStateException.class) public void removeNonExistantMultiSelfEdge()
        throws IllegalStateException {
        final int node = 0;

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        graph.removeAllSelfEdges(node);
    }

    @Test(expected = IllegalStateException.class) public void removeEdgeNonExistantSrcNode()
        throws IllegalStateException {
        final int node = 0;
        final String label = "edge-label";

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        graph = graph.addEdge(node, node, label);
        final int bogusNode = 1;
        graph = graph.removeEdge(bogusNode, node, label);
    }

    @Test(expected = IllegalStateException.class) public void removeMultiEdgeNonExistantSrcNode()
        throws IllegalStateException {
        final int node = 0;
        final String label = "edge-label";

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        graph = graph.addEdge(node, node, label);
        final int bogusNode = 1;
        graph = graph.removeAllEdges(bogusNode, node);
    }

    @Test(expected = IllegalStateException.class) public void removeEdgeNonExistantDstNode()
        throws IllegalStateException {
        final int node = 0;
        final String label = "edge-label";

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        graph = graph.addEdge(node, node, label);
        final int bogusNode = 1;
        graph = graph.removeEdge(node, bogusNode, label);
    }

    @Test(expected = IllegalStateException.class) public void removeMultiEdgeNonExistantDstNode()
        throws IllegalStateException {
        final int node = 0;
        final String label = "edge-label";

        Graph<String, String> graph = this.emptyGraph;
        graph = graph.addNode(node, null);
        graph = graph.addEdge(node, node, label);
        final int bogusNode = 1;
        graph = graph.removeAllEdges(node, bogusNode);
    }

    @Test(expected = IllegalStateException.class) public void removeSelfEdgeNonExistantNode()
        throws IllegalStateException {
        final String label = "edge-label";

        Graph<String, String> graph = this.emptyGraph;
        final int bogusNode = 0;
        graph = graph.removeSelfEdge(bogusNode, label);
    }

    @Test(expected = IllegalStateException.class) public void removeMultiSelfEdgeNonExistantNode()
        throws IllegalStateException {
        Graph<String, String> graph = this.emptyGraph;
        final int bogusNode = 0;
        graph = graph.removeAllSelfEdges(bogusNode);
    }

    @Test
    public void hashCodes() throws IllegalStateException {
        final int node1 = 0;
        final int node2 = 1;
        final String nodeLabel2 = "node-label-2";
        final String label = "edge-label";

        final Graph<String, String> graph1 = this.emptyGraph;
        final Graph<String, String> graph2 = graph1.addNode(node1, null).addNode(node2, nodeLabel2);
        final Graph<String, String> graph3 = graph2.addEdge(node1, node1, label);
        final Graph<String, String> graph4 = graph3.removeAllEdges(node1, node1);
        final Graph<String, String> graph5 = graph4.removeNode(node1).removeNode(node2);

        assertEquals(graph1.hashCode(), graph1.hashCode());
        assertEquals(graph2.hashCode(), graph2.hashCode());
        assertEquals(graph3.hashCode(), graph3.hashCode());
        assertEquals(graph4.hashCode(), graph4.hashCode());
        assertEquals(graph5.hashCode(), graph5.hashCode());

        assertEquals(graph1.hashCode(), graph5.hashCode());
        assertEquals(graph2.hashCode(), graph4.hashCode());
    }

    @Test
    public void equals() throws IllegalStateException {
        final int node1 = 0;
        final int node2 = 1;
        final String nodeLabel1 = null;
        final String nodeLabel2 = "node-label-2";
        final String label = "edge-label";

        final Graph<String, String> graph1 = this.emptyGraph;
        final Graph<String, String> graph2 = graph1.addNode(node1, nodeLabel1);
        final Graph<String, String> graph3 = graph2.addNode(node2, nodeLabel2);
        final Graph<String, String> graph4 = graph3.addEdge(node1, node2, label);
        final Graph<String, String> graph5 = graph4.removeAllEdges(node1, node2);
        final Graph<String, String> graph6 = graph5.removeNode(node2);
        final Graph<String, String> graph7 = graph6.removeNode(node1);

        assertEquals(graph1, graph1);
        assertEquals(graph2, graph2);
        assertEquals(graph3, graph3);
        assertEquals(graph4, graph4);
        assertEquals(graph5, graph5);
        assertEquals(graph6, graph6);
        assertEquals(graph7, graph7);

        assertEquals(graph1, graph7);
        assertEquals(graph2, graph6);

        assertNotEquals(graph7, null);
        assertNotEquals(graph7, new Object());

        final GraphNode<String, String> context1 = graph4.get(node1);
        final GraphNode<String, String> context2 = graph4.get(node2);

        assertNotEquals(context1, context2);
        assertNotEquals(context1, emptyGraph.createNode(node1, nodeLabel2));
        assertNotEquals(context2, emptyGraph.createNode(node1, label));

        assertNotEquals(context2, null);
        assertNotEquals(context2, new Object());
    }

    @Test
    public void string() throws IllegalStateException {
        final int node = 0;
        final String label = "node-label";

        final Graph<String, String> graph1 = this.emptyGraph;
        final Graph<String, String> graph2 = graph1.addNode(node, label);

        assertEquals(graph1.toString(), graph1.toString());
        assertEquals(graph2.toString(), graph2.toString());
        assertNotEquals(graph1.toString(), graph2.toString());

        final GraphNode<String, String> context = graph2.get(node);

        assertEquals(context.toString(), context.toString());
        assertNotEquals(context.toString(), emptyGraph.createNode(node, null));
    }
}
