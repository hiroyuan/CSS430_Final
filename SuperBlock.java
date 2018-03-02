class SuperBlock{
	private final int DEFAULT_INODE_BLOCK = 64;
	public int totalBlocks;			//The number of disk block
	public int totalInodes;			//The number of Inodes
	public int freeList;			//The block number of the free list's head

	public SuperBlock(int diskSize) {
		// read super block from disk
		byte[] superBlock = new byte[Disk.blockSize];
		SysLib.rawread(0, superBlock);
		totalBlocks = SysLib.bytes2int(superBlock, 0);
		totalInodes = SysLib.bytes2int(superBlock, 4);
		freeList = SysLib.bytes2int(superBlock, 8);

		if (totalBlocks == diskSize && totalInodes > 0 && freeList >= 2)
			// disk contents are valid
			return;
		else {
			// need to format
			totalBlocks = diskSize;
			format(DEFAULT_INODE_BLOCK);
		}
	}

	public void sync() {
		byte[] block = new byte[Disk.blockSize];
		SysLib.int2bytes(totalBlocks, block, 0); // turn blocks into bytes
		SysLib.int2bytes(totalInodes, block, 4); // turn inodes into bytes
		SysLib.int2bytes(freeList, block, 8); // turn free list into bytes
		SysLib.rawwrite(0, block); // write
	}

	public int getFreeBlock() {
		if (freeList > 0 && freeList < totalBlocks) {
			byte[] block = new byte[Disk.blockSize];
			SysLib.rawread(0, block);
			int retVal = freeList; // return current free block
			freeList = SysLib.bytes2int(block, 8); // find next free block
			return retVal;
		}
		return -1;
	}

	public void returnBlock(int blockNumber) {
		
	}

	public void format(int size) {

	}
}