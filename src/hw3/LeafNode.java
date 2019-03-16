package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class LeafNode implements Node {

	private int degree;
	private ArrayList<Entry> entries;
	private InnerNode parent;
	private int parentIndex;

	public LeafNode(int degree) {
		this.degree = degree;
		this.entries = new ArrayList<Entry>();
		this.parent = null;
	}

	public ArrayList<Entry> getEntries() {
		return entries;
	}

	public int getDegree() {
		return degree;
	}

	public InnerNode getParent() {
		return this.parent;
	}
	
	public int getParentIndex() {
		return this.parentIndex;
	}

	public boolean isLeafNode() {
		return true;
	}

	public void setParent(InnerNode node) {
		this.parent = node;
	}

	public void setParentIndex(int index) {
		this.parentIndex = index;
	}

	public void setEntries(ArrayList<Entry> entries) {
		this.entries = entries;
	}

	public boolean isFull() {
		if (entries.size() >= degree) {
			return true;
		} else {
			return false;
		}
	}

	public void insert(Entry entry) {
		//only happens if it is very first insertion (in root)
		if (this.getEntries().size() == 0) { 
			entries.add(entry);
			return;
		}
		//if it is greater than last entry, tag onto end (handles out of bounds exception)
		if (entry.getField().compare(RelationalOperator.GTE, entries.get(entries.size() - 1).getField())) {
			entries.add(entry);
			return;
		}
		//else, find where the entry belongs
		int counter = 0;
		while (!(entry.getField().compare(RelationalOperator.LT, entries.get(counter).getField()))) {
			counter++;
		}
		entries.add(counter, entry);
		//if entry is first, update key in parent node
		if (counter == 0 && this.parent != null && this.parentIndex != 0) {
			this.parent.getKeys().set(this.parentIndex - 1, entry.getField());
		}
	}

	public Node findMatch(Field f) {
		// TODO Auto-generated method stub
		return null;
	}

}