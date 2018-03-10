public class FileSystem
{
	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;

	private final int SEEK_SET = 0;
	private final int SEEK_CUR = 1;
	private final int SEEK_END = 2;
	
	private final int MAX_BYTES = 512;
	
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
	public int close(FileTableEntry ftEnt) 
	{
		//If this file table entry is null return error
		if(ftEnt == null)
			return -1;
		
		synchronized(ftEnt)
        {
            ftEnt.count--;
            if(ftEnt.count <= 0)
            {
                boolean check = filetable.ffree(ftEnt)
				if(check) return 0;
				return -1;
            }
            else
            {
                return -1;
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
		int byteRead = 0;
		int currByte = 0;
		
		if(ftEnt == null || buffer == null)		//Error cases
			return -1;
		synchronized(ftEnt)
		{
			int blockToRead = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
			if(blockToRead != -1)	//When the block exist
			{
				byte[] data = new byte[MAX_BYTES];
				SysLib.rawread(blockToRead,data);
				for(int i=0;i<buffer.length;i++)
				{
					currByte = ftEnt.seekPtr%Disk.blockSize;
					if(currByte == 0)
					{
						int nextBlockToRead = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
						SysLib.rawread(nextBlockToRead,data);
					}
					buffer[i] = data[currByte];
					ftEnt.seekPtr++;
				}
				return buffer.length
			}
			return -1;		//If error happen
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
		if (ftEnt == null || ftEnt.count > 1)
			return false;

		byte[] block = new byte[Disk.blockSize];
		SysLib.rawread(ftEnt.inode.indirect, buffer);
		ftEnt.inode.indirect = -1;

		if (block != null) {
			int indirContainer = (ftEnt.inode.length / 512) - 11;
            short result = SysLib.bytes2short(block, indirContainer * 2);
            for (int i = 0; i < indirContainer; i++) {
            	superblock.returnBlock(result);
            	result = SysLib.bytes2short(block, indirContainer * 2);
            }
			ftEnt.inode.indirect = -1;
		}
		block = null;

		for (int i = 0; i < ftEnt.inode.directSize; i++) {
			superblock.returnBlock(ftEnt.inode.direct[i]);
			ftEnt.inode.direct[i] = -1;
		}


		ftEnt.inode.length = 0;
		ftEnt.seekPtr = 0;
		ftEnt.inode.toDisk(ftEnt.iNumber);
		return true;
	}
}
