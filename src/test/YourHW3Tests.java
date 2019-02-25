package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw3.BPlusTree;
import hw3.Entry;
import hw3.InnerNode;
import hw3.LeafNode;
import hw3.Node;

public class YourHW3Tests {
	
	@Test
	public void testDifferentDegrees() {
		BPlusTree bt = new BPlusTree(3, 3);
		
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(5), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(18), 0));
		bt.insert(new Entry(new IntField(20), 0));
		bt.insert(new Entry(new IntField(24), 0));
		
		//verify root properties
		Node root = bt.getRoot();
	
		assertTrue(root.isLeafNode() == false);
	
		InnerNode in = (InnerNode)root; // 7
		
		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();
		
		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(7)));
		
		//grab left and right children from root
		InnerNode l = (InnerNode)c.get(0); // 4
		InnerNode r = (InnerNode)c.get(1); // 10, 17
		
		assertTrue(l.isLeafNode() == false);
		assertTrue(r.isLeafNode() == false);
		
		//check values in left node
		ArrayList<Field> kl = l.getKeys(); // 2
		ArrayList<Node> cl = l.getChildren(); // [1] [2]
		
		assertTrue(kl.get(0).compare(RelationalOperator.EQ, new IntField(2)));
		
		//get left node's children, verify
		Node ll = cl.get(0); // [1]
		Node lr = cl.get(1); // [2]
		
		assertTrue(ll.isLeafNode());
		assertTrue(lr.isLeafNode());
		
		LeafNode lll = (LeafNode)ll;
		LeafNode lrl = (LeafNode)lr;
		
		ArrayList<Entry> ell = lll.getEntries();
		assertTrue(ell.get(0).getField().equals(new IntField(1)));

		ArrayList<Entry> elr = lrl.getEntries();
		assertTrue(elr.get(0).getField().equals(new IntField(2)));
		
		//verify right node
		ArrayList<Field> kr = r.getKeys(); // 12, 18
		ArrayList<Node> cr = r.getChildren(); // [5] [10] [18, 20, 24]

		assertTrue(kr.get(0).compare(RelationalOperator.EQ, new IntField(12)));
		assertTrue(kr.get(1).compare(RelationalOperator.EQ, new IntField(18)));
				
		//get right node's children, verify
		Node rl = cr.get(0);
		Node rm = cr.get(1);
		Node rr = cr.get(2);
				
		assertTrue(rl.isLeafNode());
		assertTrue(rm.isLeafNode());
		assertTrue(rr.isLeafNode());

		LeafNode rll = (LeafNode)rl;
		LeafNode rml = (LeafNode)rm;
		LeafNode rrl = (LeafNode)rr;
				
		ArrayList<Entry> erl = rll.getEntries();
		assertTrue(erl.get(0).getField().equals(new IntField(5)));
		
		ArrayList<Entry> erm = rml.getEntries();
		assertTrue(erm.get(0).getField().equals(new IntField(10)));

		ArrayList<Entry> err = rrl.getEntries();
		assertTrue(err.get(0).getField().equals(new IntField(18)));
		assertTrue(err.get(1).getField().equals(new IntField(20)));
		assertTrue(err.get(2).getField().equals(new IntField(24)));
	}

	@Test
	public void testLeafNodeSplit() {
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(5), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(5), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(8), 0));
		bt.insert(new Entry(new IntField(12), 0));
		
		//verify root properties
		Node root = bt.getRoot();
	
		assertTrue(root.isLeafNode() == false);
	
		InnerNode in = (InnerNode)root; // 5
		
		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();
		
		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(5)));
		
		//grab left and right children from root
		InnerNode l = (InnerNode)c.get(0); // 3
		InnerNode r = (InnerNode)c.get(1); // 8
		
		assertTrue(l.isLeafNode() == false);
		assertTrue(r.isLeafNode() == false);
		
		//check values in left node
		ArrayList<Field> kl = l.getKeys(); // 3
		ArrayList<Node> cl = l.getChildren(); // [1, 3] [5]
		
		assertTrue(kl.get(0).compare(RelationalOperator.EQ, new IntField(3)));
		
		//get left node's children, verify
		Node ll = cl.get(0);
		Node lr = cl.get(1);
		
		assertTrue(ll.isLeafNode());
		assertTrue(lr.isLeafNode());
		
		LeafNode lll = (LeafNode)ll;
		LeafNode lrl = (LeafNode)lr;
		
		ArrayList<Entry> ell = lll.getEntries();

		assertTrue(ell.get(0).getField().equals(new IntField(1)));
		assertTrue(ell.get(1).getField().equals(new IntField(3)));

		ArrayList<Entry> elr = lrl.getEntries();

		assertTrue(elr.get(0).getField().equals(new IntField(5)));

		//verify right node
		ArrayList<Field> kr = r.getKeys(); // 8
		ArrayList<Node> cr = r.getChildren(); // [7, 8] [12]

		assertTrue(kr.get(0).compare(RelationalOperator.EQ, new IntField(8)));
		
		//get right node's children, verify
		Node rl = cr.get(0);
		Node rr = cr.get(1);
		
		assertTrue(rl.isLeafNode());
		assertTrue(rr.isLeafNode());

		LeafNode rll = (LeafNode)rl;
		LeafNode rrl = (LeafNode)rr;
		
		ArrayList<Entry> erl = rll.getEntries();

		assertTrue(erl.get(0).getField().equals(new IntField(7)));
		assertTrue(erl.get(1).getField().equals(new IntField(8)));

		ArrayList<Entry> err = rrl.getEntries();

		assertTrue(err.get(0).getField().equals(new IntField(12)));

	}

}
