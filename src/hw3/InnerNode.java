package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class InnerNode implements Node {
	
	private int degree; //number of children of the node
	private ArrayList<Field> keys;
	private ArrayList<Node> children;
	
	public InnerNode(int degree) {
		this.degree = degree;
	}
	
	public ArrayList<Field> getKeys() {
		return keys;
	}
	
	public ArrayList<Node> getChildren() {
		return children;
	}

	public int getDegree() {
		return degree;
	}
	
	public boolean isLeafNode() {
		return false;
	}
	
	public boolean isFull() {
		if(keys.size() == degree) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public Node findMatch(Field f) {
		int counter = 0;
		if(f.compare(RelationalOperator.LT, keys.get(counter))) {
			return children.get(counter);
		}
		if(f.compare(RelationalOperator.GT, keys.get(keys.size() - 1))) {
			return children.get(keys.size());
		}
		while (!(f.compare(RelationalOperator.GT, keys.get(counter)) && f.compare(RelationalOperator.LT, keys.get(counter + 1)))) {
			counter ++;
		}
		return children.get(counter + 1);
	}

}