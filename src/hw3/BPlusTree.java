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

			this.updateLeafNodes(splitLeafNode);
			
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
					splitParentNode.updateParentOfChildren();
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
					splitParentNode.updateParentOfChildren();
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
			
			InnerNode parent = leafNode.getParent();
			boolean removed = leafNode.removeEntry(e);
			if(!removed) {
				// entry does not exist, do nothing
				return;
			}
			
			int leafThreshold = (int) Math.ceil(this.pLeaf/2.0);
			
			// Case: leaf empty (would only happen if pLeaf=2)
			if(entries.isEmpty()) {
				// update list of children (remove the empty leaf node)
				this.leafNodes.remove(leafNode);
				// remove the child from the parent node
				boolean r = parent.getChildren().remove(leafNode);
				// update the keys
				parent.updateKeys();
			}
			// Case: # leaf values < ceil(pLeaf/2)
			else if(entries.size() < leafThreshold) {
				LeafNode leftSibling = this.getLeftSibling(leafNode);
				LeafNode rightSibling = this.getRightSibling(leafNode);
				if(leftSibling != null) {
					// BORROW LEFT sibling's entry
					boolean borrowed = leafNode.borrowLeft(leftSibling);
					if(!borrowed) {
						// MERGE w/ LEFT sibling's child 
						boolean merged = leafNode.mergeLeft(leftSibling);
						if(merged) {
							// update list of children
							this.leafNodes.remove(leafNode);
							if( ((InnerNode)this.root).getChildren().size() == 1 ) {
								this.root = (Node) leftSibling;
							}
						}
					}
					parent.updateKeys();
					// no need to update the same parent twice
					if(!parent.equals(leftSibling.getParent())) {
						leftSibling.getParent().updateKeys();
					}
				} else if(rightSibling != null) {
					// BORROW RIGHT sibling's entry
					boolean borrowed = leafNode.borrowRight(rightSibling);
					if(!borrowed) {
						// MERGE w/ RIGHT sibling's child
						boolean merged = leafNode.mergeRight(rightSibling);
						if(merged) {
							// update list of children
							this.leafNodes.remove(leafNode);
							if( ((InnerNode)this.root).getChildren().size() == 1 ) {
								this.root = (Node) rightSibling;
							}
						}
					}
					parent.updateKeys();
					// no need to update the same parent twice
					if(!parent.equals(rightSibling.getParent())) {
						rightSibling.getParent().updateKeys();
					}
				}
			} else {
				parent.updateKeys();
			}
			// check that # of children under parent is valid
			int innerThreshold = (int) Math.ceil(this.pInner/2.0);
			// Case: parent has no children
			// Case: # children < ceil(pInner/2)
			if(parent.numberOfChildren() < innerThreshold) {
				InnerNode leftParent = this.getLeftParent(parent);
				InnerNode rightParent = this.getRightParent(parent);
				if(leftParent != null) {	
					// BORROW LEFT parent's child
					boolean borrowed = parent.borrowLeft(leftParent);
					if(!borrowed) {
						// MERGE w/ LEFT parent
						boolean merged = parent.mergeLeft(leftParent);
						// if merged, then parent becomes empty
						this.updateAllKeys(leftParent);
						if( ((InnerNode)this.root).getChildren().size() == 1 ) {
							this.root = leftParent;
						}
					} else {
						this.updateAllKeys(parent);
						
						// not sure if necessary
						leftParent.updateKeys();
					}
				} else if(rightParent != null) {
					// BORROW RIGHT parent's child
					boolean borrowed = parent.borrowRight(rightParent);
					if(!borrowed) {
						// MERGE w/ RIGHT parent
						boolean merged = parent.mergeRight(rightParent);
						// if merged, then parent becomes empty
						this.updateAllKeys(rightParent);
						if( ((InnerNode)this.root).getChildren().size() == 1 ) {
							this.root = rightParent;
						}
					} else {
						this.updateAllKeys(parent);
						
						// not sure if necessary
						rightParent.updateKeys();
					}
				}
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
		if(index < this.leafNodes.size()) {
			return this.leafNodes.get(index);
		}
		return null;
	}
	
	public InnerNode getLeftParent(InnerNode p) {
		int i = 0;
		// no left sibling
		if(this.leafNodes.get(i).getParent().equals(p)) {
			return null;
		}
		for(; i < this.leafNodes.size(); i++) {
			InnerNode lfParent = this.leafNodes.get(i).getParent();
			if(lfParent.equals(p)) {
				break;
			}
		}
		return this.leafNodes.get(i-1).getParent();
	}
	
	public InnerNode getRightParent(InnerNode p) {
		int i = this.leafNodes.size()-1;
		// no right sibling
		if(this.leafNodes.get(i).getParent().equals(p)) {
			return null;
		}
		for(; i < this.leafNodes.size(); i--) {
			InnerNode lfParent = this.leafNodes.get(i).getParent();
			if(lfParent.equals(p)) {
				break;
			}
		}
		return this.leafNodes.get(i+1).getParent();
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
	
	public void updateAllKeys(InnerNode p) {
		if(p.equals(this.root)) {
			p.updateKeys();
			return;
		}
		p.updateKeys();
		InnerNode pParent = p.getParent();
		updateAllKeys(pParent);
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
