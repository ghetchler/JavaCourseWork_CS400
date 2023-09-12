import java.util.LinkedList;
import java.util.Stack;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Red-Black Tree implementation with a Node inner class for representing the nodes of the tree.
 * Currently, this implements a Binary Search Tree that we will turn into a red black tree by
 * modifying the insert functionality. In this activity, we will start with implementing rotations
 * for the binary search tree insert algorithm.
 */
public class RedBlackTree<T extends Comparable<T>> implements SortedCollectionInterface<T> {

    /**
     * This class represents a node holding a single value within a binary tree.
     */
    protected static class Node<T> {
        public int blackHeight; // this tracks black height for current node
        // 0 = red, 1 = black, 2 = double black
        public T data;
        // The context array stores the context of the node in the tree:
        // - context[0] is the parent reference of the node,
        // - context[1] is the left child reference of the node,
        // - context[2] is the right child reference of the node.
        // The @SupressWarning("unchecked") annotation is used to supress an unchecked
        // cast warning. Java only allows us to instantiate arrays without generic
        // type parameters, so we use this cast here to avoid future casts of the
        // node type's data field.
        @SuppressWarnings("unchecked")
        public Node<T>[] context = (Node<T>[]) new Node[3];

        public Node(T data) {
            this.data = data;
            this.blackHeight = 0;
        }


        /**
         * @return true when this node has a parent and is the right child of that parent, otherwise
         * return false
         */
        public boolean isRightChild() {
            return context[0] != null && context[0].context[2] == this;
        }

    }

    protected Node<T> root; // reference to root node of tree, null when empty
    protected int size = 0; // the number of values in the tree

    /**
     * Performs a naive insertion into a binary search tree: adding the input data value to a new
     * node in a leaf position within the tree. After this insertion, no attempt is made to
     * restructure or balance the tree. This tree will not hold null references, nor duplicate data
     * values.
     *
     * @param data to be added into this binary search tree
     * @return true if the value was inserted, false if not
     * @throws NullPointerException     when the provided data argument is null
     * @throws IllegalArgumentException when data is already contained in the tree
     */
    public boolean insert(T data) throws NullPointerException, IllegalArgumentException {
        // null references cannot be stored within this tree
        if (data == null)
            throw new NullPointerException("This RedBlackTree cannot store null references.");

        Node<T> newNode = new Node<>(data);
        if (this.root == null) {
            // add first node to an empty tree
            root = newNode;
            size++;
            enforceRBTreePropertiesAfterInsert(newNode);
            return true;
        } else {
            // insert into subtree
            Node<T> current = this.root;
            while (true) {
                int compare = newNode.data.compareTo(current.data);
                if (compare == 0) {
                    throw new IllegalArgumentException("This RedBlackTree already contains value "
                            + data.toString());
                } else if (compare < 0) {
                    // insert in left subtree
                    if (current.context[1] == null) {
                        // empty space to insert into
                        current.context[1] = newNode;
                        newNode.context[0] = current;
                        this.size++;
                        enforceRBTreePropertiesAfterInsert(newNode);
                        return true;
                    } else {
                        // no empty space, keep moving down the tree
                        current = current.context[1];
                    }
                } else {
                    // insert in right subtree
                    if (current.context[2] == null) {
                        // empty space to insert into
                        current.context[2] = newNode;
                        newNode.context[0] = current;
                        this.size++;
                        enforceRBTreePropertiesAfterInsert(newNode);
                        return true;
                    } else {
                        // no empty space, keep moving down the tree
                        current = current.context[2];
                    }
                }
            }
        }
    }

