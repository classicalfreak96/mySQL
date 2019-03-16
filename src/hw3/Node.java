package hw3;

import hw1.Field;

public interface Node {
	
	
	public int getDegree();
	public boolean isLeafNode();
	public boolean isOverflowing();
	public boolean isFull();
	public Node findMatch(Field f);
	public void setParentIndex(int index);
	
}
