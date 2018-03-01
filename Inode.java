public class Inode{
	private final static int iNodeSize = 32;			//Fix size for an Inode is 32 bytes
	private final static int directSize = 11;			// # of direct Pointers
	
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
	}

	public int toDisk(short iNumber) {	//Save to the disk as i-th inode
		//Need to implement
		return 0;
	}
}