public class FileTable {

   private Vector<FileTableEntry> table;         // the actual entity of this file table
   private Directory dir;        // the root directory 
   private final int UNUSED =0;
   private final int USED =1;
   private final int READ =2;
   private final int WRITE =3;

   public FileTable( Directory directory ) { // constructor
      table = new Vector<FileTableEntry>( );     // instantiate a file (structure) table
      dir = directory;           // receive a reference to the Director
   }                             // from the file system

   // major public methods
   public synchronized FileTableEntry falloc( String filename, String mode ) {
      // allocate a new file (structure) table entry for this file name
<<<<<<< HEAD
	  FileTableEntry fte = null;
	  Inode inode = null;
	  // allocate/retrieve and register the corresponding inode using dir
	  while(true){
		  int inumber = namei(filename);		//Finding the Inode with the fileName first
		  if(inumber>=0)	//If the Inode exist
		  {
			  inode = new Inode(inumber);
			  if(mode.equals("r")) 		//In case mode is read
			  {
				  if(inode.flag == USED || inode.flag == UNUSED){		//If the Inode is not reading then change the flag into read
					  inode.flag = READ;
					  break;
				  }
				  else{		//If the inode is reading or writing then wait
					  try{
						wait();
					  }catch(Exception ex){
						  System.err.out("Interrupted exception");
					  }
				  }
			  }
			  else{	//In case the mode is read, read/write and append 
				  if(inode.flag == USED || inode.flag == UNUSED){		//If the Inode is not writing then change the flag into write
					  inode.flag = WRITE;
					  break;
				  }
				  else{		//If the inode is Reading or Writing then wait
					  try{
						wait();
					  }catch(Exception ex){
						  System.err.out("Interrupted exception");
					  }
				  }
			  }
		  }
		  else if(mode.equals("r")){		//If the Inode does not exist then allocate and create a new Inode for the fileName
			  iNumber = dir.ialloc(fileName);
			  inode = new Inode(inumber);
			  inode.flag = WRITE;
			  break;
		  }
		  else{
			  return null;
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
=======
      // allocate/retrieve and register the corresponding inode using dir
      // increment this inode's count
      // immediately write back this inode to the disk??????????????????????????????
      // return a reference to this file (structure) table entry
	if(mode.equals("w") || mode.equals("w+") || mode.equals("a")
	{
		FileTableEntry fte = null;
		Inode inode = null;
		//check if filename exists within directory
		// if filename dne
			// create new file: ialloc to allocate new inode number for filename
		// else
			// retrieve this FileTableEntry from Directory
		int iNumber = namei(filename);
		if(iNumber == -1) // ASSUME -1 TO BE RETURN VALUE WHEN NOT FOUND?
		{
			iNumber = dir.ialloc(filename); // allocate new inode number in directory for this filename
			inode = new Inode(iNumber);

			fte = new FileTableEntry(inode, iNumber, mode);
		}
		else
		{
			// retrieve from the existing iNumber from the directory
			inode = new Inode(iNumber);
			
			fte = new FileTableEntry(inode, iNumber, mode);
		}

	}
	
>>>>>>> origin/master
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
            return true;	
        }
        return false;	//Return false if the file table entry is not found in my table
   }

   public synchronized boolean fempty( ) {
      return table.isEmpty( );  // return if table is empty 
   }                            // should be called before starting a format
}
