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
public class ArchiveExtractOneFileCallback  implements IArchiveExtractCallback // , ICryptoGetTextPassword,
{
    boolean DEBUG=false;    
    
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
    
    OutputStream _outFileStream;
    OutputStream OutS;
    public void SetStream(OutputStream os){
        OutS=os;
    }
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
                

                    isDirectory = item.isDirectory();
                    
                    if (isDirectory) {
                         return HRESULT.S_OK;                        
                   }
                        if (DEBUG){System.out.println("open "+item.getName()) ;}
                        outStream[0] = OutS;                          
                    

                
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
    
    public ArchiveExtractOneFileCallback() { 
        PasswordIsDefined = false;         
    }        
    public void Init(SevenZip.Archive.IInArchive archiveHandler)  {
        NumErrors = 0;        
        _archiveHandler = archiveHandler;
    }        
}