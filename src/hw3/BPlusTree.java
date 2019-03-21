package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class BPlusTree {

	private Node root;
	private int pInner;
	private int pLeaf;
	private ArrayList<LeafNode> leafNodes;

	public BPlusTree(int pInner, int pLeaf) {
		this.pInner = pInner;
		this.pLeaf = pLeaf;
		this.root = new LeafNode(this.pLeaf);
		this.leafNodes = new ArrayList<LeafNode>();
		leafNodes.add((LeafNode) this.root);
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
			boolean added = false;
			
			// insert into appropriate position in the leaf node
			for (int i = 0; i < leafNode.getEntries().size(); i++) {
				if (e.getField().compare(RelationalOperator.LTE, leafNode.getEntries().get(i).getField())) {
					leafNode.getEntries().add(i, e);
					added = true;
					break;
				}
			}
			// insert into appropriate position in the leaf node
			if (!added) {
				leafNode.getEntries().add(e);
			}
			// split the leaf node
			int indexToRemove = Math.floorDiv(leafNode.getEntries().size() + 1, 2);
			while (leafNode.getEntries().size() > indexToRemove) {
				newNodeData.add(leafNode.getEntries().get(indexToRemove));
				leafNode.getEntries().remove(indexToRemove);
			}
			// splitLeafNode is the right half
			// leafNode is the left half
			LeafNode splitLeafNode = new LeafNode(this.pLeaf);
			splitLeafNode.setEntries(newNodeData); // at this point we've split the nodes: leafNode and splitLeafNode

			// this.leafNodes.add(splitLeafNode);
			this.updateLeafNodes(splitLeafNode);
			System.out.println("TOTAL CHILDREN " + this.leafNodes.size());
			
			// case: leafNode was originally the root, but inserting caused overflow, so need to create a new root from leaf
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
		Field field = e.getField();
		LeafNode leafNode = this.search(field);
		// if the actual field exists
		if(leafNode != null) {
			ArrayList<Entry> entries = leafNode.getEntries();
			ArrayList<Entry> entries2 = this.leafNodes.get(0).getEntries();
			// ============= DEBUGGING =============
			for(Entry temp : entries2) {
				System.out.print(temp.getField().toString() + ", ");
			}
			
			InnerNode parent = leafNode.getParent();
			boolean removed = leafNode.removeEntry(e);
			if(!removed) {
				// entry does not exist, do nothing
				return;
			}
			
			int leafThreshold = (int) Math.ceil(this.pLeaf/2.0);
			System.out.println("ENTRY: " + field.toString() + " SIZE after remove " + entries.size());
			
			// Case: leaf empty
			if(entries.isEmpty()) {
				// TODO update leafNodes (remove the empty leaf node)
				
				// remove the child from the parent node
				boolean r = parent.getChildren().remove(leafNode);
				// update the keys
				parent.updateKeys();
				return;
			}
			// Case: leaf < ceil(p/2)
			else if(entries.size() < leafThreshold) {
				LeafNode leftSibling = this.getLeftSibling(leafNode);
				LeafNode rightSibling = this.getRightSibling(leafNode);
				if(leftSibling != null) {
					// borrow left
					boolean borrowed = leafNode.borrowLeft(leftSibling);
					if(!borrowed) {
						// merge left
						boolean merged = leafNode.mergeLeft(leftSibling);
						if(merged) {
							parent.getChildren().remove(leafNode);
							// TODO update list of children here
						}
					}
					parent.updateKeys();
					leftSibling.getParent().updateKeys();
				} else if(rightSibling != null) {
					// borrow right
					boolean borrowed = leafNode.borrowRight(rightSibling);
					if(!borrowed) {
						// merge right
						System.out.println("RIGHT SIBLING BEFORE: " + rightSibling.getEntries().get(0).getField().toString());
						boolean merged = leafNode.mergeRight(rightSibling);
						if(merged) {
							System.out.println("RIGHT SIBLING AFTER: " + rightSibling.getEntries().get(0).getField().toString());
							System.out.println("MERGE RIGHT SUCCESSFUL");
							parent.getChildren().remove(leafNode);
							// TODO update list of children here
						}
					}
					parent.updateKeys();
					rightSibling.getParent().updateKeys();
				}
			} else {
				parent.updateKeys();
			}
		}
	}
	
	public LeafNode getLeftSibling(LeafNode lf) {
		int index = this.leafNodes.indexOf(lf) - 1;
		if(index >= 0) {
			return this.leafNodes.get(index);
		}
		return null;
	}
	
	public LeafNode getRightSibling(LeafNode lf) {
		int index = this.leafNodes.indexOf(lf) + 1;
		System.out.println("RIGHT SIBLING INDEX " + index);
		if(index < this.leafNodes.size()) {
			return this.leafNodes.get(index);
		}
		return null;
	}
	
	public void updateLeafNodes(LeafNode lf) {
		if(this.leafNodes.isEmpty()) {
			this.leafNodes.add(lf);
			return;
		}
		int index = this.leafNodes.size()-1;
		// if it is greater than last entry, tag onto end (handles out of bounds exception)
		LeafNode end = this.leafNodes.get(index);
		Entry lastEntry = end.getEntries().get(end.getEntries().size()-1);
		if(lf.getEntries().get(0).getField().compare(RelationalOperator.GTE, lastEntry.getField())) {
			this.leafNodes.add(lf);
			return;
		}
		//else, find where the entry belongs
		index--;
		Entry e = lf.getEntries().get(0);
		for(; index >= 0; index--) {
			ArrayList<Entry> lfEntries = this.leafNodes.get(index).getEntries();
			Entry compareTo = lfEntries.get(lfEntries.size()-1);
			if(e.getField().compare(RelationalOperator.GTE, compareTo.getField())) {
				break;
			}
		}
		this.leafNodes.add(index+1, lf);
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
		return (LeafNode) accessedNode;
	}

}
