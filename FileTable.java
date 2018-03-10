import java.util.*;
public class FileTable {

	private Vector<FileTableEntry> table;         // the actual entity of this file table
	private Directory dir;        // the root directory 
	private final int UNUSED =0;
	private final int READ =1;
	private final int WRITE =2;
	private final int TOTAL_INODES = 64;		//We have 64 inode in total
	private Vector<Inode> maintainInode;		//The Inode vector to maintain Indode each thread can access

	public FileTable( Directory directory ) { // constructor
		table = new Vector<FileTableEntry>( );     // instantiate a file (structure) table
		dir = directory;           	// receive a reference to the Director
									// from the file system
		maintainInode = new Vector<Inode>(TOTAL_INODES);
		for(short i=0;i<TOTAL_INODES;i++)
		{
			Inode inode = new Inode(i);
			maintainInode.addElement(inode);
		}
	}
	
	public FileTable( Directory directory, boolean format ) { // constructor to initialize variables in format 
		if(format)
		{
			table = new Vector<FileTableEntry>( );     // instantiate a file (structure) table
			dir = directory;           	// receive a reference to the Director
									// from the file system
			maintainInode = new Vector<Inode>(TOTAL_INODES);
			for(short i=0;i<TOTAL_INODES;i++)
			{
				Inode inode = new Inode(i);
				maintainInode.addElement(inode);
			}
		}
	}

	// major public methods
	public synchronized FileTableEntry falloc( String filename, String mode ) {
		// allocate a new file (structure) table entry for this file name
		short iNumber = -1;
		FileTableEntry fte = null;
		Inode inode = null;
		// allocate/retrieve and register the corresponding inode using dir
		while(true){
		  iNumber = dir.namei(filename);		//Finding the Inode with the fileName first
		  if(iNumber>=0)	//If the Inode exist
		  {		
			  inode = maintainInode.elementAt(Integer.valueOf(iNumber));
			  break;
		  }
		  else
		  {
				if(mode.equals("r")){
					return null; //does not allow mode "r" to a file that does not exist
				}
              //for any other mode: "w" or "w+" or "a"
				if((iNumber = dir.ialloc(filename))>=0)	//If any Inode is still available to be used
				{
					inode = new Inode();
					maintainInode.set(Integer.valueOf(iNumber),inode);	//Set the new Inode into maintainInode vector
					break;
				}
				else		//Other cases just return null;
				{
					return null;
				}
		  }
		}

        //only accessed when file exists AND mode is "r"
		if(mode.equals("r"))
		{
			if(inode.flag == WRITE)
			{
				try{
					wait();
				}catch (InterruptedException e){}
			}
			inode.flag = READ;
		}
		else	//Other cases: writing, appending, set flag to 3
		{
			if(inode.flag != UNUSED)
			{
				try{
					wait();
				}catch (InterruptedException e){}
			}
			inode.flag = WRITE;
		}
		// increment this inode's count
		inode.count++;
		// immediately write back this inode to the disk
		inode.toDisk(iNumber);
		// return a reference to this file (structure) table entry
		fte = new FileTableEntry(inode,iNumber,mode);
		table.addElement(fte);
		return fte;

	}

	public synchronized boolean ffree( FileTableEntry entry ) {
	  // receive a file table entry reference
	  // save the corresponding inode to the disk
	  Inode inode = new Inode(entry.iNumber);
	  // free this file table entry.
	   if (table.remove(entry))		// return true if this file table entry found in my table
		{
			// update count
			entry.inode.count--;

			// reset flag to not used
			if(entry.inode.count == 0)
			{
				entry.inode.flag =0;
			}

			entry.inode.toDisk(entry.iNumber);
			entry = null;

			// notify the other waitings that inode been updated
			notify();
			return true;
		}
		return false;	//Return false if the file table entry is not found in my table
	}

	public synchronized boolean fempty( ) {
	  return table.isEmpty( );  // return if table is empty 
	}                            // should be called before starting a format
}
