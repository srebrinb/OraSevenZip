/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bg.bobs.SevenZip;

import com.swemel.sevenzip.CRC;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 *
 * @author sbalabanov
 */
public class InStreamBLOBWithCRC extends InputStream {

    private ByteArrayInputStream inStream;
    static public final int STREAM_SEEK_SET = 0;
    static public final int STREAM_SEEK_CUR = 1;
    long _size;
    boolean _wasFinished;
    CRC _crc = new CRC();
    private int pos=0;

    public InStreamBLOBWithCRC(oracle.sql.BLOB blob) throws  SQLException {
        inStream=new  ByteArrayInputStream(blob.getBytes(1, (int)blob.length()));
    }

    public long seek(int offset, int seekOrigin) throws IOException {
        inStream.reset();        
        if (seekOrigin == STREAM_SEEK_CUR){
            pos+=offset;
        }else{
            pos=offset;
        }
    
        inStream.skip(pos);
        return pos;
    }


    @Override
    public int read() throws IOException {
        int ret = inStream.read();
        _crc.UpdateByte((byte) ret);
        _size++;
        pos++; 
        return ret;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int ret = inStream.read(b);
        _crc.Update(b, ret);
        _size += ret;
        pos+=ret;
        return ret;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int ret = inStream.read(b, off, len);
        _crc.Update(b, off, ret);
        _size += ret;
        pos+=ret;
        return ret;
    }

    public void init() {
        _size = 0;
        _wasFinished = false;
        _crc.Init();
        pos=0;
    }

    public long getSize() {
        return _size;
    }

    public void releaseStream() throws IOException {
        inStream.close();
    }

    public int getCrc() {
        return _crc.GetDigest();
    }
}
