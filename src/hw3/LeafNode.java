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
		if (this.getEntries().size() == 0) {
			entries.add(entry);
			return;
		}
		if (entry.getField().compare(RelationalOperator.GTE, entries.get(entries.size() - 1).getField())) {
			entries.add(entry);
			return;
		}
		int counter = 0;
		while (!(entry.getField().compare(RelationalOperator.LT, entries.get(counter).getField()))) {
			counter++;
		}
		entries.add(counter, entry);
	}

	public void splitNode(Entry entry) {
		LeafNode newLeafNode = new LeafNode(degree);
		ArrayList<Entry> allEntries = new ArrayList<Entry>(this.entries);
		boolean added = false;
		for (int i = 0; i < allEntries.size(); i++) {
			if (entry.getField().compare(RelationalOperator.LTE, allEntries.get(i).getField())) {
				allEntries.add(i, entry);
				added = true;
			}
		}
		if (!added) {
			allEntries.add(entry);
		}
		int removeIndex = Math.floorDiv(allEntries.size(), 2);
		while (this.entries.size() <= removeIndex) {
			newLeafNode.entries.add(this.entries.get(removeIndex));
			this.entries.remove(removeIndex);
		}
		if (this.parent == null) {

		}
	}

	public Node findMatch(Field f) {
		// TODO Auto-generated method stub
		return null;
	}

}