    /**
     * Performs the rotation operation on the provided nodes within this tree. When the provided
     * child is a left child of the provided parent, this method will perform a right rotation. When
     * the provided child is a right child of the provided parent, this method will perform a left
     * rotation. When the provided nodes are not related in one of these ways, this method will
     * throw an IllegalArgumentException.
     *
     * @param child  is the node being rotated from child to parent position (between these two node
     *               arguments)
     * @param parent is the node being rotated from parent to child position (between these two node
     *               arguments)
     * @throws IllegalArgumentException when the provided child and parent node references are not
     *                                  initially (pre-rotation) related that way
     */
    private void rotate(Node<T> child, Node<T> parent) throws IllegalArgumentException {
        if (!child.context[0].data.equals(parent.data))
            throw new IllegalArgumentException("parent and child are not related");

        if (parent.data.equals(root.data)) { // if the parent is a root node
            if (child.isRightChild()) {
                if (child.context[1] != null) { // if child has a left child
                    parent.context[2] = child.context[1]; // makes child's old LC parent's new RC
                    parent.context[0] = child; // makes parent's new parent child node
                    child.context[1] = parent; // makes child's left child parent node
                    root = child; // sets child as new root
                    child.context[0] = null; // sets child's parent to null since it's the root
                } else {
                    parent.context[0] = child; // makes parent's new parent child node
                    child.context[1] = parent; // makes child's left child parent node
                    root = child; // sets child as new root
                    child.context[0] = null; // sets child's parent to null since it's the root
                    parent.context[2] = null; // sets parent RC to null
                }
            } else {
                if (child.context[2] != null) { // if child already has RC, need to add it to
                    // parent's LC
                    parent.context[1] = child.context[2];
                    parent.context[0] = child; // makes parent's new parent child node
                    child.context[2] = parent; // makes child's right child parent node
                    root = child; // sets child as new root
                    child.context[0] = null; // sets child's parent to null since it's the root
                } else {
                    parent.context[0] = child; // makes parent's new parent child node
                    child.context[2] = parent; // makes child's right child parent node
                    root = child; // sets child as new root
                    child.context[0] = null; // sets child's parent to null since it's the root
                    parent.context[1] = null; // sets parent LC to null
                }
            }
        } else if (child.isRightChild()) { // if parent node isn't root
            //left rotation..
            if (child.context[1] != null) { // if child has a left child
                parent.context[2] = child.context[1]; // makes child's old LC parent's new RC
                child.context[0] = parent.context[0]; // the child's new parent is the parent of the
                // parent node
                parent.context[0].context[1] = child; // the parent's RC is updated to child
                parent.context[0] = child; // parent's parent equals the child node
                child.context[1] = parent; // child's left child equals the old parent node
            } else {
                child.context[0] = parent.context[0]; // the child's new parent is the parent of the
                // parent node
                parent.context[0].context[1] = child; // the parent's RC is updated to child
                parent.context[0] = child; // parent's parent equals the child node
                child.context[1] = parent; // child's left child equals the old parent node
                parent.context[2] = null; // sets parent right child to null since there's
                // nothing to
                // put there
            }
        } else {
            //right rotation..
            if (child.context[2] != null) { // if child already has RC, need to add it to
                // parent's LC
                parent.context[1] = child.context[2];
                child.context[0] = parent.context[0]; // the child's new parent is the parent of
                // the parent node
                parent.context[0].context[1] = child; // the parent's LC is updated to child
                parent.context[0] = child; // parent's parent equals the child node
                child.context[2] = parent; // child's right child equals the old parent node
            } else {
                child.context[0] = parent.context[0]; // the child's new parent is the parent of
                // the parent node
                parent.context[0].context[1] = child; // the parent's LC is updated to child
                parent.context[0] = child; // parent's parent equals the child node
                child.context[2] = parent; // child's right child equals the old parent node
                parent.context[1] = null; // sets parent left child to null since there's nothing to
                // put there
            }
        }
    }

