
/**
 * ArrayBST provides a Binary Search Tree mechanism using an array as the
 * underlying representation, rather than a collection of linked nodes.
 * 
 * @author koomen, modified by Alex Peng
 * @version 140927.1
 * 
 * Assignment: Project 2
 * 
 * Due Date: 04/18/2017
 */

/**
 * ArrayBST provides a Binary Search Tree mechanism using an array as the
 * underlying representation, rather than a collection of linked nodes.
 * @param <GO> - the type of Comparable that is inserted into the Array BST
 */
public class ArrayBST<Data extends Comparable<? super GO>> {

	private GO[] data;
	private boolean balance = true;//flag for whether the tree should be balanced (default = true)

	public ArrayBST() {
		//default initial capacity = 1 (index 0 is not used)
		data = new GO[2];
	}

	public ArrayBST(int initialCapacity) {
		data = new GO[initialCapacity+1]; //(index 0 is not used)
	}

	/**
	 * @return int - the number of nodes in the tree (nodes "in use")
	 */
	public int size() {
		return subTreeSize(1);//size from the root of the whole tree
	}

	/**
	 * @return int - the height of the tree (length of longest path)
	 */
	public int height() {
		return nodeHeight(1);//height from the root of the whole tree
	}

	/**
	 * @return double - the average depth of all nodes
	 */
	public double averageDepth() {
		return (double)totalDepth(1,0)/size();// total depth from the root of the whole tree / size of whole tree
	}

	/**
	 * If the BST already contains a data object d' equal to the given data
	 * object d then sequence is added, and count is incremented. 
	 * 
	 * @param d
	 * @return
	 */
	public boolean insert(GO d) {
		return insert(d,1);//try to insert starting from node 1
	}

	/**
	 * find(d) If the BST contains a data object d' equal to the given object d
	 * then find returns d'; otherwise it returns null
	 * 
	 * @param d - the data that is being searched
	 * @return - null if not fount, that data if found
	 */
	public GO find(GO d) {
		int node = 1;
		while(!isNull(node)){
			if(d.compareTo(data[node]) == 0)
				return data[node];

			if(d.compareTo(data[node]) < 0)
				node = left(node);
			else
				node = right(node);
		}

		return null;
	}

	/**
	 * INORDER traversal of the BST
	 * @param worker - the functional object that gets to accept each node's data
	 */
	public void inOrder(Consumer<GO> worker) {
		inOrder(worker, 1);
	}

	/**
	 * @param flag - true if AVL property should be maintained, false otherwise
	 *            fails if flag == true && size() > 1.
	 *            Default behavior of the ArrayBST is keepBalanced(false).
	 */
	public void keepBalanced(boolean flag) {
		balance = flag;
	}

	/**
	 * Method that returns the data in the tree with the given index
	 * @param node - index of the tree
	 * @return
	 */
	public GO get(int node){
		return !isNull(node)? data[node] : null;
	}

	/*
	 * public methods 
	 * **************************************************
	 * private methods
	 */

	private int left(int t) {
		return 2*t;
	}

	private int right(int t) {
		return 2*t + 1;
	}

	private int parent(int t) {
		return t/2;
	}

	private boolean isNull(int t) {
		return (t < 1) || (t >= data.length) || (data[t] == null);
	}

	private boolean hasLeft(int p) {
		return !isNull(left(p));
	}

	private boolean hasRight(int p) {
		return !isNull(right(p));
	}

	/**
	 * Recursive helper method that inserts GO d into the Array BST
	 * @param d - the data that we want to insert
	 * @param node - the current node we are at 
	 */
	private boolean insert(GO d, int node){
		if(node >= data.length){
			GO[] biggerTree = new GO[data.length * 2];
			System.arraycopy(data, 0, biggerTree, 0, data.length);
			data = biggerTree;

			data[node] = d;

			if(balance)
				balance(node);

			return true;
		}
		else if(data[node] == null){
			data[node] = d;

			if(balance)
				balance(node);

			return true;
		}
		else if(d.compareTo(data[node]) < 0)
			return insert(d,left(node));
		else if(d.compareTo(data[node]) > 0)
			return insert(d,right(node));
		else{//data already exists
			data[node].addSeq(d.getFirstSeq());
			data[node].incrementFreq();
			return false;
		}
	}

	/**
	 * Recursive method that returns the height from a given node
	 * @param node - the height from this node
	 * @return
	 */
	private int nodeHeight(int node){
		if(isNull(node))
			return -1;

		return Math.max(nodeHeight(right(node)), nodeHeight(left(node))) + 1;
	}

	/**
	 * Recursive helper method that calculated the size of a sub tree with the given root node
	 * @param root - root node of the sub tree
	 * @return - the size of the sub tree
	 */
	private int subTreeSize(int root){
		if(isNull(root))
			return 0;

		return subTreeSize(left(root)) + subTreeSize(right(root)) + 1;
	}

	/**
	 * Recursive helper method that calculates the total depth of a sub tree
	 * @param root - the root of the sub tree
	 * @param depth - the depth of the root of the sub tree
	 * @return
	 */
	private int totalDepth(int root, int depth){
		if(isNull(root))
			return 0;
			
		return totalDepth(left(root), depth+1) + totalDepth(right(root), depth+1) + depth;
	}

