package bg.bobs.SevenZip;

import SevenZip.IInStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import oracle.sql.BLOB;
import java.io.InputStream;

import java.sql.SQLException;

public class inStreamBLOB extends IInStream {
    private ByteArrayInputStream inStream;
    private long pos=0;
    public inStreamBLOB(oracle.sql.BLOB blob) throws SQLException {        
        inStream=new  ByteArrayInputStream(blob.getBytes(1, (int)blob.length()));
    }

    public long Seek(long offset, int seekOrigin) throws IOException {
        inStream.reset();        
        if (seekOrigin == STREAM_SEEK_CUR){
            pos+=offset;
        }else{
            pos=offset;
        }
    
        inStream.skip(pos);
        return pos;
    }

    public int read() throws java.io.IOException {
        int res=inStream.read();
        pos++; 
        return res;
    }
    
    public int read(byte [] data, int off, int size) throws java.io.IOException {
        int res= inStream.read(data,off,size);
        pos+=res; 
        return res;
    }
        
    public int read(byte [] data, int size) throws java.io.IOException {
        int res= inStream.read(data,0,size);
        pos+=res; 
        return res;
    }
    
    public void close() throws java.io.IOException {
        inStream.close();
        inStream = null;
        pos=0;
    }
}
