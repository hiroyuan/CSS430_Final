class SuperBlock{
	private final int DEFAULT_INODE_BLOCK = 64;
	private final int BLOCKS = 1000; //Use 1000 for number of block for this project
	public int totalBlocks;			//The number of disk block
	public int totalInodes;			//The number of Inodes
	public int freeList;			//The block number of the free list's head

    //CHECKED
	public SuperBlock(int diskSize) {
		// read super block from disk
		byte[] superBlock = new byte[Disk.blockSize];
		SysLib.rawread(0, superBlock);
		totalBlocks = SysLib.bytes2int(superBlock, 0);
		totalInodes = SysLib.bytes2int(superBlock, 4);
		freeList = SysLib.bytes2int(superBlock, 8);

        //1 superblock and minimum of 1 totalInodes, freeList head at block number 2 is valid
		if (totalBlocks == diskSize && totalInodes > 0 && freeList >= 2)
			// disk contents are valid
			return;
		else {
			// need to format
			totalBlocks = diskSize;
			format(DEFAULT_INODE_BLOCK);
		}
	}

    //CHECKED
	public void sync() {
		byte[] block = new byte[Disk.blockSize];
		SysLib.int2bytes(totalBlocks, block, 0); // turn blocks into bytes
		SysLib.int2bytes(totalInodes, block, 4); // turn inodes into bytes
		SysLib.int2bytes(freeList, block, 8); // turn free list into bytes, begins from offset 8
		SysLib.rawwrite(0, block); // write
	}

    //CHECKED
	public int getFreeBlock() {
		if (freeList > 0 && freeList < totalBlocks) {
			byte[] block = new byte[Disk.blockSize];
			SysLib.rawread(freeList, block);
			int retVal = freeList; // return current free block
			freeList = SysLib.bytes2int(block, 0); // find next free block, make SuperBlock point to the next free block.
			return retVal;
		}
		return -1;
	}
    
    //CHECKED
	public void format(int numberOfInode) {
		if (numberOfInode <= 0)
			numberOfInode = DEFAULT_INODE_BLOCK;

		// initialize iNodes
		totalInodes = numberOfInode;
        
		for (int i = 0; i < totalInodes; i++) {
			Inode iNode = new Inode();
            iNode.flag = 0;
			iNode.toDisk((short) i); // allocate every single inode to disk
		}

        //+2 or +1 ???????????
		freeList = totalInodes / 16 + 1;

		// initialize the every free blocks except last block
		// free blocks should contain next free block number
		// last free block should contain -1 for freeList
		for (int i = freeList; i < BLOCKS; i++) {
			byte[] block = new byte[Disk.blockSize];
			if (i <= BLOCKS - 2) {
				// format block here
				for (int index = 0; index < Disk.blockSize; index++)
					block[index] = 0; //nullify value within this free block

				SysLib.int2bytes(i + 1, block, 0); //assign next free block into the block
			}
			else if (i == BLOCKS - 1) {
				for (int index = 0; index < Disk.blockSize; index++)
					block[index] = 0; //nullify value within this free block
				
				SysLib.int2bytes(-1, block, 0); //last block should point -1 for freeList
													//since there is no more free block
			}
			SysLib.rawwrite(i, block);
		}

		sync();
	}

  	//CHECKED
  	public boolean returnBlock(int blockID)
  	{
      	if(blockID > 0 && blockID < totalBlocks)
      	{
          	byte[] freeBlock = new byte[Disk.blockSize];
          	SysLib.int2bytes(freeList, freeBlock, 0); // make new freeBlock point to the previous SuperBlock's freeList head
            SysLib.rawwrite(blockID, freeBlock); // write this empty block with blockID to the Disk
          	freeList = blockID; //ensuring that Superblock stores freeList that points to the newly added free block
          	return true;
        }
        else
          	return false;
    }
}
