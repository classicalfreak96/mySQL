package hw3;

import java.util.ArrayList;

public class LeafNode implements Node {
	
	private int degree;
	private ArrayList<Entry> entries;
	
	public LeafNode(int degree) {
		this.degree = degree;
	}
	
	public ArrayList<Entry> getEntries() {
		return entries;
	}

	public int getDegree() {
		return degree;
	}
	
	public boolean isLeafNode() {
		return true;
	}

}