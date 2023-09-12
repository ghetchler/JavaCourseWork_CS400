// --== CS400 File Header Information ==--
// Name: Garrett Hetchler
// Email: ghetchler@wisc.edu
// Group and Team: NA
// Group TA: NA
// Lecturer: Payman
// Notes to Grader: <optional extra notes>

import org.junit.Assert;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Hashtable;
import java.util.List;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * This class extends the BaseGraph data structure with additional methods for computing the total
 * cost and list of node data along the shortest path connecting a provided starting to ending
 * nodes.  This class makes use of Dijkstra's shortest path algorithm.
 */
public class DijkstraGraph<NodeType, EdgeType extends Number> extends BaseGraph<NodeType,
        EdgeType> implements GraphADT<NodeType, EdgeType> {

    /**
     * While searching for the shortest path between two nodes, a SearchNode contains data about one
     * specific path between the start node and another node in the graph.  The final node in this
     * path is stored in it's node field.  The total cost of this path is stored in its cost field.
     * And the predecessor SearchNode within this path is referened by the predecessor field (this
     * field is null within the SearchNode containing the starting node in it's node field).
     * <p>
     * SearchNodes are Comparable and are sorted by cost so that the lowest cost SearchNode has the
     * highest priority within a java.util.PriorityQueue.
     */
    protected class SearchNode implements Comparable<SearchNode> {
        public Node node;
        public double cost;
        public SearchNode predecessor;

        public SearchNode(Node node, double cost, SearchNode predecessor) {
            this.node = node;
            this.cost = cost;
            this.predecessor = predecessor;
        }

        public int compareTo(SearchNode other) {
            if (cost > other.cost) return +1;
            if (cost < other.cost) return -1;
            return 0;
        }
    }

    /**
     * This helper method creates a network of SearchNodes while computing the shortest path between
     * the provided start and end locations.  The SearchNode that is returned by this method is
     * represents the end of the shortest path that is found: it's cost is the cost of that shortest
     * path, and the nodes linked together through predecessor references represent all of the nodes
     * along that shortest path (ordered from end to start).
     *
     * @param start the data item in the starting node for the path
     * @param end   the data item in the destination node for the path
     * @return SearchNode for the final end node within the shortest path
     * @throws NoSuchElementException when no path from start to end is found or when either start
     *                                or end data do not correspond to a graph node
     */
    protected SearchNode computeShortestPath(NodeType start, NodeType end) {
        // TODO: implement in step 6
        if (!this.containsNode(start) || !this.containsNode(end)) // if not contained, not here
            throw new NoSuchElementException("Start or end vertices cannot be found");

        Hashtable<NodeType, Node> visited = new Hashtable<>(); // table containing all nodes that
        // have been visited
        PriorityQueue<SearchNode> listOfNodes = new PriorityQueue<>(); // pq of all nodes that
        // need to be visited
        SearchNode shortestPath = new SearchNode(null, 0.0, null);// new searchNode
        // that will contain last node visited in path(end node), total cost to go from start to
        // finish, previous node visited before reaching end

        listOfNodes.add(new SearchNode(nodes.get(start), 0.0, null));
        //listOfNodes.add(start);// adds start to the list

        if (start.equals(end)) { // if start and end are the same return
            return new SearchNode(nodes.get(start), 0.0, null);
        }

        while (!listOfNodes.isEmpty()) { // while our pq isn't empty

            SearchNode temp = listOfNodes.remove(); // removes the first node in the pq
//            if(!visited.contains(temp)) {
//                visited.put(temp.node.data, temp.node);
//                shortestPath.cost += temp.cost;
//                shortestPath.predecessor = temp.predecessor;
//                shortestPath.node = temp.node;
//                for (int i = 0; i < temp.node.edgesLeaving.size(); ++i ) {
//                    if (!visited.contains((temp.node.edgesLeaving.get(i).data))) {
//                        System.out.println("if hit");
//                        listOfNodes.add(new SearchNode(temp.node.edgesLeaving.get(i).successor,
//                        temp.cost,
//                                temp));
//                    }
//                }
//System.out.println("For loop done");
//            }
            visited.put(temp.node.data, temp.node); // adds temp(removed node) data to visited
            List<Edge> listLeavingEdges = temp.node.edgesLeaving; // finds all leaving edges

            LinkedList<SearchNode> listOfSearchNodes = new LinkedList<>();

            for (int i = 0; i < listLeavingEdges.size(); ++i) { // for all the leaving edges
                Edge leavingEdge = listLeavingEdges.get(i); // gets edge
                SearchNode temp2 = new SearchNode(leavingEdge.successor,
                        leavingEdge.data.doubleValue(), temp);
                // creates new searchNode for target node connected to temp
                if (visited.contains(temp2.node)) {
                    // System.out.println(temp2.node.data + ": already in visited");
                    continue; // if the target node has already been visited, skip it

                }
                temp2.cost += temp.cost; // adds temp cost to temp2 cost to keep track of total cost
                listOfNodes.add(temp2);// adds unvisited target node to pq
                listOfSearchNodes.add(temp2);
                //System.out.println(temp2.node.data + ": added to list of nodes to searched");

            }
            LinkedList<SearchNode> shortPathFinder = new LinkedList<>(); // new list to hold
            // possible searchNodes
            PriorityQueue<SearchNode> copy = new PriorityQueue<SearchNode>(listOfNodes); // copy
            // of pq
            for (int i = 0; i < copy.size() + 1; ++i) {
                if (copy.size() == 0) {
                    continue;
                }
                shortPathFinder.add(copy.remove()); // adds elements from pq to pathFinder
            }
            if (temp.node.data.equals(end)) { // if temp is end, we have gone
                // through enough nodes
               if (shortestPath.node == null) {
                   shortestPath.cost = temp.cost;
                   shortestPath.node = temp.node;
                   shortestPath.predecessor = temp.predecessor;
               } else if (shortestPath.compareTo(temp) == 1) {
                   shortestPath.cost = temp.cost;
                   shortestPath.node = temp.node;
                   shortestPath.predecessor = temp.predecessor;
               }
            }
        }


        if (shortestPath.node == null)
            throw new NoSuchElementException("There is no path between these nodes");
        return shortestPath;
    }

    /**
     * Returns the list of data values from nodes along the shortest path from the node with the
     * provided start value through the node with the provided end value.  This list of data values
     * starts with the start value, ends with the end value, and contains intermediary values in the
     * order they are encountered while traversing this shortest path.  This method uses Dijkstra's
     * shortest path algorithm to find this solution.
     *
     * @param start the data item in the starting node for the path
     * @param end   the data item in the destination node for the path
     * @return list of data item from node along this shortest path
     */
    public List<NodeType> shortestPathData(NodeType start, NodeType end) {
        // TODO: implement in step 7
        List<NodeType> shortPathList = new ArrayList<>(); // list to hold shortest path data

        SearchNode temp = new SearchNode(null, 0.0, null);
        temp =  computeShortestPath(start,end); // sets temp to end of shortest path

        while (temp.predecessor != null) {
            //System.out.println(temp.predecessor.node.data);
            shortPathList.add(temp.node.data); // adds temp to shortpath list
            temp = temp.predecessor; // sets temp to predecessor to cycle through to start

        }
        shortPathList.add(start); // adds start to finish off path
        List<NodeType> tempList = new ArrayList<>(shortPathList); // temp array
        shortPathList.clear();
        for (int i = tempList.size() - 1; i > 0; --i) { // reorders so that start is at beginning of
            // list
            shortPathList.add(tempList.get(i));
        }
        shortPathList.add(end);
        return shortPathList;
    }

    /**
     * Returns the cost of the path (sum over edge weights) of the shortest path from the node
     * containing the start data to the node containing the end data.  This method uses Dijkstra's
     * shortest path algorithm to find this solution.
     *
     * @param start the data item in the starting node for the path
     * @param end   the data item in the destination node for the path
     * @return the cost of the shortest path between these nodes
     */
    public double shortestPathCost(NodeType start, NodeType end) {
        return computeShortestPath(start, end).cost;
    }

    // TODO: implement 3+ tests in step 8.

    /**
     * holds all the tests for the Dijkstra Graph
     */
    public class DijkstraGraphTest {
        /**
         * tests to see if it can find the shortest path on a simple graph
         */
        @Test
        public void testFindingShortestPathSimpleGraph() {
            DijkstraGraph<String, Number> test = new DijkstraGraph<>();

            test.insertNode("A");
            test.insertNode("B");
            test.insertNode("C");
            test.insertNode("D");

            test.insertEdge("A", "B", 4);
            test.insertEdge("A", "C", 1);
            test.insertEdge("B", "D", 2);
            test.insertEdge("C", "D", 2);


            assertAll(() -> assertEquals(4, test.shortestPathCost("A", "B")),
                    () -> assertEquals(3, test.shortestPathCost("A", "D")));

        }

        /**
         * Create a test that makes use of an example graph you previously traced by hand, and
         * confirm that the results of your implementation match what you previously computed by
         * hand.
         */
        @Test
        public void testMoreComplexGraph() {
            DijkstraGraph<String, Number> test = new DijkstraGraph<>();

            test.insertNode("A");
            test.insertNode("B");
            test.insertNode("C");
            test.insertNode("D");
            test.insertNode("E");
            test.insertNode("F");
            test.insertNode("G");

            test.insertEdge("A", "B", 4);
            test.insertEdge("A", "C", 1);
            test.insertEdge("B", "D", 2);
            test.insertEdge("C", "D", 2);
            test.insertEdge("B", "A", 3);
            test.insertEdge("C", "E", 4);
            test.insertEdge("E", "F", 3);
            test.insertEdge("E", "G", 5);
            test.insertEdge("A", "G", 15);

            assertAll(() -> assertEquals("G", test.computeShortestPath("A", "G").node.data),
                    () -> assertEquals("E",
                            test.computeShortestPath("A", "G").predecessor.node.data),
                    () -> assertEquals("C",
                            test.computeShortestPath("A", "G").predecessor.predecessor.node.data)
                    , () -> assertEquals("A",
                            test.computeShortestPath("A", "G").predecessor.predecessor.predecessor.node.data));

        }

        /**
         * Create another test using the same graph as you did for the test above, but check the
         * cost and sequence of data along the shortest path between a different start and end
         * node.
         */
        @Test
        public void testCostAndSequenceOfMoreComplexGraph() {
            DijkstraGraph<String, Number> test = new DijkstraGraph<>();

            test.insertNode("A");
            test.insertNode("B");
            test.insertNode("C");
            test.insertNode("D");
            test.insertNode("E");
            test.insertNode("F");
            test.insertNode("G");

            test.insertEdge("A", "B", 4);
            test.insertEdge("A", "C", 1);
            test.insertEdge("B", "D", 2);
            test.insertEdge("C", "D", 2);
            test.insertEdge("B", "A", 3);
            test.insertEdge("C", "E", 4);
            test.insertEdge("E", "F", 3);
            test.insertEdge("E", "G", 5);
            test.insertEdge("A", "G", 15);

            assertAll(() -> assertEquals(10, test.shortestPathCost("A", "G")),
                    () -> assertEquals(8, test.shortestPathCost("A", "F")), () -> assertEquals(3,
                            test.shortestPathData("A", "D").size()), () -> assertEquals("A",
                            test.shortestPathData("A", "D").get(0)), () -> assertEquals("C",
                            test.shortestPathData("A", "D").get(1)), () -> assertEquals("D",
                            test.shortestPathData("A", "D").get(2)));
        }

        /**
         * Create a test that checks the behavior of your implementation when the node that you are
         * searching for a path between exist in the graph, but there is no sequence of directed
         * edges that connects them from the start to the end.
         */
        @Test
        public void testNodesThatAreNotConnected() {
            DijkstraGraph<String, Number> test = new DijkstraGraph<>();

            test.insertNode("A");
            test.insertNode("B");
            test.insertNode("C");
            test.insertNode("D");
            test.insertNode("E");
            test.insertNode("F");
            test.insertNode("G");
            test.insertNode("H");

            test.insertEdge("A", "B", 4);
            test.insertEdge("A", "C", 1);
            test.insertEdge("B", "D", 2);
            test.insertEdge("C", "D", 2);
            test.insertEdge("B", "A", 3);
            test.insertEdge("C", "E", 4);
            test.insertEdge("E", "F", 3);
            test.insertEdge("E", "G", 5);
            test.insertEdge("A", "G", 15);

            try {
                test.shortestPathData("A", "H");
                Assert.fail("should have thrown an exception");

            } catch (NoSuchElementException e) {
                String expected = "There is no path between these nodes";
                assertEquals(expected, e.getMessage());
            }

        }
        /**
         * tests if a node has multiple edges going into it, that it finds the shorter of the two and
         * returns it
         */
        @Test
        public void testDifferentValuesIntoSameNode() {
            DijkstraGraph<String, Number> test = new DijkstraGraph<>();

            test.insertNode("A");
            test.insertNode("B");
            test.insertNode("C");
            test.insertNode("D");

            test.insertEdge("A","B",4);
            test.insertEdge("A","C",1);
            test.insertEdge("B","D",2);
            test.insertEdge("C","D",2);
            test.insertEdge("A","B",2);

            assertEquals(2, test.shortestPathCost("A", "B"));

        }
    }
}
