/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bg.bobs.SevenZip;

import com.swemel.sevenzip.UpdateItem;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.jdbc.OracleResultSet;

/**
 *
 * @author sbalabanov
 */
public class SevenZipStmtInStream extends InputStream {    
    
    InStreamBLOBWithCRC stream;    
    Vector<Boolean> processed = new Vector<Boolean>();
    Vector<Integer> CRCs = new Vector<Integer>();
    Vector<Long> sizes = new Vector<Long>();
    boolean _currentSizeIsDefined;
    boolean _fileIsOpen;
    long _currentSize;
    long _filePos;
    long size=0;
    OracleResultSet _cursor;
    UpdateItem ui;
    private Vector<UpdateItem> updateItems = new Vector<UpdateItem>();
    private Boolean eof=false;
    int currentIndex = 0;
    public Vector<UpdateItem> getFiles(){
        return updateItems;
    }
    public void init(OracleResultSet cursor){
        _cursor=cursor;
        processed.clear();
        CRCs.clear();
        sizes.clear();
        _fileIsOpen = false;        
        _currentSizeIsDefined = false;
        size=0;
        eof=false;
    }
    
    void openStream() throws SQLException  {
        _filePos = 0;
        while (_cursor.next()) {
          ui = new UpdateItem();
          ui.setName(_cursor.getString("FILENAME"));          
       //   System.out.println(_cursor.getString("FILENAME"));
          //ui.setMTime(java.nio.file.attribute.FileTime.fromMillis(Timestamp.getTime()));          
          
          ui.setNewProps(true);
          ui.setNewData(true);          
          ui.setIndexInArchive(-1);
          ui.setIndexInClient(currentIndex++);
          ui.setIsAnti(false);
          ui.setSize(0);
          long millis = System.currentTimeMillis() ;
          ui.setATimeDefined(true);
          ui.setATime(millis);
          ui.setCTimeDefined(true);
          ui.setCTime(millis);
          ui.setMTimeDefined(true);              
          ui.setMTime(millis);
          ui.setFullName(_cursor.getString("FILENAME"));
          ui.setIsDir(false);
          ui.setIsAnti(false);
          ui.setAttrib(32);
          
          stream = new InStreamBLOBWithCRC(_cursor.getBLOB("FILECONT"));          
          stream.init();
          
          if (stream != null) {
                _fileIsOpen = true;

                _currentSize = stream.getSize();
                _currentSizeIsDefined = true;
                return;
          }

        }       
        sizes.add(0L);
        processed.add(true);          
        addDigest();
        eof=true;
    }
    public void closeStream() throws IOException {
        stream.releaseStream();
        _fileIsOpen = false;
        _currentSizeIsDefined = false;
        processed.add(true);
        sizes.add(_filePos);
        ui.setSize (_filePos);
        updateItems.add(ui);
        addDigest();
    }

    @Override    
    public int read(byte[] b) throws IOException {
        return read(b,0,b.length);
    }
    @Override
    public int read(byte[] data, int off, int len) throws IOException {
        int processedSize = -1;
        while (len > 0) {
            if (_fileIsOpen) {

                int processed2 = stream.read(data, off, len);
                if (!(processed2 > 0)) {
                    closeStream();
                    continue;
                }
                processedSize = processed2;
                size+=processed2;
                _filePos += processed2;
                break;
            }
            try {
                if (eof) break;
                openStream();
            } catch (SQLException ex) {
                Logger.getLogger(SevenZipStmtInStream.class.getName()).log(Level.SEVERE, null, ex);
                throw new IOException(ex.getMessage());
            }
        }
        return processedSize;
    }
    @Override
    public int read() throws IOException {
        byte[] ret = new byte[1];
        if(read(ret)<0) return -1;
        return ret[0];
    }
    public void addDigest() {
        CRCs.add(stream.getCrc());
    }
    public int getCrc(int i)
    {
        return CRCs.get(i);
    }
    public long getFullSize() {
        long size = 0;
        for (int i = 0; i < sizes.size(); i++)
            size += sizes.get(i);
        return size;
    }
}
