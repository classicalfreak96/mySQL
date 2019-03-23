package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class InnerNode implements Node {

	private int degree; // number of children of the node
	private ArrayList<Field> keys;
	private ArrayList<Node> children;
	private InnerNode parent;
	private int parentIndex; //same as parentIndex of LeafNode

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
	
	public boolean isFull() {
		if (keys.size() >= degree) {
			return true;
		} else {
			return false;
		}
	}
	
	public int numberOfChildren() {
		return this.children.size();
	}

	//updates all keys based on new children insertions or updated children values 
	public void updateKeys() {
//		System.out.println("================ UPDATE KEYS ================");
		ArrayList<Field> newKeys = new ArrayList<Field>();
		for (Node child: children) {
			if(child.isLeafNode()) {
				int size = ((LeafNode) child).getEntries().size();
				newKeys.add(((LeafNode) child).getEntries().get(size - 1).getField());
			} else {
				int size = ((InnerNode) child).getKeys().size();
				newKeys.add(((InnerNode) child).getKeys().get(size - 1));
//				System.out.println("UPDATE KEYS " + (((InnerNode) child).getKeys().get(size - 1).toString()));
			}
//			System.out.println("UPDATE KEYS " + (((LeafNode) child).getEntries().get(size - 1).getField().toString()));
		}
		newKeys.remove(newKeys.size() - 1);
		
		this.keys = newKeys;
	}
	
	public void updateParentOfChildren() {
		for (Node child : children) {
			child.setParent(this);
		}
	}

	//finds a matching children to return when looking for a field
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
	
	public boolean borrowLeft(InnerNode leftSibling) {
		// BORROW LEFT parent's child
		int innerThreshold = (int) Math.ceil(this.degree/2.0);
		System.out.println(innerThreshold);
		ArrayList<Node> leftChildren = leftSibling.getChildren();
		if(leftChildren.size()-1 < innerThreshold) {
			return false;
		}
		LeafNode leftBorrow = (LeafNode) leftChildren.get(leftChildren.size()-1);
		this.children.add(0, leftBorrow);
		leftChildren.remove(leftBorrow);
		return true;
	}
	
	public boolean borrowRight(InnerNode rightSibling) {
		// BORROW RIGHT parent's child
		int innerThreshold = (int) Math.ceil(this.degree/2.0);
		ArrayList<Node> rightChildren = rightSibling.getChildren();
		if(rightChildren.size()-1 < innerThreshold) {
			return false;
		}
		LeafNode rightBorrow = (LeafNode) rightChildren.get(0);
		parent.getChildren().add(parent.numberOfChildren()-1, rightBorrow);
		rightChildren.remove(rightBorrow);
		return true;
	}
	
	public void mergeLeft(InnerNode leftSibling) {
		ArrayList<Node> leftChildren = leftSibling.getChildren();
		
	}

}