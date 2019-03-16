package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class BPlusTree {

	private Node root;
	private int pInner;
	private int pLeaf;

	public BPlusTree(int pInner, int pLeaf) {
		this.pInner = pInner;
		this.pLeaf = pLeaf;
		this.root = new LeafNode(this.pLeaf);
	}

	/*
	 * finds out if the leaf node returned from the previous function actually does
	 * contain field f, if it does it returns the leafnode, if not it returns null
	 */
	public LeafNode search(Field f) {
		LeafNode accessedNode = this.searchLeafNode(f);
		for (Entry entry : ((LeafNode) accessedNode).getEntries()) {
			if (f.compare(RelationalOperator.EQ, entry.getField())) {
				return accessedNode;
			}
		}
		return null;
	}

	public void insert(Entry e) {
		Field field = e.getField();
		LeafNode leafNode = this.searchLeafNode(field); //finds leaf node to insert into
		
		// if leafNode not full, just insert
		if (!leafNode.isFull()) {
			leafNode.insert(e);
		} 
		//otherwise prepare insertion. places new entry into correct place in existing entries and then splits entries 
		else {
			ArrayList<Entry> newNodeData = new ArrayList<Entry>();
			Boolean added = false;
			for (int i = 0; i < leafNode.getEntries().size(); i++) {
				if (e.getField().compare(RelationalOperator.LTE, leafNode.getEntries().get(i).getField())) {
					leafNode.getEntries().add(i, e);
					added = true;
					break;
				}
			}
			if (!added) {
				leafNode.getEntries().add(e);
			}
			int indexToRemove = Math.floorDiv(leafNode.getEntries().size() + 1, 2);
			while (leafNode.getEntries().size() > indexToRemove) {
				newNodeData.add(leafNode.getEntries().get(indexToRemove));
				leafNode.getEntries().remove(indexToRemove);
			}
			LeafNode splitLeafNode = new LeafNode(this.pLeaf);
			splitLeafNode.setEntries(newNodeData); // at this point we've split the nodes: leafNode and splitLeafNode
			
			// if no parent, create new root from leaf
			if (leafNode.getParent() == null) {
				InnerNode newRoot = new InnerNode(this.pInner);
				ArrayList<Node> children = new ArrayList<Node>();
				children.add(leafNode);
				children.add(splitLeafNode);
				newRoot.setChildren(children);
				newRoot.updateKeys();
				leafNode.setParent(newRoot);
				leafNode.setParentIndex(0);
				splitLeafNode.setParent(newRoot);
				splitLeafNode.setParentIndex(1);
				this.root = newRoot;
			}
			
			// otherwise, add on to parent anyway, will check later if overflowing to split
			else {
				ArrayList<Node> children = new ArrayList<Node>(leafNode.getParent().getChildren());
				splitLeafNode.setParentIndex(leafNode.getParentIndex() + 1);
				splitLeafNode.setParent(leafNode.getParent());
				children.set(leafNode.getParentIndex(), leafNode);
				children.add(splitLeafNode.getParentIndex(), splitLeafNode);
				leafNode.getParent().setChildren(children);
				leafNode.getParent().updateKeys();
			}

			//see if parent needs to split
			InnerNode parentNode = leafNode.getParent();
			while (parentNode.isFull()) {
				// split into 3 parts- left, toPushUp (element to push to parent), right nodes
				int indexToMoveUp = Math.floorDiv(parentNode.getKeys().size() - 1, 2);
				int indexToAccess = indexToMoveUp + 1;
				ArrayList<Field> newKeys = new ArrayList<Field>();
				ArrayList<Node> newChildren = new ArrayList<Node>();
				for (int i = indexToAccess; i < parentNode.getKeys().size(); i++) {
					System.out.println("index to access: " + indexToAccess);
					newKeys.add(parentNode.getKeys().get(indexToAccess));
					newChildren.add(parentNode.getChildren().get(indexToAccess));
					parentNode.getKeys().remove(indexToAccess);
					parentNode.getChildren().remove(indexToAccess);
				}
				newChildren.add(parentNode.getChildren().get(indexToAccess));
				parentNode.getChildren().remove(indexToAccess);
				for (int i = 0; i < newChildren.size(); i++) {
					newChildren.get(i).setParentIndex(i);
				}
				InnerNode splitParentNode = new InnerNode(this.pInner); // split into parentNode and splitParentNode. toPushUp is part of parentNode
				splitParentNode.setKeys(newKeys);
				splitParentNode.setChildren(newChildren);

				// if this is the root node, create new root node and link to splits
				if (parentNode.getParent() == null) {
					InnerNode newRoot = new InnerNode(this.pInner);
					ArrayList<Field> rootKeys = new ArrayList<Field>();
					ArrayList<Node> rootChildren = new ArrayList<Node>();
					rootKeys.add(parentNode.getKeys().get(indexToMoveUp));
					parentNode.getKeys().remove(indexToMoveUp);
					rootChildren.add(parentNode);
					rootChildren.add(splitParentNode);
					parentNode.setParent(newRoot);
					splitParentNode.setParent(newRoot);
					parentNode.setParentIndex(0);
					splitParentNode.setParentIndex(1);
					newRoot.setKeys(rootKeys);
					newRoot.setChildren(rootChildren);
					this.root = newRoot;
					parentNode = (InnerNode) this.root;
				}
				// else push up regardless, set parentNode to parent node and check if full in while loop
				else {
					ArrayList<Node> children = new ArrayList<Node>(parentNode.getParent().getChildren());
					splitParentNode.setParentIndex(parentNode.getParentIndex() + 1);
					splitParentNode.setParent(parentNode.getParent());
					parentNode.getParent().getKeys().add(splitParentNode.getParentIndex(),
							parentNode.getKeys().get(indexToMoveUp));
					parentNode.getKeys().remove(indexToMoveUp);
					children.set(parentNode.getParentIndex(), parentNode);
					children.add(splitParentNode.getParentIndex(), splitParentNode);
					parentNode = parentNode.getParent();
				}

			}
		}

	}

	public void delete(Entry e) {
		// your code here
	}

	public Node getRoot() {
		return root;
	}

	// searches for the leaf node that *should* contain field f
	public LeafNode searchLeafNode(Field f) {
		Node accessedNode = root;
		while (!accessedNode.isLeafNode()) {
			accessedNode = accessedNode.findMatch(f);
		}
		if (((LeafNode) accessedNode).getEntries().size() == 0) {
			return (LeafNode) accessedNode;
		}
		return (LeafNode) accessedNode;
	}

}
