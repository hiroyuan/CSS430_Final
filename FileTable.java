public class FileTable {

	private Vector<FileTableEntry> table;         // the actual entity of this file table
	private Directory dir;        // the root directory 
	private final int UNUSED =0;
	private final int USED =1;
	private final int READ =2;
	private final int WRITE =3;
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
		  inumber = dir.namei(filename);		//Finding the Inode with the fileName first
		  if(inumber>=0)	//If the Inode exist
		  {		
			  inode = maintainInode.elementAt(Integer.valueOf(iNumber));
			  break;
		  }
		  else
		  {
				if(mode.equals("r")){		//If the Inode does not exist then allocate and create a new Inode for the fileName
					return null;
				}
				
				if((iNumber = dir.ialloc(filename))>=0)	//If the exist an inode free to allocate
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

		//When we read set flag to 1
		if(mode.equals("r"))
		{
			if(inode.flag == USED || inode. == UNUSED)
				inode.flag = READ;
			else
			{
				try{
					wait();
				}catch (InterruptedException e)
			}
		}
		else	//Other cases: writing, appending, set flag to 3
		{
			if(inode.flag == USED || inode. == UNUSED)
				inode.flag = WRITE;
			else
			{
				try{
					wait();
				}catch (InterruptedException e)
			}
		}
		// increment this inode's count
		inode.count++;
		// immediately write back this inode to the disk
		inode.toDisk(inumber);
		// return a reference to this file (structure) table entry
		fte = new FileTableEntry(inode,inumber,mode);
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
			if (inode.flag == READ)
			{
				if (inode.count == 1)
				{
					// free this file table entry.
					notify();
					inode.flag = USED;
				}
			}
			else if (inode.flag == WRITE)
			{
				inode.flag = USED;
				notifyAll();		//notify to all thread that this inode is free to read or write
			}
			//decrease the count of users of that file
			inode.count--;
			// save the corresponding inode to the disk
			inode.toDisk(entry.iNumber);
			entry = null;		//Free this file table entry now
			return true;	
		}
		return false;	//Return false if the file table entry is not found in my table
	}

	public synchronized boolean fempty( ) {
	  return table.isEmpty( );  // return if table is empty 
	}                            // should be called before starting a format
}