    protected void enforceRBTreePropertiesAfterInsert(Node<T> newNode) {
        if (this.root.data.equals(newNode.data)) {
            newNode.blackHeight = 1;
        } else if (newNode.context[0].blackHeight == 1) {
            return;  //parent of new node is black, no change is needed
        }
        // parent of new node is red:
        else if (newNode.context[0].blackHeight == 0) {
            if (newNode.context[0].isRightChild()) { // if parent is RC we want to look at G's LC
                if (newNode.context[0].context[0].context[1] == null || newNode.context[0].context[0].context[1].blackHeight == 1) { // Aunt LC and is black or
                    // null,
                    if (!newNode.isRightChild()) { // if newNode is a LC need to rotate first
                        rotate(newNode, newNode.context[0]);
                        rotate(newNode, newNode.context[0]); // rotate again
                        newNode.blackHeight = 1; // makes new node black
                        newNode.context[1].blackHeight = 0; // makes old grandparent red
                        enforceRBTreePropertiesAfterInsert(newNode); // recursive call using new
                        // node
                    } else {

                        rotate(newNode.context[0], newNode.context[0].context[0]);// rotate at
                        // grandparent

                        newNode.context[0].context[2].blackHeight = 0;// make old grandparent red
                        newNode.context[0].blackHeight = 1;// make parent black
                        enforceRBTreePropertiesAfterInsert(newNode.context[0]); // recursive call
                        // using parent node to make sure parent isn't breaking
                        // properties
                    }
                } else { // Aunt is LC and is red
                    // recolor grandparent to red and parent and aunt to black
                    newNode.context[0].context[0].blackHeight = 0; // makes grandparent red
                    newNode.context[0].blackHeight = 1; // make parent black
                    if (newNode.context[0].context[0].context[1] != null) { // if Aunt isn't null
                        newNode.context[0].context[0].context[1].blackHeight = 1; // make Aunt black
                    }
                    // recursive call using grandparent node to make sure that grandparent isn't
                    // breaking
                    // properties
                    enforceRBTreePropertiesAfterInsert(newNode.context[0].context[0]);
                }
            } else // we want to look at G's RC
                if (newNode.context[0].context[0].context[2] == null || newNode.context[0].context[0].context[2].blackHeight == 1) { // Aunt  RC and is black or null
                    // rotate at grandparent
                    if (newNode.isRightChild()) {
                        rotate(newNode, newNode.context[0]); // rotate to put new node above parent
                        rotate(newNode, newNode.context[0]); // rotate again to put new node as root
                        newNode.blackHeight = 1; // makes new node black
                        newNode.context[2].blackHeight = 0; // makes old grandparent red
                        enforceRBTreePropertiesAfterInsert(newNode); // recursive call using new
                        // node
                    } else { // new node is LC
                        rotate(newNode.context[0], newNode.context[0].context[0]);
                        newNode.context[0].context[2].blackHeight = 0;// make grandparent red
                        newNode.context[0].blackHeight = 1; // make parent black
                        enforceRBTreePropertiesAfterInsert(newNode.context[0]);// recursive call
                        // using parent node to make sure parent isn't breaking properties
                    }

                } else { // Aunt is RC and is red
                    // recolor grandparent to red and parent and aunt to black
                    newNode.context[0].context[0].blackHeight = 0; // makes grandparent red
                    newNode.context[0].blackHeight = 1; // make parent black
                    newNode.context[0].context[0].context[2].blackHeight = 1; // make Aunt black
                    // recursive call using grandparent node to make sure that grandparent isn't
                    // breaking properties
                    enforceRBTreePropertiesAfterInsert(newNode.context[0].context[0]);
                }
        }
        if (this.root.data.equals(newNode.data)) {
            newNode.blackHeight = 1;
        }
    }

    /**
     * Get the size of the tree (its number of nodes).
     *
     * @return the number of nodes in the tree
     */
    public int size() {
        return size;
    }

    /**
     * Method to check if the tree is empty (does not contain any node).
     *
     * @return true of this.size() return 0, false if this.size() > 0
     */
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * Removes the value data from the tree if the tree contains the value. This method will not
     * attempt to rebalance the tree after the removal and should be updated once the tree uses
     * Red-Black Tree insertion.
     *
     * @return true if the value was remove, false if it didn't exist
     * @throws NullPointerException     when the provided data argument is null
     * @throws IllegalArgumentException when data is not stored in the tree
     */
    public boolean remove(T data) throws NullPointerException, IllegalArgumentException {
        // null references will not be stored within this tree
        if (data == null) {
            throw new NullPointerException("This RedBlackTree cannot store null references.");
        } else {
            Node<T> nodeWithData = this.findNodeWithData(data);
            // throw exception if node with data does not exist
            if (nodeWithData == null) {
                throw new IllegalArgumentException("The following value is not in the tree and " + "cannot be deleted: " + data.toString());
            }
            boolean hasRightChild = (nodeWithData.context[2] != null);
            boolean hasLeftChild = (nodeWithData.context[1] != null);
            if (hasRightChild && hasLeftChild) {
                // has 2 children
                Node<T> successorNode = this.findMinOfRightSubtree(nodeWithData);
                // replace value of node with value of successor node
                nodeWithData.data = successorNode.data;
                // remove successor node
                if (successorNode.context[2] == null) {
                    // successor has no children, replace with null
                    this.replaceNode(successorNode, null);
                } else {
                    // successor has a right child, replace successor with its child
                    this.replaceNode(successorNode, successorNode.context[2]);
                }
            } else if (hasRightChild) {
                // only right child, replace with right child
                this.replaceNode(nodeWithData, nodeWithData.context[2]);
            } else if (hasLeftChild) {
                // only left child, replace with left child
                this.replaceNode(nodeWithData, nodeWithData.context[1]);
            } else {
                // no children, replace node with a null node
                this.replaceNode(nodeWithData, null);
            }
            this.size--;
            return true;
        }
    }

