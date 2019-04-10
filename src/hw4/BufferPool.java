package hw4;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapPage;
import hw1.Tuple;

/**
 * BufferPool manages the reading and writing of pages into memory from disk.
 * Access methods call into it to retrieve pages, and it fetches pages from the
 * appropriate location.
 * <p>
 * The BufferPool is also responsible for locking; when a transaction fetches a
 * page, BufferPool which check that the transaction has the appropriate locks
 * to read/write the page.
 */
public class BufferPool {
	/** Bytes per page, including header. */
	public static final int PAGE_SIZE = 4096;

	/**
	 * Default number of pages passed to the constructor. This is used by other
	 * classes. BufferPool should use the numPages argument to the constructor
	 * instead.
	 */
	public static final int DEFAULT_PAGES = 50;
	private Map<Integer, HeapPage> cache = new HashMap<Integer, HeapPage>();  //Map<page ID, heapPage>
	private Map<Integer, Boolean> isDirty = new HashMap<Integer, Boolean>();  //Map <page ID, if dirty then true, else false
	private Map<Integer, Map<Integer, Permissions>> pageLocks = new HashMap<Integer, Map<Integer, Permissions>>(); // Map<page ID, Map<transaction ID, lock type>>
	private Map<Integer, ArrayList<int[]>> transactionPageMap = new HashMap<Integer, ArrayList<int[]>>();      // Map<transaction ID, ArrayList<[pageID, tableID]>>/ 
	private int maxPages;

	/**
	 * Creates a BufferPool that caches up to numPages pages.
	 *
	 * @param numPages maximum number of pages in this buffer pool.
	 */
	public BufferPool(int numPages) {
//		this.catalog  = Database.getCatalog();
		this.maxPages = numPages;
	}

	/**
	 * Retrieve the specified page with the associated permissions. Will acquire a
	 * lock and may block if that lock is held by another transaction.
	 * <p>
	 * The retrieved page should be looked up in the buffer pool. If it is present,
	 * it should be returned. If it is not present, it should be added to the buffer
	 * pool and returned. If there is insufficient space in the buffer pool, an page
	 * should be evicted and the new page should be added in its place.
	 *
	 * @param tid     the ID of the transaction requesting the page
	 * @param tableId the ID of the table with the requested page
	 * @param pid     the ID of the requested page
	 * @param perm    the requested permissions on the page
	 */
	public HeapPage getPage(int tid, int tableId, int pid, Permissions perm) throws Exception {
//		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		HeapPage heapPage;
		this.addToTransactionPageMap(tid, tableId, pid);
		if (cache.containsKey(pid)) {
//			System.out.println("contains pageID already");
			heapPage = cache.get(pid);
			if (this.hasWriteLock(pid)) { 			//check for existing write lock 
//				System.out.println("has write lock!");
				Map<Integer, Permissions> existingLocks = pageLocks.get(pid);
				if (existingLocks.containsKey(pid)) {		//if existing lock is same as current transaction, just downgrade from write to read
					pageLocks.get(pid).replace(pid, perm);
				}
				else {										//else abort. TODO: block!
					this.transactionComplete(tid, false);
				}
			}
			else {
//				System.out.println("no write lock");
				if (perm.permLevel == 0) {			   //if you want to add a read lock, go ahead
//					System.out.println("adding read lock for transaction: " + tid);
					pageLocks.get(pid).put(tid, perm); //put tid and permissions into pageLock
				}
				else {								   //if you want to add a write lock, must make sure there are no other locks. 
					Map<Integer, Permissions> existingLocks = pageLocks.get(pid);
					if (existingLocks.isEmpty()) {
//						System.out.println("adding write lock for transaction: " + tid);
						pageLocks.get(pid).put(tid, perm);
					}
					else if (existingLocks.size() == 1 && existingLocks.containsKey(pid)) { //upgrade lock to read if its the same transaction and is the only one that exists
//						System.out.println("upgrading read lock of transaction " + tid);
						pageLocks.get(pid).replace(pid, perm);
					}
					else {
//						System.out.println("removing transaction: " + tid);
//						this.printPageLocks();
						this.transactionComplete(tid, false);
//						this.printPageLocks();
					}
				}
			}
		}
		else {
//			System.out.println("caching pageID for first time");
			if (cache.size() >= this.maxPages) {
				this.evictPage();
			}
			heapPage = Database.getCatalog().getDbFile(tableId).readPage(pid); //read heappage from disk 
			this.cache.put(pid, heapPage);									   //add to cache, initialize as not dirty 
			this.isDirty.put(pid, false);
			Map<Integer, Permissions> newPermissions = new HashMap<Integer, Permissions>(); //add permissions to current locks on page 
			newPermissions.put(tid, perm);
			this.pageLocks.put(pid, newPermissions);
		}
		return heapPage;
	}

	/**
	 * Releases the lock on a page. Calling this is very risky, and may result in
	 * wrong behavior. Think hard about who needs to call this and why, and why they
	 * can run the risk of calling it.
	 *
	 * @param tid     the ID of the transaction requesting the unlock
	 * @param tableID the ID of the table containing the page to unlock
	 * @param pid     the ID of the page to unlock
	 */
	public void releasePage(int tid, int tableId, int pid) {
		//TODO: is this similar to my removeTransactionLocks function?
	}

