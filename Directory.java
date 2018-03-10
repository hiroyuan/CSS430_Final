public class Directory {
   private static int maxChars = 30; // max characters of each file name
   private static short ERROR = -1;		//Error return value
   private static int ALLOC_BYTE = 64;
   
	
   // Directory entries
   private int fsize[];        // each element stores a different file size.
   private char fnames[][];    // each element stores a different file name.

   public Directory( int maxInumber ) { // directory constructor
      fsize = new int[maxInumber];     // maxInumber = max files
      for ( int i = 0; i < maxInumber; i++ ) 
         fsize[i] = 0;                 // all file size initialized to 0
      fnames = new char[maxInumber][maxChars];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = root.length( );        // fsize[0] is the size of "/".
      root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
   }

   public void bytes2directory( byte data[] ) {
      // assumes data[] received directory information from disk
	  // initializes the Directory instance with this data[]
	  int offset = 0;	//Initialize the offset first
	  //Initialize the fsize array with the size of each file 
	  for(int i=0;i<fsize.length;i++){
		  fsize[i] = SysLib.bytes2int(data,offset);
		  offset+=4;			//One block is two bytes (4 offset) for this implementation
	  }
      //Initialize the fnames array with the name of the file
	  for(int i=0;i<fsize.length;i++){
		  String temp = new String(data,offset,maxChars*2);
		  temp.getChars(0,fsize[i],fnames[i],0);
		  offset+=maxChars*2;
	  }
   }

   public byte[] directory2bytes( ) {
      // converts and return Directory information into a plain byte array
      // this byte array will be written back to disk
      // note: only meaningfull directory information should be converted
      // into bytes.
		byte [] dir = new byte[fsize.length * 4 + maxChars * fsize.length * 2];
        int offset = 0;
        for (int i = 0; i < fsize.length; i++)
        {
            SysLib.int2bytes(fsize[i], dir, offset);
            offset += 4;
        }
        for (int i = 0; i < fsize.length; i++)
        {
            String temp = new String(fnames[i], 0, fsize[i]);
            byte [] bytes = temp.getBytes();
            System.arraycopy(bytes, 0, dir, offset, bytes.length);
            offset += maxChars;
        }
        return dir;
   }

   public short ialloc( String filename ) {
      // filename is the one of a file to be created.
      // allocates a new inode number for this filename
	  short result = ERROR;		//Initialize result with an error first
	  for(short i=0;i<fsize.length;i++)
	  {
		  if(fsize[i] == 0){
			  int file;
			  if(filename.length()>maxChars)
				  file = maxChars;
			  else
				  file = filename.length();
			  fsize[i] = file;
			  System.arraycopy (filename.toCharArray(), 0, fnames[i], 0, file);
			  return i;
		  }
	  }
	  return result;
   }

   public boolean ifree( short iNumber ) {
      // deallocates this inumber (inode number)
      // the corresponding file will be deleted.
	  if(iNumber < fsize.length && iNumber>0 &&fsize[iNumber]>0)
	  {
		  for(int i=0;i<fsize[iNumber];i++)
		  {
			  fnames[iNumber][i] = 0;
		  }
		  fsize[iNumber] = 0;
		  return true;
	  }
	  else
		  return false;
   }

   public short namei( String filename ) {
      // returns the inumber corresponding to this filename
	  for(short i = 0;i<fsize.length;i++){
		  if(fsize[i] == filename.length())
		  {
			  String temp = new String(fnames[i],0,fsize[i]);
			  if(filename.equals(temp))
				  return i;
		  }
	  }
	  return ERROR;
   }
}