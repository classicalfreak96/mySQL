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
import hw1.Field;
import hw1.HeapFile;
import hw1.IntField;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;
import hw2.Relation;

public class YourHW2Tests {

	private HeapFile testhf;
	private TupleDesc testtd;
	private HeapFile ahf;
	private TupleDesc atd;
	private Catalog c;

	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File("testfiles/A.dat.bak").toPath(), new File("testfiles/A.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		
		int tableId = c.getTableId("test");
		testtd = c.getTupleDesc(tableId);
		testhf = c.getDbFile(tableId);
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/A.txt");
		
		tableId = c.getTableId("A");
		atd = c.getTupleDesc(tableId);
		ahf = c.getDbFile(tableId);
	}
	
	@Test
	//re-commit
	public void testRelationToString() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		String s = ar.toString();
		String tupleDescString = ar.getDesc().toString();
		assertTrue("TupleDesc toString does not contain tupleDesc", s.contains(tupleDescString));
		for (int i = 0; i < ar.getTuples().size(); i++) {
			for (int j = 0; j < ar.getDesc().getSize(); j++) {
				Type type = ar.getDesc().getType(j);
				Field field = ar.getTuples().get(i).getField(j);
				if (type == Type.STRING) {
					StringField newString = new StringField(field.toByteArray());
					assertTrue("Relation does not contain string " + newString.toString(), s.contains(newString.toString()));
				}
				else if (type == Type.INT) {
					IntField newInt = new IntField(field.toByteArray());
					assertTrue("Relation does not contain int " + newInt.toString(), s.contains(newInt.toString()));
				}
			}
		}
	}

}