	/** Return true if the specified transaction has a lock on the specified page */
	public boolean holdsLock(int tid, int tableId, int pid) {
		Map<Integer, Permissions> locks = pageLocks.get(pid);
		if (locks == null) {
			return false;
		}
		for (Map.Entry<Integer, Permissions> lock : locks.entrySet()) {
			int transactionID = lock.getKey();
			if (transactionID == tid) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Commit or abort a given transaction; release all locks associated to the
	 * transaction. If the transaction wishes to commit, write
	 *
	 * @param tid    the ID of the transaction requesting the unlock
	 * @param commit a flag indicating whether we should commit or abort
	 */
	public void transactionComplete(int tid, boolean commit) throws IOException {
		if (commit) {
			//TODO: release locks and flush to disk
		}
		else {
			//TODO: delete all transactions related to that transaction, reload fresh copy of page from disk into cache
			for (int[] IDs : this.transactionPageMap.get(tid)) {
				this.removeTransactionLocks(IDs[0], tid);
				this.cache.replace(IDs[0], Database.getCatalog().getDbFile(IDs[1]).readPage(IDs[0]));
				this.isDirty.replace(IDs[0], false);
				this.transactionPageMap.remove(IDs[0]);
			}
		}
//		this.printTransactionPageMap();
		
	}

	/**
	 * Add a tuple to the specified table behalf of transaction tid. Will acquire a
	 * write lock on the page the tuple is added to. May block if the lock cannot be
	 * acquired.
	 * 
	 * Marks any pages that were dirtied by the operation as dirty
	 *
	 * @param tid     the transaction adding the tuple
	 * @param tableId the table to add the tuple to
	 * @param t       the tuple to add
	 */
	public void insertTuple(int tid, int tableId, Tuple t) throws Exception {
		// your code here
	}

	/**
	 * Remove the specified tuple from the buffer pool. Will acquire a write lock on
	 * the page the tuple is removed from. May block if the lock cannot be acquired.
	 *
	 * Marks any pages that were dirtied by the operation as dirty.
	 *
	 * @param tid     the transaction adding the tuple.
	 * @param tableId the ID of the table that contains the tuple to be deleted
	 * @param t       the tuple to add
	 */
	public void deleteTuple(int tid, int tableId, Tuple t) throws Exception {
		// your code here
	}

	private synchronized void flushPage(int tableId, int pid) throws IOException {
		HeapPage page = this.cache.get(pid);
		Database.getCatalog().getDbFile(tableId).writePage(page);
		this.isDirty.replace(pid, false);
	}

	/**
	 * Discards a page from the buffer pool. Flushes the page to disk to ensure
	 * dirty pages are updated on disk.
	 */
	private synchronized void evictPage() throws Exception {
		for (Map.Entry<Integer, Boolean> entry : isDirty.entrySet()) {
			int pageID = entry.getKey();
			Boolean dirty = entry.getValue();
			if (!dirty) {
				isDirty.remove(pageID);
				cache.remove(pageID);
				break;
			}
		}
	}
	
	//helper functions
	
	private boolean hasWriteLock(int pid) {
		Map<Integer, Permissions> locks = pageLocks.get(pid);
		if (locks == null) {
			return false;
		}
		for (Map.Entry<Integer, Permissions> lock : locks.entrySet()) {
			Permissions perm = lock.getValue();
			if (perm.permLevel == 1) {
				return true;
			}
		}
		return false;
	}
	
	private void addToTransactionPageMap(int transactionID, int tableID, int pageID) {
		if (this.transactionPageMap.containsKey(transactionID)) {
			ArrayList<int[]> IDs = transactionPageMap.get(transactionID);
			boolean add = true;
			for (int[] id : IDs) {
				if (id[0] == pageID) {
					add = false;
					break;
				}
			}
			if (add) {
				int[] toAdd = {pageID, tableID};
				IDs.add(toAdd);
			}
		}
		else {
			ArrayList<int[]> IDs = new ArrayList<int[]>();
			int[] toAdd = {pageID, tableID};
			IDs.add(toAdd);
			this.transactionPageMap.put(transactionID, IDs);
		}
	}
	
	private void removeTransactionLocks(int pageID, int transactionID) {
		Map<Integer, Permissions> locks = pageLocks.get(pageID);
		for (Map.Entry<Integer, Permissions> lock : locks.entrySet()) {
			if (transactionID == lock.getKey()) {
				locks.remove(lock.getKey());
				break;
			}
		}
	}
	
	
	//debugging
	private void printPageLocks() {
		System.out.println("PRINTING PAGE LOCKS");
		for (Map.Entry<Integer, Map<Integer, Permissions>> entry : this.pageLocks.entrySet()) {
			System.out.println("Page id: " + entry.getKey());
			for (Map.Entry<Integer, Permissions> entry1 : entry.getValue().entrySet()) {
				System.out.println("Transaction id: " + entry1.getKey());
				System.out.println("Permission: " + entry1.getValue().toString());
			}
		}
	}
	
	private void printTransactionPageMap() {
		System.out.println("PRINTING TRANSACTRION PAGE MAPPING");
		for (Map.Entry<Integer, ArrayList<int[]>> entry : this.transactionPageMap.entrySet()) {
			System.out.println("Transaction id: " + entry.getKey());
			for (int[] array : entry.getValue()) {
				System.out.println("page ID: " + array[0]);
				System.out.println("tableID: " + array[1]);
			}
		}
	}

}
