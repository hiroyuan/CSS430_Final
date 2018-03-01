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
			//FileSystem.format(DEFAULT_INODE_BLOCK);
		}
	}

	public void sync() {

	}

	public void getFreeBlock() {

	}

	public void returnBlock() {

	}
}