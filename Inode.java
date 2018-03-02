public class Inode{
	private final static int iNodeSize = 32;			//Fix size for an Inode is 32 bytes
	private final static int directSize = 11;			// # of direct Pointers
	private final static int MAX_BYTES = 512;			//Maximum size for a block
	private final static int intBlock = 4;				//Int using 4 offsets
	private final static int shortBlock = 2;			//Short using 2 offsets
	
	public int length;			//File sizes in bytes
	public short count;			//# file-table entries point to this
	public short flag; 			//0 = unused, 1= used
	public short direct[] = new short[directSize];	//Direct pointers
	public short indirect;			//Indirect pointer
	
	//Default constructor
	public Inode(){
		length = 0;
		count = 0;
		flag = 1;
		
		for(int i=0;i<directSize;i++)
			direct[i] = -1;					//Point to nothing
		indirect = -1;
	}
	
	public Inode(short iNumber){		//Retrieve the Inode from disk
		//Need to implement
		//find the number of blocks to read
        int blockNumber = 1 + iNumber / blockSize;

        //allocate bytes
        byte[] data = new byte[maxBytes];
        SysLib.rawread(blockNumber,data);

        //define the offset
        int offset = (iNumber % 16) * iNodeSize;

        //create space
        length = SysLib.bytes2int(data, offset);
        offset += intBlock;
        count = SysLib.bytes2short(data, offset);
        offset += shortBlock;
        flag = SysLib.bytes2short(data, offset);
        offset += shortBlock;

        for (int i = 0; i < directSize; i++){
            direct[i] = SysLib.bytes2short(data, offset);
            offset += shortBlock;
        }
        indirect = SysLib.bytes2short(data, offset);
        offset += shortBlock;
	}

	public int toDisk(short iNumber) {	//Save to the disk as i-th inode
		//Need to implement
		return 0;
	}
}