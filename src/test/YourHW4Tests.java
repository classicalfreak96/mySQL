package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Before;
import org.junit.Test;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.IntField;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw4.BufferPool;
import hw4.Permissions;

public class YourHW4Tests {
	
	private Catalog c;
	private BufferPool bp;
	private HeapFile hf;
	private TupleDesc td;
	private int tid;
	
	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		
		int tableId = c.getTableId("test");
		td = c.getTupleDesc(tableId);
		hf = c.getDbFile(tableId);
		
		bp = Database.getBufferPool();
		
		tid = c.getTableId("test");
	}

	@Test
	public void testLock() throws Exception {
	    bp.getPage(0, tid, 0, Permissions.READ_WRITE);
	    bp.getPage(0, tid, 0, Permissions.READ_WRITE);
	    assertTrue(bp.holdsLock(0, tid, 0) == true);
	    bp.transactionComplete(0, true);
	    assertTrue(bp.holdsLock(0, tid, 0) == false);
	}

	@Test
	public void testWriteWithLock() throws Exception {
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		
		boolean thrown = false;
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
		try {
			bp.insertTuple(0, tid, t);
		}
		catch (Exception e) {
			thrown = true;
		}
		
		assertTrue(thrown == true);
	}
}
