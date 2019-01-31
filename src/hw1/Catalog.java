package hw1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * The Catalog keeps track of all available tables in the database and their
 * associated schemas.
 * For now, this is a stub catalog that must be populated with tables by a
 * user program before it can be used -- eventually, this should be converted
 * to a catalog that reads a catalog table from disk.
 */

// QUESTIONS: getTableId() is the heap file's id the same as the table's id? Or can it be the key (int)
// 				in our hashmap?
// tableIdIterator() did we do this correctly? Shouldn't it be iterating over table id's?
// In general: what is a table id? Who makes it?

public class Catalog {
	
//	private ArrayList<TableSchema> tables;
	private HashMap<Integer, TableSchema> tables;
    /**
     * Constructor.
     * Creates a new, empty catalog.
     */
    public Catalog() {
    		this.tables = new HashMap<Integer, TableSchema>();
    }

    /**
     * Add a new table to the catalog.
     * This table's contents are stored in the specified HeapFile.
     * @param file the contents of the table to add;  file.getId() is the identfier of
     *    this file/tupledesc param for the calls getTupleDesc and getFile
     * @param name the name of the table -- may be an empty string.  May not be null.  If a name conflict exists, use the last table to be added as the table for a given name.
     * @param pkeyField the name of the primary key field
     */
    public void addTable(HeapFile file, String name, String pkeyField) {
    		// need to check for naming conflicts
    		Object[] keys = tables.keySet().toArray();
    		for(int i = 0; i < keys.length; i++) {
    			Object keyID = keys[i];
    			TableSchema ts = this.tables.get(keyID);
    			if (ts.name == name) {
    				this.tables.remove(keyID);
    				break;
    			}
    		}
    		this.tables.put(file.getId(), new TableSchema(name, file, pkeyField));
    }

    public void addTable(HeapFile file, String name) {
        addTable(file,name,"");
    }

    /**
     * Return the id of the table with a specified name,
     * @throws NoSuchElementException if the table doesn't exist
     */
    public int getTableId(String name) throws NoSuchElementException {
    		// not sure if this is right (is HeapFile's getId method the same as the table's id?)
    		Object[] keys = tables.keySet().toArray();
    		for (int i = 0; i < keys.length; i++) {
    			TableSchema ts = this.tables.get(keys[i]);
    			if (ts.name.equals(name)) {
    				return ts.heapfile.getId();
    			}
    		}
    		throw new NoSuchElementException();
    }

    /**
     * Returns the tuple descriptor (schema) of the specified table
     * @param tableid The id of the table, as specified by the DbFile.getId()
     *     function passed to addTable
     */
    public TupleDesc getTupleDesc(int tableid) throws NoSuchElementException {
    	try {
    		return tables.get(tableid).getDesc();
    	}
    	catch (Exception e) {
    		throw new NoSuchElementException();
    	}
    }

    /**
     * Returns the HeapFile that can be used to read the contents of the
     * specified table.
     * @param tableid The id of the table, as specified by the HeapFile.getId()
     *     function passed to addTable
     */
    public HeapFile getDbFile(int tableid) throws NoSuchElementException {
    	try {
    		return tables.get(tableid).getHeapFile();
    	}
    	catch (Exception e){
    		throw new NoSuchElementException();
    	}
    }

    /** Delete all tables from the catalog */
    public void clear() {
    		this.tables = new HashMap<Integer, TableSchema>();
    }
    
    // not sure if throwing an exception here is necessary
    public String getPrimaryKey(int tableid) throws NoSuchElementException{
    	try {
    		return tables.get(tableid).pk;
    	}
    	catch (Exception e) {
    		throw new NoSuchElementException();
    	}
    }
    
    // does this mean we need a separate list just for table id's?
    public Iterator<Integer> tableIdIterator() {
    		return this.tables.keySet().iterator();
    }
    
    // are we allowed to throw an exception if the id does not exist?
    public String getTableName(int id) throws NoSuchElementException {
    	try {
    		return tables.get(id).name;
    	}
    	catch (Exception e) {
    		throw new NoSuchElementException();
    	}
    }
    
    /**
     * Reads the schema from a file and creates the appropriate tables in the database.
     * @param catalogFile
     */
    public void loadSchema(String catalogFile) {
        String line = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(catalogFile)));

            while ((line = br.readLine()) != null) {
                //assume line is of the format name (field type, field type, ...)
                String name = line.substring(0, line.indexOf("(")).trim();
                //System.out.println("TABLE NAME: " + name);
                String fields = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                String[] els = fields.split(",");
                ArrayList<String> names = new ArrayList<String>();
                ArrayList<Type> types = new ArrayList<Type>();
                String primaryKey = "";
                for (String e : els) {
                    String[] els2 = e.trim().split(" ");
                    names.add(els2[0].trim());
                    if (els2[1].trim().toLowerCase().equals("int"))
                        types.add(Type.INT);
                    else if (els2[1].trim().toLowerCase().equals("string"))
                        types.add(Type.STRING);
                    else {
                        System.out.println("Unknown type " + els2[1]);
                        System.exit(0);
                    }
                    if (els2.length == 3) {
                        if (els2[2].trim().equals("pk"))
                            primaryKey = els2[0].trim();
                        else {
                            System.out.println("Unknown annotation " + els2[2]);
                            System.exit(0);
                        }
                    }
                }
                Type[] typeAr = types.toArray(new Type[0]);
                String[] namesAr = names.toArray(new String[0]);
                TupleDesc t = new TupleDesc(typeAr, namesAr);
                HeapFile tabHf = new HeapFile(new File("testfiles/" + name + ".dat"), t);
                addTable(tabHf,name,primaryKey);
                System.out.println("Added table : " + name + " with schema " + t);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IndexOutOfBoundsException e) {
            System.out.println ("Invalid catalog entry : " + line);
            System.exit(0);
        }
    }
    
    // Private inner class
    // a table's metadata
    private class TableSchema {
    		private String name;
    		private HeapFile heapfile;
    		private String pk;
    		private int tableID;
    		
    		public TableSchema(String name, HeapFile heapfile, String pk) {
    			this.name = name;
    			this.heapfile = heapfile;
    			this.pk = pk;	
    			this.tableID = this.heapfile.getId();
    		}
    		
    		public HeapFile getHeapFile() {
    			return this.heapfile;
    		}
    		
    		public TupleDesc getDesc() {
    			return heapfile.getTupleDesc();
    		}
    }
}

