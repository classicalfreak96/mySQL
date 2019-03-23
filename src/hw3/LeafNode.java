package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class LeafNode implements Node {

	private int degree; //number of data points
	private ArrayList<Entry> entries;
	private InnerNode parent;
	private int parentIndex; //the index of the key in the parent on the right that points to this leafNode.
	                         //rightmost leaf node's parent index is parent.getKeys().size() (cannot use to index in parent) 

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
	
	//checks if the leafNode is full or overflowing
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
			this.parent.updateKeys();
		}
	}
	
	public boolean removeEntry(Entry e) {
		Field field = e.getField();
		for(int i = 0; i < this.entries.size(); i++) {
			Entry entry = this.entries.get(i);
			if(field.equals(entry.getField())) {
				this.entries.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public boolean borrowLeft(LeafNode left) {
		ArrayList<Entry> entries = left.getEntries();
		Entry e = entries.get(entries.size()-1);
		int threshold = (int) Math.ceil(entries.size()/2.0);
		if(entries.size()-1 < threshold) {
			return false;
		}
		this.insert(e);
		left.removeEntry(e);
		return true;
	}
	
	public boolean borrowRight(LeafNode right) {
		ArrayList<Entry> entries = right.getEntries();
		int threshold = (int) Math.ceil(this.degree/2.0);
		if(entries.size()-1 < threshold) {
			return false;
		}
		Entry e = entries.get(0);
		this.insert(e);
		right.removeEntry(e);
		return true;
	}
	
	public boolean mergeLeft(LeafNode left) {
		ArrayList<Entry> entries = left.getEntries();
		if (this.entries.size() <= this.degree - entries.size()) {
			for(Entry e : this.entries) {
				left.insert(e);
			}
			return true;
		}
		return false;
	}
	
	public boolean mergeRight(LeafNode right) {
		ArrayList<Entry> entries = right.getEntries();
		if (this.entries.size() <= this.degree - entries.size()) {
			for(Entry e : this.entries) {
				right.insert(e);
			}
			return true;
		}
		return false;
	}
	
	public Node findMatch(Field f) {
		// TODO Auto-generated method stub
		return null;
	}

}