    /**
     * Checks whether the tree contains the value *data*.
     *
     * @param data the data value to test for
     * @return true if *data* is in the tree, false if it is not in the tree
     */
    public boolean contains(T data) {
        // null references will not be stored within this tree
        if (data == null) {
            throw new NullPointerException("This RedBlackTree cannot store null references.");
        } else {
            Node<T> nodeWithData = this.findNodeWithData(data);
            // return false if the node is null, true otherwise
            return (nodeWithData != null);
        }
    }

    /**
     * Helper method that will replace a node with a replacement node. The replacement node may be
     * null to remove the node from the tree.
     *
     * @param nodeToReplace   the node to replace
     * @param replacementNode the replacement for the node (may be null)
     */
    protected void replaceNode(Node<T> nodeToReplace, Node<T> replacementNode) {
        if (nodeToReplace == null) {
            throw new NullPointerException("Cannot replace null node.");
        }
        if (nodeToReplace.context[0] == null) {
            // we are replacing the root
            if (replacementNode != null) replacementNode.context[0] = null;
            this.root = replacementNode;
        } else {
            // set the parent of the replacement node
            if (replacementNode != null) replacementNode.context[0] = nodeToReplace.context[0];
            // do we have to attach a new left or right child to our parent?
            if (nodeToReplace.isRightChild()) {
                nodeToReplace.context[0].context[2] = replacementNode;
            } else {
                nodeToReplace.context[0].context[1] = replacementNode;
            }
        }
    }

    /**
     * Helper method that will return the inorder successor of a node with two children.
     *
     * @param node the node to find the successor for
     * @return the node that is the inorder successor of node
     */
    protected Node<T> findMinOfRightSubtree(Node<T> node) {
        if (node.context[1] == null && node.context[2] == null) {
            throw new IllegalArgumentException("Node must have two children");
        }
        // take a steop to the right
        Node<T> current = node.context[2];
        while (true) {
            // then go left as often as possible to find the successor
            if (current.context[1] == null) {
                // we found the successor
                return current;
            } else {
                current = current.context[1];
            }
        }
    }

    /**
     * Helper method that will return the node in the tree that contains a specific value. Returns
     * null if there is no node that contains the value.
     *
     * @return the node that contains the data, or null of no such node exists
     */
    protected Node<T> findNodeWithData(T data) {
        Node<T> current = this.root;
        while (current != null) {
            int compare = data.compareTo(current.data);
            if (compare == 0) {
                // we found our value
                return current;
            } else if (compare < 0) {
                // keep looking in the left subtree
                current = current.context[1];
            } else {
                // keep looking in the right subtree
                current = current.context[2];
            }
        }
        // we're at a null node and did not find data, so it's not in the tree
        return null;
    }

