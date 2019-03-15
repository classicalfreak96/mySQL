package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class LeafNode implements Node {
	
	private int degree;
	private ArrayList<Entry> entries;
	
	public LeafNode(int degree) {
		this.degree = degree;
		this.entries = new ArrayList<Entry>();
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
	
	public boolean isFull() {
		if(entries.size() == degree) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void insert(Entry entry) {
		if (this.getEntries().size() == 0) {
			entries.add(entry);
			return;
		}
		if (entry.getField().compare(RelationalOperator.GTE, entries.get(entries.size()-1).getField())) {
			entries.add(entry);
			return;
		}
		int counter = 0;
		while (! (entry.getField().compare(RelationalOperator.LT, entries.get(counter).getField()))) {
			counter ++;
		}
		entries.add(counter, entry);
	}

	public Node findMatch(Field f) {
		// TODO Auto-generated method stub
		return null;
	}

}