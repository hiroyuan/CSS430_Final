public class Inode{
    private final static int iNodeSize = 32;            //Fix size for an Inode is 32 bytes
    private final static int directSize = 11;            // # of direct Pointers
    private final static int MAX_BYTES = 512;            //Maximum size for a block
    private final static int intOffset = 4;     // offset increment after 1x Java integer value from byte[]
    private final static int shortOffset = 2;   // offset increment after 1x Java short value from byte[]
    private final static int MAX_INODES = 16;
    
    // size of 1 Java integer = 4 bytes
    // size of 1 Java short = 2 bytes
    public int length;            //File sizes in bytes -> 4 bytes
    public short count;            //# file-table entries point to this -> 2 bytes
    public short flag;             //0 = unused, 1= used,... -> 2 bytes
    public short direct[] = new short[directSize];    //Direct pointers -> 11 * 2 bytes = 22 bytes
    public short indirect;            //Indirect pointer -> 2 bytes (size of Java short)
    //Total bytes of an Inode = 4 + 2 + 2 + 22 + 2 = 32 bytes
    
    //Default constructor
    public Inode(){
        length = 0;
        count = 0;
        flag = 1;
        
        for(int i=0;i<directSize;i++)
            direct[i] = -1;
        
        indirect = -1;
    }
    
    public Inode(short iNumber){        //Retrieve the Inode from disk
        //Assume this constructor to create a duplicate of an Inode existing in Disk
        
        //find the block number to be read from Disk
        int blockID = 1 + iNumber / MAX_INODES;
        
        //allocate bytes
        byte[] data = new byte[MAX_BYTES];
        SysLib.rawread(blockID, data);
        
        //offset of Inode within a the block (where to start reading from the byte[512] of inodes)
        //i.e. Inode with iNumber 32 is the 0th Inode of a block
        int inodeOffset = (iNumber % 16) * iNodeSize;
        
        //assign values to Inode's class variables
        length = SysLib.bytes2int(data, inodeOffset);
        inodeOffset += intOffset; // inodeOffset to point to starting byte of next data of the inode
        
        count = SysLib.bytes2short(data, inodeOffset);
        inodeOffset += shortOffset; // inodeOffset to point to starting byte of next data of the inode
        
        flag = SysLib.bytes2short(data, inodeOffset);
        inodeOffset += shortOffset; // inodeOffset to point to starting byte of next data of the inode
        
        for (int i = 0; i < directSize; i++){
            direct[i] = SysLib.bytes2short(data, inodeOffset);
            inodeOffset += shortOffset; // inodeOffset to point to starting byte of next data of the inode
        }
        
        indirect = SysLib.bytes2short(data, inodeOffset);
        inodeOffset += shortBlock; // inodeOffset to point to starting byte of next data of the inode
    }
    
    public int toDisk(short iNumber) {    //Save the i-th inode to the disk
        byte [] data = new byte[iNodeSize];
        
        int offset = 0;
        
        //assigning length value to new byte[]
        SysLib.int2bytes(length, data, offset);
        offset += intBlock;
        
        //assigning count value to new byte[]
        SysLib.short2bytes(count, data, offset);
        offset += shortBlock;
        
        //assigning flag value to new byte[]
        SysLib.short2bytes(flag, data, offset);
        offset += shortBlock;
        
        //assigning direct block pointer values to new byte[]
        for (int i = 0; i < directSize; i++)
        {
            SysLib.short2bytes(direct[i], data, offset);
            offset += shortBlock;
        }
        
        //assigning indirect block pointer values to new byte[]
        SysLib.short2bytes(indirect, data, offset);
        offset += shortBlock;
        
        int blockID = 1 + iNumber / MAX_INODES;
        
        //retrieve the entire inode block of 16 ondes where this modified inode is located within Disk
        byte[] newData = new byte[MAX_BYTES];
        SysLib.rawread(blockID,newData);
        
        //offset to access the very first byte of this inode within the block containing 16 inodes
        int inodeOffset = (iNumber % MAX_INODES) * iNodeSize;
        
        //copy this new inode info from data[] to replace previous info of this inode within the block of inodes
        System.arraycopy(data, 0, newData, inodeOffset, iNodeSize);
        SysLib.rawwrite(blockID,newData); // write back the entire inode block, containing modified info for inode #iNumber back into the Disk
        
        return 0;
    }
    
    public short getIndexBlockNumber(){
        return indirect;
    }
    public boolean setIndexBlock(short indexBlockNumber)
    {
        if(indexBlockNumber >= -1
        indirect = indexBlockNumber;
    }
    public short findTargetBlock(int seekPointer){
        //check if seekPointer still points to byte within direct block
        if(indirect < 0)
        {
            return -1;
        }
        
        int blockIndex = seekPointer/MAX_BYTES; //1 block = 512 bytes
        if(seekBlock < directSize) // seekPointer still points to a byte within one of the direct block
        {
            return direct[blockIndex];
        }
        else // seekPointer points to a byte within one block of data pointed by indirect pointer
        {
            //reading in an indirect block from Disk, each 2 bytes of indirect block represents data block
            //, similar to direct block with each 2 byte for data
            byte[] indirectBlock = new byte[MAX_BYTES];
            SysLib.rawread(indirect, indirectBlock);
            
            int pointerNumber = blockIndex - 11; // represent an index out of the 256 pointers
            int blockOffset = pointerNumber * 2; // 1 pointer = 2 bytes -> offset of 2 byte indices within byte[512]
            
            //get short value starting from blockOffset from indirectBlock
            return SysLib.bytes2short(indirectBlock, blockOffset);
        }
        
    }
}