    /**
     * This method performs an inorder traversal of the tree. The string representations of each
     * data value within this tree are assembled into a comma separated string within brackets
     * (similar to many implementations of java.util.Collection, like java.util.ArrayList,
     * LinkedList, etc).
     *
     * @return string containing the ordered values of this tree (in-order traversal)
     */
    public String toInOrderString() {
        // generate a string of all values of the tree in (ordered) in-order
        // traversal sequence
        StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        if (this.root != null) {
            Stack<Node<T>> nodeStack = new Stack<>();
            Node<T> current = this.root;
            while (!nodeStack.isEmpty() || current != null) {
                if (current == null) {
                    Node<T> popped = nodeStack.pop();
                    sb.append(popped.data.toString());
                    if (!nodeStack.isEmpty() || popped.context[2] != null) sb.append(", ");
                    current = popped.context[2];
                } else {
                    nodeStack.add(current);
                    current = current.context[1];
                }
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * This method performs a level order traversal of the tree. The string representations of each
     * data value within this tree are assembled into a comma separated string within brackets
     * (similar to many implementations of java.util.Collection). This method will be helpful as a
     * helper for the debugging and testing of your rotation implementation.
     *
     * @return string containing the values of this tree in level order
     */
    public String toLevelOrderString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        if (this.root != null) {
            LinkedList<Node<T>> q = new LinkedList<>();
            q.add(this.root);
            while (!q.isEmpty()) {
                Node<T> next = q.removeFirst();
                if (next.context[1] != null) q.add(next.context[1]);
                if (next.context[2] != null) q.add(next.context[2]);
                sb.append(next.data.toString());
                if (!q.isEmpty()) sb.append(", ");
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    public String toString() {
        return "level order: " + this.toLevelOrderString() + "\nin order: " + this.toInOrderString();
    }

    /**
     * this tests adding a Node as RC to a red LC parent with a black aunt
     */
    @Test
    void ensureCorrectRotationWhenRCIsAddedToLCParentWithBlackAunt() {

        RedBlackTree<Integer> test1 = new RedBlackTree<>();
        RedBlackTree.Node<Integer> node1 = new RedBlackTree.Node<>(45);
        RedBlackTree.Node<Integer> node2 = new RedBlackTree.Node<>(26);
        RedBlackTree.Node<Integer> node3 = new RedBlackTree.Node<>(72);
        RedBlackTree.Node<Integer> node4 = new RedBlackTree.Node<>(28);

        test1.insert(node1.data);
        test1.insert(node2.data);
        test1.insert(node3.data);
        test1.root.context[1].blackHeight = 0;
        test1.root.context[2].blackHeight = 1;
        test1.insert(node4.data);

        assertAll(() -> assertEquals(1, test1.root.blackHeight), () -> assertEquals(0,
                test1.root.context[1].blackHeight), () -> assertEquals(0,
                test1.root.context[2].blackHeight), () -> assertEquals(1,
                test1.root.context[2].context[2].blackHeight));


    }

    /**
     * tests the correct coloring after adding to the LC of a red parent that has a black aunt
     */
    @Test
    void ensureCorrectRotationWhenLCIsAddedToLCParentWithBlackAunt() {
        RedBlackTree<Integer> test1 = new RedBlackTree<>();
        RedBlackTree.Node<Integer> node1 = new RedBlackTree.Node<>(45);
        RedBlackTree.Node<Integer> node2 = new RedBlackTree.Node<>(26);
        RedBlackTree.Node<Integer> node3 = new RedBlackTree.Node<>(72);
        RedBlackTree.Node<Integer> node4 = new RedBlackTree.Node<>(20);

        test1.insert(node1.data);
        test1.insert(node2.data);
        test1.insert(node3.data);
        test1.root.context[1].blackHeight = 0;
        test1.root.context[2].blackHeight = 1;
        test1.insert(node4.data);

        assertAll(() -> assertEquals(1, test1.root.blackHeight), () -> assertEquals(0,
                test1.root.context[1].blackHeight), () -> assertEquals(0,
                test1.root.context[2].blackHeight), () -> assertEquals(1,
                test1.root.context[2].context[2].blackHeight));
    }

    /**
     * test to make sure that the inserted nodes have the correct color
     */
    @Test
    void ensureCorrectColoringOfRedBlackTree() {
        RedBlackTree<Integer> test1 = new RedBlackTree<>();
        RedBlackTree.Node<Integer> node1 = new RedBlackTree.Node<>(4);
        RedBlackTree.Node<Integer> node2 = new RedBlackTree.Node<>(2);
        RedBlackTree.Node<Integer> node3 = new RedBlackTree.Node<>(6);
        RedBlackTree.Node<Integer> node4 = new RedBlackTree.Node<>(1);
        RedBlackTree.Node<Integer> node5 = new RedBlackTree.Node<>(3);
        RedBlackTree.Node<Integer> node6 = new RedBlackTree.Node<>(5);


        test1.insert(node1.data);
        test1.insert(node2.data);
        test1.insert(node3.data);
        test1.insert(node4.data);
        test1.insert(node5.data);
        test1.insert(node6.data);

        assertAll(() -> assertEquals(1, test1.root.blackHeight), () -> assertEquals(1,
                test1.root.context[1].blackHeight), () -> assertEquals(1,
                test1.root.context[2].blackHeight), () -> assertEquals(0,
                test1.root.context[1].context[1].blackHeight), () -> assertEquals(0,
                test1.root.context[1].context[2].blackHeight), () -> assertEquals(0,
                test1.root.context[2].context[1].blackHeight));

    }
}