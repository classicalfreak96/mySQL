package hw1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A heap file stores a collection of tuples. It is also responsible for managing pages.
 * It needs to be able to manage page creation as well as correctly manipulating pages
 * when tuples are added or deleted.
 * @author Sam Madden modified by Doug Shook
 *
 */
public class HeapFile {
	
	public static final int PAGE_SIZE = 4096;
	private File file;
	private TupleDesc type;
	private int numHeapPages;
	
	/**
	 * Creates a new heap file in the given location that can accept tuples of the given type
	 * @param f location of the heap file
	 * @param types type of tuples contained in the file
	 */
	public HeapFile(File f, TupleDesc type) {
		this.file = f;
		this.type = type;
		this.numHeapPages = this.getNumPages();
	}
	
	public File getFile() {
		return this.file;
	}
	
	public TupleDesc getTupleDesc() {
		return this.type;
	}
	
	/**
	 * Creates a HeapPage object representing the page at the given page number.
	 * Because it will be necessary to arbitrarily move around the file, a RandomAccessFile object
	 * should be used here.
	 * @param id the page number to be retrieved
	 * @return a HeapPage at the given page number
	 * @throws IOException 
	 */
	public HeapPage readPage(int id) {
		byte[] readData = new byte[HeapFile.PAGE_SIZE];
		int startByte = HeapFile.PAGE_SIZE * id;
		try {
			RandomAccessFile theFile = new RandomAccessFile(this.file, "r");
			theFile.seek(startByte);
			theFile.read(readData);
			theFile.close();
			return new HeapPage(id, readData, this.getId());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns a unique id number for this heap file. Consider using
	 * the hash of the File itself.
	 * @return
	 */
	public int getId() {
		return this.file.hashCode();
	}
	
	/**
	 * Writes the given HeapPage to disk. Because of the need to seek through the file,
	 * a RandomAccessFile object should be used in this method.
	 * @param p the page to write to disk
	 */
	public void writePage(HeapPage p) {
		//your code here
		int startByte = HeapFile.PAGE_SIZE * p.getId();
		try {
			RandomAccessFile theFile = new RandomAccessFile(this.file, "rw");
			theFile.seek(startByte);
			theFile.write(p.getPageData());
			theFile.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a tuple. This method must first find a page with an open slot, creating a new page
	 * if all others are full. It then passes the tuple to this page to be stored. It then writes
	 * the page to disk (see writePage)
	 * @param t The tuple to be stored
	 * @return The HeapPage that contains the tuple
	 */
	public HeapPage addTuple(Tuple t) {
		//your code here
		for(int i = 0; i < this.numHeapPages; i++) {
			HeapPage temp = this.readPage(i);
			try {
				temp.addTuple(t);
				this.writePage(temp);
				return temp;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		HeapPage hp = this.readPage(this.numHeapPages);
		try {
			hp.addTuple(t);
			this.writePage(hp);
			this.numHeapPages++;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return hp;
	}
	
	/**
	 * This method will examine the tuple to find out where it is stored, then delete it
	 * from the proper HeapPage. It then writes the modified page to disk.
	 * @param t the Tuple to be deleted
	 */
	public void deleteTuple(Tuple t){
		//your code here
	}
	
	/**
	 * Returns an ArrayList containing all of the tuples in this HeapFile. It must
	 * access each HeapPage to do this (see iterator() in HeapPage)
	 * @return
	 */
	public ArrayList<Tuple> getAllTuples() {
		ArrayList<Tuple> tuples = new ArrayList<>();
		for(int i = 0; i < this.numHeapPages; i++) {
			Iterator<Tuple> iter = this.readPage(i).iterator();
			iter.forEachRemaining(tuples::add);
		}
		return tuples;
	}
	
	/**
	 * Computes and returns the total number of pages contained in this HeapFile
	 * @return the number of pages
	 */
	public int getNumPages() {
		return (int)Math.ceil((this.file.length() / HeapFile.PAGE_SIZE));
	}
}
