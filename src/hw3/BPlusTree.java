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
    	Node node = this.searchLeafNode(field);
//    	Node leafNode = this.searchLeafNode(field);
//    	if (!node.isFull()) {
//    		((LeafNode) node).insert(e);
//    	}
//    	else {
    	while (node.isFull()) {
    		LeafNode leafNode = (LeafNode) node;
//    		if (node.isLeafNode()) {
//    			LeafNode leafNode = (LeafNode) node;
//    		}
//    		else {
//    			InnerNode innerNode = (InnerNode) node;
//    		}
    		//split leaf node into 2 leaf nodes
			ArrayList<Entry> newNodeData = new ArrayList<Entry>();
			Boolean added = false;
			for(int i = 0; i < leafNode.getEntries().size(); i++) {
				if (e.getField().compare(RelationalOperator.LTE, leafNode.getEntries().get(i).getField())) {
					leafNode.getEntries().add(i, e);
					added = true;
					break;
				}
				if (!added) {
					leafNode.getEntries().add(e);
					break;
				}
			}
			int indexToRemove = Math.floorDiv(leafNode.getEntries().size(), 2);
			while(leafNode.getEntries().size() > indexToRemove) {
				newNodeData.add(leafNode.getEntries().get(indexToRemove));
				leafNode.getEntries().remove(indexToRemove);
			}
			LeafNode splitLeafNode = new LeafNode(this.pLeaf);
			splitLeafNode.setEntries(newNodeData);
			
			//if the leaf node being split is the root
    		if (leafNode.getParent() == null) {
    			InnerNode newRoot = new InnerNode(this.pInner);
    			ArrayList<Field> keys = new ArrayList<Field>();
    			ArrayList<Node> children = new ArrayList<Node>();
    			keys.add(splitLeafNode.getEntries().get(0).getField());
    			children.add(leafNode);
    			children.add(splitLeafNode);
    			newRoot.setKeys(keys);
    			newRoot.setChildren(children);
    			leafNode.setParent(newRoot);
    			leafNode.setParentIndex(0);
    			splitLeafNode.setParent(newRoot);
    			splitLeafNode.setParentIndex(1);
    			this.root = newRoot;
    		}
    	}
    	((LeafNode) node).insert(e);
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
