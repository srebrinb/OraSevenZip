package bg.bobs.SevenZip;
import SevenZip.Archive.IArchiveExtractCallback;
import SevenZip.Archive.IInArchive;
import SevenZip.HRESULT;
import SevenZip.Archive.SevenZipEntry;

import oracle.jdbc.OracleCallableStatement;


import oracle.jdbc.driver.OracleDriver;
import oracle.sql.BLOB;


import java.sql.SQLException;
import java.io.OutputStream;
public class ArchiveExtractCallback implements IArchiveExtractCallback // , ICryptoGetTextPassword,
{
    boolean DEBUG=false;
    class myOutputStream extends java.io.OutputStream {
        
        BLOB tmpBLOB;
        OutputStream BAB;
        String fileName;
        public myOutputStream (String iFileName) throws SQLException {            
            tmpBLOB = BLOB.createTemporary(conn, true, BLOB.DURATION_SESSION);
            tmpBLOB.open(BLOB.MODE_READWRITE);
            BAB = tmpBLOB.setBinaryStream(0);
            fileName=iFileName;
        }
        
        public void close()  throws java.io.IOException {
            try {
                BAB.close();                
                call_stmt.setString(1, fileName);
                call_stmt.setBLOB (2,tmpBLOB);
                call_stmt.execute();                
            } catch (SQLException e) {
                throw new java.io.IOException(e.getMessage());   
            }
        }
        /*
        public void flush()  throws java.io.IOException {
            file.flush();
        }
         */
        public void write(byte[] b)  throws java.io.IOException {
            BAB.write(b);
            if (DEBUG){System.out.println(b.length) ;}
        }
        
        public void write(byte[] b, int off, int len)  throws java.io.IOException {
               BAB.write(b,off,len);            
            if (DEBUG){System.out.println(b.length) ;}
        }
        
        public void write(int b)  throws java.io.IOException {
            BAB.write(b);
            if (DEBUG){System.out.println(b) ;}
        }
    }
    
    public int SetTotal(long size) {
        return HRESULT.S_OK;
    }
    
    public int SetCompleted(long completeValue) {
        return HRESULT.S_OK;
    }
    
    public void PrintString(String str) {
        System.out.print(str);
    }
    
    public void PrintNewLine() {
        System.out.println("");
    }
    public int PrepareOperation(int askExtractMode) {
        _extractMode = false;
        switch (askExtractMode) {
            case IInArchive.NExtract_NAskMode_kExtract:
                _extractMode = true;
        };
         if (DEBUG){ 
        switch (askExtractMode) {
            case IInArchive.NExtract_NAskMode_kExtract:
                PrintString("Extracting  ");
                break;
            case IInArchive.NExtract_NAskMode_kTest:
                PrintString("Testing     ");
                break;
            case IInArchive.NExtract_NAskMode_kSkip:
               PrintString("Skipping     ");
                break;
         };
         PrintString(_filePath+"\n");
        }
      //  
        return HRESULT.S_OK;
    }
    
    public int SetOperationResult(int operationResult) throws java.io.IOException {
        switch(operationResult) {
            case IInArchive.NExtract_NOperationResult_kOK:
                break;
            default:
            {
                NumErrors++;
           //     PrintString("     ");
                switch(operationResult) {
                    case IInArchive.NExtract_NOperationResult_kUnSupportedMethod:
                        PrintString("Unsupported Method");
                        break;
                    case IInArchive.NExtract_NOperationResult_kCRCError:
                        PrintString("CRC Failed");
                        break;
                    case IInArchive.NExtract_NOperationResult_kDataError:
                        PrintString("Data Error");
                        break;
                    default:
                        PrintString("Unknown Error");
                }
            }
        }
            /*
            if(_outFileStream != null && _processedFileInfo.UTCLastWriteTimeIsDefined)
                _outFileStreamSpec->File.SetLastWriteTime(&_processedFileInfo.UTCLastWriteTime);
             */
        if (_outFileStream != null) _outFileStream.close(); // _outFileStream.Release();
            /*
            if (_extractMode && _processedFileInfo.AttributesAreDefined)
                NFile::NDirectory::MySetFileAttributes(_diskFilePath, _processedFileInfo.Attributes);
             */
        //PrintNewLine();
        return HRESULT.S_OK;
    }
    
    java.io.OutputStream _outFileStream;
  
    public int GetStream(int index,
            java.io.OutputStream [] outStream,
            // Updated to pass parent_dir argument [GAB, OpenLogic 2013-10-28]
            int askExtractMode, java.io.File parent_dir) throws java.io.IOException {
        
        outStream[0] = null;
        
        SevenZipEntry item = _archiveHandler.getEntry(index);
        _filePath = item.getName();


        switch (askExtractMode) {
            case IInArchive.NExtract_NAskMode_kTest:
                return HRESULT.S_OK;
                
            case IInArchive.NExtract_NAskMode_kExtract:
                
                try {
                    isDirectory = item.isDirectory();
                    
                    if (isDirectory) {
                         return HRESULT.S_OK;                        
                   }
                    try{
                        outStream[0] = new myOutputStream(item.getName());                          
                    } catch (SQLException e) {
                        throw new java.io.IOException(e.getMessage());   
                    }
                    
                } catch (java.io.IOException e) {
                    return HRESULT.S_FALSE;
                }
                
                return HRESULT.S_OK;                
        }
        
        // other case : skip ...
        
        return HRESULT.S_OK;
        
    }
    
    SevenZip.Archive.IInArchive _archiveHandler;  // IInArchive
    String _filePath;       // name inside arcvhive
    String _diskFilePath;   // full path to file on disk
    
    public long NumErrors;
    boolean PasswordIsDefined;
    String Password;
    boolean _extractMode;
    
    boolean isDirectory;
    
    public ArchiveExtractCallback() { 
        PasswordIsDefined = false;         
    }
    private static OracleDriver oracleDriver = new oracle.jdbc.driver.OracleDriver();
    private static java.sql.Connection conn = null;
    private OracleCallableStatement call_stmt;
    public void Init(SevenZip.Archive.IInArchive archiveHandler) throws SQLException {
        NumErrors = 0;
        conn = oracleDriver.defaultConnection();
        _archiveHandler = archiveHandler;
    }
    public void Init(SevenZip.Archive.IInArchive archiveHandler,String prossesProc) throws SQLException {
        NumErrors = 0;
        _archiveHandler = archiveHandler;
        
        conn = oracleDriver.defaultConnection();
        conn.setAutoCommit(false);        
        call_stmt = (OracleCallableStatement) conn.prepareCall("call "+prossesProc+"(?, ?)");
        
    }
    
}