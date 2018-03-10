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
	
	public FileTableEntry open(String filename, String mode)
	{
		FileTableEntry retVal = filetable.falloc(filename, mode);
		if (mode.equals("w")) {
			if (deallocAllBlocks(retVal) == false) // need to implement
				return null;
		}
		return retVal;
	}
	
		
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
                boolean check = filetable.ffree(ftEnt);
				if(check) return 0;
				return -1;
            }
            else
            {
                return -1;
            }
        }
	}
	
	public void sync()
	{
		byte[] block = directory.directory2bytes();
		FileTableEntry root = open("/", "w"); //because sync() function in super block should perform write, we open with write permission
		write(root, directory.directory2bytes());
		close(root);
		superblock.sync();
	}
	
	public int format(int files)
	{
		boolean toFormat = true;
        if(files > 0) //&& filetable.fempty()) ???????????????????????
        {
            // have superblock formatted
            superblock.format(files);
        
            // have a new directory
            directory = new Directory(superblock.totalInodes);
        
            // have a new filetable
            filetable = new FileTable(directory,toFormat);
            return 0;
        }
        return -1;
	}
			
	private boolean deallocAllBlocks(FileTableEntry ftEnt)
	{
		if (ftEnt == null || ftEnt.count > 1)
			return false;

		byte[] block = new byte[Disk.blockSize];
		if(ftEnt.inode.indirect == -1)
		{
			block = null;
		}
		else
		{
			SysLib.rawread(ftEnt.inode.indirect, block);
			ftEnt.inode.indirect = -1;
		}
	
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
    
	public int fsize(FileTableEntry ftEnt)
	{
        synchronized(ftEnt)
        {
            return ftEnt.inode.length;
        }
	}
	
	public int seek(FileTableEntry ftEnt, int offset, int whence)
	{
		synchronized(ftEnt)
		{
			switch(whence)
			{
				case SEEK_SET:		
					if (offset <= fsize(ftEnt) && offset >= 0)
                    {
                        ftEnt.seekPtr = offset;
                        
                    }
                    else
                    {
                        return -1;
                    }
					break;
				case SEEK_CUR:
					if (ftEnt.seekPtr + offset <= fsize(ftEnt) && ((ftEnt.seekPtr + offset) >= 0))
                    {
                        ftEnt.seekPtr += offset;
                    }
                    else
                    {
                        return -1;
                    }
					break;
				case SEEK_END:		//Add the seekPointer of the file equal the length of the inode to the offset
					if (fsize(ftEnt) + offset >= 0 && fsize(ftEnt) + offset <= fsize(ftEnt))
                    {
                        ftEnt.seekPtr = offset + fsize(ftEnt);
                    }
                    else
                    {
                        return -1;
                    }
					break;
			}
			return ftEnt.seekPtr;
		}
	}
	
	public int delete(String filename)
	{
		// get the inode number by using the filename through the directory
        short num = directory.namei(filename);
        if (num < 0) return -1;
		
		FileTableEntry entryToDelete = open(filename, "w");
		boolean temp = ((close(entryToDelete)==0) && directory.ifree(entryToDelete.iNumber));
		if (temp) return 0;

        return -1;
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
				return buffer.length;
			}
			return -1;		//If error happen
		}
	}
		
	public int write(FileTableEntry ftEnt, byte[] buffer)
	{
		 // error check for filetable entry is not exist or buffer is not exist
        if (ftEnt == null || buffer == null) {
            return -1;
        }
        // if the mode is read, it is not allow to modify
        if (ftEnt.mode == "r") {
            return -1;
        }

        // a copy of the indirect block of the inode if there is any 
        byte[] indirectBlock = new byte[Disk.blockSize];
		// copy the indirect block from the disk to the our indirectBlock
        if (ftEnt.inode.indirect > 0) {
            SysLib.rawread(ftEnt.inode.indirect, indirectBlock);
        }

        // indicate which logical indirect block we should keep writing on
        // if it is smaller than 0, we know no indirect block is using
        int logical_indirect = ftEnt.inode.length/Disk.blockSize - 11;
	    if (logical_indirect <0) logical_indirect  = 0;
		
        // the block that the seek pointer points at
        short targetBlock = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
		
        // the dataBlock is a copy of the block from the disk that we want to write
        byte[] dataBlock = new byte[Disk.blockSize];
		
        // if the seek pointer is in a valid block
        // we get the copy of the corresponding block
        if ( targetBlock >= 0 ) {
            SysLib.rawread(targetBlock, dataBlock);
        } 
        // otherwise, we need allocate a free block for further writing 
		else {
            targetBlock = (short) superblock.getFreeBlock();
            // an indicator to indicate whether 
            // we can successfully assign a target block to the firect block
			boolean test = false;
			for (int i = 0; i<11; i++) {
			    if (ftEnt.inode.direct[i] == -1) {
				    ftEnt.inode.direct[i] = targetBlock;
					test = true;
					break;
				}
			}
            // otherwise, we know that we are using indirect block
            if ( !test ) {
                SysLib.short2bytes(targetBlock, indirectBlock, logical_indirect * 2);
                logical_indirect++;
            }
        }



        // offset indicates where the seek pointer is in the block
        int offset = ftEnt.seekPtr % Disk.blockSize;


		int size = buffer.length;
		// keep tracking how many bytes that we had wrote
        // i.e. a pointer in the buffer
        int i = 0;

        // when the writing is not done
		while (i<size) {

            // if the offset reach the bottom of the block
            if ( offset == Disk.blockSize ) {
                // write back the dataBlock to the disk
                SysLib.rawwrite(targetBlock, dataBlock);
                
                // find another target block
                targetBlock = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
				
                
				if (targetBlock > 0) {
                    SysLib.rawread(targetBlock, dataBlock);
				} 
                // if we cannot get the target block
                // get another free block
                // follow the logic above
                else {
                    targetBlock = (short) superblock.getFreeBlock();
					
					boolean test = false;
			        for (int j = 0; j<11; j++) {
			            if (ftEnt.inode.direct[j] == -1) {
				            ftEnt.inode.direct[j] = targetBlock;
					        test = true;
					        break;
				        }
			        }
					
                    if ( !test ) {
                        SysLib.short2bytes(targetBlock, indirectBlock, logical_indirect * 2);
                        logical_indirect++;
                    }
				}
                offset = 0;
            }


            dataBlock[offset] = buffer[i];
            // update the index
            offset++;
            ftEnt.seekPtr++;
			i++;
        }
		
        // write back the dataBlock back to the disk
        SysLib.rawwrite(targetBlock, dataBlock);

        if ( ftEnt.seekPtr > ftEnt.inode.length ) {
            ftEnt.inode.length = ftEnt.seekPtr;
        }

        if ( logical_indirect > 0 ) {
            if ( ftEnt.inode.indirect < 0 ) {
                ftEnt.inode.indirect = (short) superblock.getFreeBlock();
            }
            SysLib.rawwrite(ftEnt.inode.indirect, indirectBlock);
        }

        return size;
    }
   
}
