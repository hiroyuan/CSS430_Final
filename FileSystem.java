public class FileSystem
{
	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;

	private final int SEEK_SET = 0;
	private final int SEEK_CUR = 1;
	private final int SEEK_END = 2;
	
	public FileSystem(int diskBlocks)
	{
		superblock = new SuperBlock(diskBlocks);
		
		directory = new Directory(superblock.totalInodes);
		
		filetable = new FileTable(directory);
		
		FileTableEntry dirEnt = open("/" , "r");
		int dirSize = fsize(dirEnt);
		if (dirSize > 0)
		{
			byte[] dirData = new byte[dirSize];
			read(dirEnt, dirData);
			directory.bytes2directory(dirData);
		}
		close(dirEnt);
	}
	
	public void sync()
	{
		byte[] block = directory.directory2bytes();
		FileTableEntry root = open("/", "w"); //because sync() function in super block should perform write, we open with write permission
		write(root, directory.directory2bytes());
		close(root);
		superblock.sync();
	}
	public boolean format(int files)
	{
        if(files > 0) //&& filetable.fempty()) ???????????????????????
        {
            // have superblock formatted
            superblock.format(files);
        
            // have a new directory
            directory = new Directory(superblock.totalInodes);
        
            // have a new filetable
            filetable = new FileTable(directory);
            return true;
        }
        return false;
	}
		
	public FileTableEntry open(String filename, String mode)
	{
		FileTableEntry retVal = filetable.falloc(filename, mode);
		if (mode.equals("w")) {
			if (deallocAllBlocks(retVal) == false) // need to implement
				return null;
		}
		return retVal;
	}	
		
    //done
	public boolean close(FileTableEntry ftEnt) 
	{
		synchronized(ftEnt)
        {
            ftEnt.count--;
            if(count > 0)
            {
                return true;
            }
            else
            {
                return filetable.ffree(ftEnt);
            }
        }
	}
    
    //done
	public int fsize(FileTableEntry ftEnt)
	{
        synchronized(ftEnt)
        {
            return ftEnt.inode.length;
        }
	}
		
    
	public int read(FileTableEntry ftEnt, byte[] buffer)
	{
		if()
        {
            
        }
        else
        {
            return ERROR;
        }
	}
		
	public int write(FileTableEntry ftEnt, byte[] buffer)
	{
		synchronized(ftEnt)
        {
            
        }
	}	
    
    
	public boolean delete(String filename)
	{
		FileTableEntry entryToDelete = open(filename, "w");
		if (directory.ifree(entryToDelete.iNumber))
			return true;
		else {
			System.err.println("Deletion unsuccessful");
			return false;
		}
	}
    
    
	public int seek(FileTableEntry ftEnt, int offset, int whence)
	{
		synchronized(ftEnt)
		{
			switch(whence)
			{
				case SEEK_SET:		
					ftEnt.seekPtr = offset;		//Put the seekPointer of the file to offset
					break;
				case SEEK_CUR:
					ftEnt.seekPtr += offset;	//Add the seekPointer of the file to offset
					break;
				case SEEK_END:		//Add the seekPointer of the file equal the length of the inode to the offset
					ftEnt.seekPtr = ftEnt.inode.length + offset;
					break;
			}
			if(ftEnt.seekPtr < 0)
				ftEnt.seekPtr = 0;
			if(ftEnt.seekPtr > ftEnt.inode.length)
				ftEnt.seekPtr = ftEnt.inode.length
		}
		return ftEnt.seekPtr;
	}
    
    
	private boolean deallocAllBlocks(FileTableEntry ftEnt)
	{
		
	}
}
