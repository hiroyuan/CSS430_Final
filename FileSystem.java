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
		
	}

	// 
	public boolean format(int files)
	{
		superblock.format(files);

		directory = new Directory(superblock.totalInodes);

		filetable = new FileTable(directory);

		return true;

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
		
	public int fsize(FileTableEntry ftEnt)
	{
        synchronized(ftEnt)
        {
            return ftEnt.inode.length;
        }
	}
		
		
	public int read(FileTableEntry ftEnt, byte[] buffer)
	{
		int buf_length = buffer.length;
		int block_size = 512;

		if (ftEnt.mode == "a" || ftEnt.mode == "w")
			return -1;
		
		synchronized(ftEnt) {
			// 
			while (entry.seekPtr < fsize(ftEnt) && (buf_length > 0))
				{
					SysLib.rawread
			// find the iNode to be read

			//if buffer size < data to be read, then keep reading until buffer is full. 
			//pointing ptr to end of read

			// else
				//

				
			if()
				{
					
				}
				else
				{
					return ERROR;
				}

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
		
	}
		
	public int seek(FileTableEntry ftEnt, int offset, int whence)
	{
	
	}

	private boolean deallocAllBlocks(FileTableEntry ftEnt)
	{
		
	}
}
