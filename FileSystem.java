public class FileSystem
{
	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;

	private final int SEEK_SET = 0;
	private final int SEEK_CUR = 1;
	private final int SEEK_END = 2;
	
	public FileSystem(int diskBlock)
	{
		superblock = new SuperBlock(diskBlocks);
		
		directory = new Directory(superblock.inodeBlocks);
		
		filetable = new FileTable(directory);
		
		FileTableEntry dirEnt = open("/" , "r");
		int diSize = fsize(dirEnt);
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
	public boolean format(int files) 
	{
		
	}
		
	public FileTableEntry open(String filename, String mode)
	{
		FileTableEntry retVal = filetable.falloc(filename, mode);
		if (mode == "w") {
			if (deallocAllBlocks(retVal) == false)
				return null;
		}
		return retVal;
	}	
		
	public boolean close(FileTableEntry ftEnt) 
	{
		
	}
		
	public int fsize(FileTableEntry ftEnt)
	{
		
	}
		
		
	public int read(FileTableEntry ftEnt, byte[] buffer)
	{
		
	}
		
	public int write(FileTableEntry ftEnt, byte[] buffer)
	{
		
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
