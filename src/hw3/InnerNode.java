package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class InnerNode implements Node {

	private int degree; // number of children of the node
	private ArrayList<Field> keys;
	private ArrayList<Node> children;
	private InnerNode parent;
	private int parentIndex;

	public InnerNode(int degree) {
		this.degree = degree;
		this.children = new ArrayList<Node>();
		this.keys = new ArrayList<Field>();
		this.parent = null;
	}

	public ArrayList<Field> getKeys() {
		return this.keys;
	}

	public ArrayList<Node> getChildren() {
		return this.children;
	}

	public int getDegree() {
		return this.degree;
	}

	public InnerNode getParent() {
		return this.parent;
	}
	
	public int getParentIndex() {
		return this.parentIndex;
	}

	public void setParent(InnerNode parent) {
		this.parent = parent;
	}

	public void setChildren(ArrayList<Node> children) {
		this.children = children;
	}

	public void setKeys(ArrayList<Field> keys) {
		this.keys = keys;
	}

	public void setParentIndex(int index) {
		this.parentIndex = index;
	}

	public boolean isLeafNode() {
		return false;
	}

	public boolean isOverflowing() {
		if (keys.size() >= degree) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isFull() {
		if (keys.size() == degree) {
			return true;
		} else {
			return false;
		}
	}

	public void updateKeys() {
//		for (int i = 0; i < children.size() - 1; i++) {
//			if (children.get(0).isLeafNode()) {
//				int size = ((LeafNode) children.get(i)).getEntries().size();
//				// if index in key array has not yet been initialized- space has never been used
//				if (keys.size() == 0) {
//					System.out.println("here 3");
//					this.keys.add(i, ((LeafNode) this.children.get(i)).getEntries().get(size - 1).getField());
//				}
////				else if () {
////					
////				}
//				// if space is already used but need to update key
//				else {
//					System.out.println("here2!!");
//					this.keys.set(i, ((LeafNode) this.children.get(i)).getEntries().get(size - 1).getField());
//				}
//				// TODO: need to delete all keys with children that have been deleted
//			}
//		}
		
		ArrayList<Field> newKeys = new ArrayList<Field>();
		for (Node child: children) {
			int size = ((LeafNode) child).getEntries().size();
			newKeys.add(((LeafNode) child).getEntries().get(size - 1).getField());
		}
		newKeys.remove(newKeys.size() - 1);
		this.keys = newKeys;
	}

	public Node findMatch(Field f) {
		int counter = 0;
		if (f.compare(RelationalOperator.LTE, keys.get(counter))) {
			return children.get(counter);
		}
		if (f.compare(RelationalOperator.GT, keys.get(keys.size() - 1))) {
			return children.get(keys.size());
		}
		while (!(f.compare(RelationalOperator.GT, keys.get(counter))
				&& f.compare(RelationalOperator.LTE, keys.get(counter + 1)))) {
			counter++;
		}
		return children.get(counter + 1);
	}

}