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
import hw1.HeapPage;
import hw1.TupleDesc;
import hw1.Type;

public class YourUnitTests {
	
	private HeapFile hf;
	private TupleDesc td;
	private Catalog c;
	private HeapPage hp;

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
		hp = hf.readPage(0);
	}
	
	@Test
	
	public void testTupleDescHashCode() {
		TupleDesc td1 = new TupleDesc(new Type[]{Type.INT}, new String[]{""});
		TupleDesc td2 = new TupleDesc(new Type[]{Type.INT}, new String[]{""});
		TupleDesc td3 = new TupleDesc(new Type[]{Type.INT, Type.STRING}, new String[]{"", ""});
		TupleDesc td4 = new TupleDesc(new Type[]{Type.INT, Type.STRING}, new String[]{"test", "text"});
		
		assertTrue(td1.hashCode() == td1.hashCode());
		assertTrue(td1.hashCode() == td2.hashCode());
		assertTrue(td3.hashCode() == td3.hashCode());
		assertTrue(td4.hashCode() == td4.hashCode());
		
		assertFalse(td1.hashCode() == td3.hashCode());
		assertFalse(td2.hashCode() == td3.hashCode());
		assertFalse(td1.hashCode() == td4.hashCode());
		assertFalse(td2.hashCode() == td4.hashCode());
		assertFalse(td3.hashCode() == td4.hashCode());
	}

	public void testTupleDescToString(){
		TupleDesc td1 = new TupleDesc(new Type[]{Type.INT}, new String[]{""});
		TupleDesc td2 = new TupleDesc(new Type[]{Type.INT, Type.STRING}, new String[]{"", ""});
		TupleDesc td3 = new TupleDesc(new Type[]{Type.INT, Type.STRING}, new String[]{"test", "text"});
		assertTrue(td1.toString().contentEquals("INT()"));
		assertTrue(td2.toString().contentEquals("INT(), STRING()"));
		assertTrue(td3.toString().contentEquals("INT(test), STRING(text)"));

	
	}

}