	/**
	 * Method that balances the BST into an AVL tree  
	 * @param node - the node that is off balance
	 */
	private void balance(int node){	
		int factor = nodeHeight(right(node)) - nodeHeight(left(node));

		if(Math.abs(factor) < 2 && !isNull(parent(node)))
			balance(parent(node));
		else{//balance
			if(factor > 1){//right heavy 
				//check right node
				int rFactor = nodeHeight(right(right(node))) - nodeHeight(left(right(node)));
				if(rFactor > 0){
					//single left rotation with node
					singRot(node,false);
				}
				else{
					//double rotation
					//1. single right rotation with right(node)
					singRot(right(node),true);
					//2. single left rotation with node
					singRot(node,false);
				}
			}
			else if (factor < -1){//left heavy
				//check left node
				int lFactor = nodeHeight(right(left(node))) - nodeHeight(left(left(node)));
				if(lFactor < 0){
					//single right rotation with node
					singRot(node,true);
				}
				else{
					//double rotation 
					//1. single left rotation with left(node)
					singRot(left(node),false);
					//2. single right rotation with node
					singRot(node,true);
				}
			}
		}
	}	

	/**
	 * Method that right-rotates node 
	 * @param node - the node that needs to be rotated
	 * @param right - true if right rotation, false if left rotation  
	 */
	private void singRot(int node, boolean rightRot){		
		//FRIST copy all the data that needs to be moved
		GO rootData = data[node];
		int childIndex = rightRot? left(node) : right(node);
		GO childData = data[childIndex];
		//copy the opposite sub tree
		int oppositeSubTreeIndex = rightRot? right(node) : left(node);//opposite of the child index
		ArrayBST<GO> oppositeSubTree = cpySubTree(oppositeSubTreeIndex);
		//copy the adopted sub tree
		int adpotedSubTreeIndex = rightRot? right(childIndex) : left(childIndex);//sub tree that changes parent
		ArrayBST<GO> adpotedSubTree = cpySubTree(adpotedSubTreeIndex);
		//copy the carried sub tree
		int carriedSubTreeIndex = rightRot? left(childIndex) : right(childIndex);//sub tree that gets carried along
		ArrayBST<GO> carriedSubTree = cpySubTree(carriedSubTreeIndex);


		//START "moving" data
		data[node] = childData;
		data[oppositeSubTreeIndex] = rootData;
		//move the opposite sub tree (oST)
		int oSTDestIndex = rightRot? right(oppositeSubTreeIndex) : left(oppositeSubTreeIndex);
		replaceSubTree(oppositeSubTree, oSTDestIndex);

		//move the adopted sub tree (aST)
		int aSTDestIndex = rightRot? left(oppositeSubTreeIndex) : right(oppositeSubTreeIndex);
		replaceSubTree(adpotedSubTree, aSTDestIndex);

		//move the carried sub tree (cST)
		int cSTDestIndex = childIndex;
		replaceSubTree(carriedSubTree, cSTDestIndex);
	}

	/**
	 * Method that copies a sub tree in the tree
	 * @param root - root of the sub tree
	 * @param size - size of the sub tree
	 * @return - returns a copy of the sub tree
	 */
	private ArrayBST<GO> cpySubTree(int root){
		ArrayBST<GO> copy = new ArrayBST<GO>((int) Math.pow(2, nodeHeight(root)+1));
		cpyElements(copy, root);

		return copy;
	}

	/**
	 * Recursive helper method that copied all the nodes in a sub tree
	 * @param tree - blank tree to copy the elements into.
	 * @param root - root of the sub tree to be copied
	 */
	private void cpyElements(ArrayBST<GO> tree, int root){
		if(!isNull(root))
			tree.insert(data[root]);

		if(hasLeft(root))
			cpyElements(tree,left(root));

		if(hasRight(root))
			cpyElements(tree,right(root));
	}

	/**
	 * Method that replaces a sub tree with another
	 * @param rep - the sub tree that will replace the old one
	 * @param dest - the destination node index of the root to be replaced
	 */
	private void replaceSubTree(ArrayBST<GO> rep, int dest){
		replaceElement(rep, dest, 1);
	}

	/**
	 * Recursive helper method that replaces data in a sub tree from another sub tree
	 * @param rep - sub tree, from which data in taken from 
	 * @param dest - node index of where to replace data
	 * @param node - node index in rep of what data to replace with
	 */
	private void replaceElement(ArrayBST<GO> rep, int dest, int node){
		if(dest < data.length)
			data[dest] = rep.get(node);

		if(left(dest) < data.length)
			replaceElement(rep, left(dest), left(node));

		if(right(dest) < data.length)
			replaceElement(rep, right(dest), right(node));
	}

	/**
	 * Recursive helper method to walk over the tree in order
	 * @param worker - the functional object that gets to accept this node's data
	 * @param node - the tree node whose data is to be accepted by the worker
	 */
	private void inOrder(Consumer<GO> worker, int node) {
		if(hasLeft(node))
			inOrder(worker, left(node));

		worker.accept(data[node]);

		if(hasRight(node))
			inOrder(worker, right(node));
	}

}
