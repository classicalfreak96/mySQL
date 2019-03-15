package hw3;

import java.util.ArrayList;

import hw1.Field;

public class InnerNode implements Node {
	
	private int degree;
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

}