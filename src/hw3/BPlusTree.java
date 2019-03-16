package hw3;


import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class BPlusTree {
	
	private Node root;
	private int pInner;
	private int pLeaf;
    
    public BPlusTree(int pInner, int pLeaf) {
    	this.pInner = pInner;
    	this.pLeaf = pLeaf;
    	this.root = new LeafNode(this.pLeaf);
    }
    
   
    /*finds out if the leaf node returned from the previous function
     * actually does contain field f, if it does it returns the leafnode,
     * if not it returns null
     */
    public LeafNode search(Field f) {
    	LeafNode accessedNode = this.searchLeafNode(f);
    	for(Entry entry : ((LeafNode) accessedNode).getEntries()) {
    		if(f.compare(RelationalOperator.EQ, entry.getField())) {
    			return accessedNode;
    		}
    	}
    	return null;
    }
    
    public void insert(Entry e) {
    	Field field = e.getField();
    	LeafNode leafNode = this.searchLeafNode(field);
    	if (!leafNode.isFull()) {
    		leafNode.insert(e);
    	}
    	else {
    		ArrayList<Entry> sortedList = new ArrayList<Entry>(leafNode.getEntries());
			ArrayList<Entry> newNodeData = new ArrayList<Entry>();
			Boolean added = false;
			for(int i = 0; i < sortedList.size(); i++) {
				if (e.getField().compare(RelationalOperator.LTE, sortedList.get(i).getField())) {
					sortedList.add(i, e);
					added = true;
					break;
				}
				if (!added) {
					sortedList.add(e);
					break;
				}
			}
			int indexToRemove = Math.floorDiv(sortedList.size(), 2);
			System.out.println("Index to remove is: " + indexToRemove);
			while(leafNode.getEntries().size() > indexToRemove) {
				System.out.println(leafNode.getEntries().size());
				newNodeData.add(leafNode.getEntries().get(indexToRemove));
				leafNode.getEntries().remove(indexToRemove);
			}
			LeafNode splitLeafNode = new LeafNode(this.pLeaf);
			splitLeafNode.setEntries(newNodeData);
    		if (leafNode.getParent() == null) {
    			InnerNode newRoot = new InnerNode(this.pInner);
    			ArrayList<Field> keys = new ArrayList<Field>();
    			ArrayList<Node> children = new ArrayList<Node>();
    			keys.add(splitLeafNode.getEntries().get(0).getField());
    			children.add(leafNode);
    			children.add(splitLeafNode);
    			newRoot.setKeys(keys);
    			newRoot.setChildren(children);
    			this.root = newRoot;
    		}
    	}
    }
    
    public void delete(Entry e) {
    	//your code here
    }
    
    public Node getRoot() {
    	return root;
    }
    
    //searches for the leaf node that *should* contain field f 
    public LeafNode searchLeafNode(Field f) {
    	Node accessedNode = root;
    	while(!accessedNode.isLeafNode()) {
    		accessedNode = accessedNode.findMatch(f);
    	}
    	if (((LeafNode) accessedNode).getEntries().size() == 0) {
    		return (LeafNode) accessedNode;
    	}
    	return (LeafNode) accessedNode;
    }
    
}
