package bg.bobs.SevenZip;

import SevenZip.Archive.IArchiveExtractCallback;
import SevenZip.Archive.IInArchive;
import SevenZip.Archive.SevenZip.Handler;
import SevenZip.Archive.SevenZipEntry;

import SevenZip.Invalid7zArchiveException;
import java.io.IOException;


import java.sql.SQLException;

import java.text.DateFormat;
import oracle.sql.*;
import oracle.jdbc.driver.*;
import java.sql.*;
import java.util.Vector;

import oracle.sql.ArrayDescriptor;

public class extractBLOB {
    public extractBLOB() {
        super();
    }
    static private String vArrayType="JAVA_UTILS.STRARRAY";
    public static void setArrayType(String ArrayType){
        vArrayType=ArrayType;
    }
    static void listing(IInArchive archive,Vector<String> listOfNames,boolean techMode) {
        
        if (!techMode) {
            System.out.println("  Date   Time   Attr         Size   Compressed  Name");
            System.out.println("-------------- ----- ------------ ------------  ------------");
        }
        
        long size = 0;
        long packSize = 0;
        long nbFiles = 0;
        System.out.println("size = " + archive.size());
        for(int i = 0; i < archive.size() ; i++) {
            SevenZipEntry item = archive.getEntry(i);
            
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT , DateFormat.SHORT );
            String str_tm = formatter.format(new java.util.Date(item.getTime()));            
            if (listOfNames.contains(item.getName())) {
                if (techMode) {
                    System.out.println("Path = " + item.getName());
                    System.out.println("Size = " + item.getSize());
                    System.out.println("Packed Size = " + item.getCompressedSize());
                    System.out.println("Modified = " + str_tm);
                    System.out.println("   Attributes : " + item.getAttributesString());
                    long crc = item.getCrc();
                    if (crc != -1)
                        System.out.println("CRC = " + Long.toHexString(crc).toUpperCase());
                    else
                        System.out.println("CRC =");
                    System.out.println("Method = " + item.getMethods() );
                    System.out.println("" );
                    
                } else {
                    System.out.print(str_tm + " " + item.getAttributesString());
                    
                    System.out.print(String.format("%13d",item.getSize()));
                    
                    System.out.print(String.format("%13d",item.getCompressedSize()));
                    
                    System.out.println("  " + item.getName());
                }
                
                size += item.getSize();
                packSize += item.getCompressedSize();
                nbFiles ++;
            }
        }
        
        if (!techMode) {
            System.out.println("-------------- ----- ------------ ------------  ------------");
            System.out.print(String.format("                    %13d%13d %d files",size,packSize,nbFiles));
        }
    }
    static void test(IInArchive archive,Vector<String> listOfNames,int mode, String parent_dir) throws Exception {
        
        ArchiveExtractCallback extractCallbackSpec = new ArchiveExtractCallback();
        IArchiveExtractCallback extractCallback = extractCallbackSpec;
        extractCallbackSpec.Init(archive,"test7Zipex");
        extractCallbackSpec.PasswordIsDefined = false;
         int res = archive.Extract(null, -1, mode , extractCallback, parent_dir);
    }

    static void extract(IInArchive archive,String proccessProc) throws SQLException,
                                                            IOException {
        
        ArchiveExtractCallback extractCallbackSpec = new ArchiveExtractCallback();
        IArchiveExtractCallback extractCallback = extractCallbackSpec;
        extractCallbackSpec.Init(archive,proccessProc);
        extractCallbackSpec.PasswordIsDefined = false;        
        int res = archive.Extract(null, -1, IInArchive.NExtract_NAskMode_kExtract, extractCallback, "");
    }
    static void testFile(IInArchive archive,String proccessProc,int[] listExtract) throws SQLException,
                                                            IOException {
        
        ArchiveExtractCallback extractCallbackSpec = new ArchiveExtractCallback();
        extractCallbackSpec.DEBUG=false;
        IArchiveExtractCallback extractCallback = extractCallbackSpec;
        extractCallbackSpec.Init(archive,proccessProc);
        extractCallbackSpec.PasswordIsDefined = false;
        
        int res = archive.Extract(listExtract, listExtract.length, IInArchive.NExtract_NAskMode_kTest, extractCallback, "");
        // int res = archive.Extract(arrays, 2, IInArchive.NExtract_NAskMode_kExtract, extractCallback, "");
    }
    static void extractFile(IInArchive archive,String proccessProc,int[] listExtract) throws SQLException,
                                                            IOException {
        
        ArchiveExtractCallback extractCallbackSpec = new ArchiveExtractCallback();
        extractCallbackSpec.DEBUG=false;
        IArchiveExtractCallback extractCallback = extractCallbackSpec;
        extractCallbackSpec.Init(archive,proccessProc);
        extractCallbackSpec.PasswordIsDefined = false;
        
        int res = archive.Extract(listExtract, listExtract.length, IInArchive.NExtract_NAskMode_kExtract, extractCallback, "");
        // int res = archive.Extract(arrays, 2, IInArchive.NExtract_NAskMode_kExtract, extractCallback, "");
    }
    private static void show_array_info( oracle.sql.ARRAY p_in )
    throws SQLException{

        System.out.println( "Array is of type      " +
                             p_in.getSQLTypeName() );
        System.out.println( "Array is of type code " +
                             p_in.getBaseType() );
        System.out.println( "Array is of length    " +
                             p_in.length() );
    }    
    public static void getFilesFromBlob(String proccessProc,BLOB blob,oracle.sql.ARRAY p_in) throws SQLException,
                                                                      IOException,
                                                                      Invalid7zArchiveException {                
        String[] valStr = (String[])p_in.getArray();
        int[] values=new int[valStr.length];            
        for (int i=0;i<valStr.length;i++){
            values[i]=Integer.parseInt(valStr[i])-1;
        }            
        inStreamBLOB istream;        
        istream = new inStreamBLOB(blob);
        IInArchive archive = new Handler();            
        int ret = archive.Open( istream );
        if (ret != 0) {            
            throw new Invalid7zArchiveException("Invalid 7z archive");            
        }        
        extractFile(archive,proccessProc,values);
    }
    public static void getAllFilesFromBlob(String proccessProc,BLOB blob) throws SQLException,
                                                        Invalid7zArchiveException,
                                                        IOException {        
        inStreamBLOB istream;        
        istream = new inStreamBLOB(blob);
        IInArchive archive = new Handler();            
        int ret = archive.Open( istream );
        if (ret != 0) {
            // System.out.println("ERROR !");
            throw new Invalid7zArchiveException("Invalid 7z archive");
            // return ;
        }        
        extract(archive,proccessProc);     
    }
    public static void getListFiles(BLOB blob,oracle.sql.ARRAY[] p_out) throws SQLException,
                                                                   IOException {
        inStreamBLOB istream;        
        istream = new inStreamBLOB(blob);
        IInArchive archive = new Handler();            
        int ret = archive.Open( istream );
        int arcSize=archive.size();
        String[] files = new String[arcSize]; 
        for(int i = 0; i < arcSize; i++) {
            SevenZipEntry item = archive.getEntry(i);
            files[i]=item.getName();
        }        
        Connection conn = new OracleDriver().defaultConnection();
        ArrayDescriptor descriptor =
               ArrayDescriptor.createDescriptor( vArrayType, conn );
        p_out[0] = new ARRAY( descriptor, conn, files ); 
    }
    public static void getListTestFiles(BLOB blob,oracle.sql.ARRAY[] p_out) throws SQLException,
                                                                   IOException {
        inStreamBLOB istream;        
        istream = new inStreamBLOB(blob);
        IInArchive archive = new Handler();            
        int ret = archive.Open( istream );
        int arcSize=archive.size();
        String[] files = new String[arcSize]; 
        int[] values=new int[arcSize]; 
        for(int i = 0; i < arcSize; i++) {
            SevenZipEntry item = archive.getEntry(i);
            files[i]=item.getName();
            values[i]=i;
        }        
        Connection conn = new OracleDriver().defaultConnection();
        ArrayDescriptor descriptor =
               ArrayDescriptor.createDescriptor(vArrayType, conn );
        testFile(archive,"",values);
        p_out[0] = new ARRAY( descriptor, conn, files ); 
    }
    public static void getFilesinBloob(BLOB blob){
        Vector<String> listOfNames = new Vector<String>();
        inStreamBLOB istream;

        try {
            istream = new inStreamBLOB(blob);
            IInArchive archive = new Handler();            
            int ret = archive.Open( istream );
            if (ret != 0) {
                // System.out.println("ERROR !");
                throw new Invalid7zArchiveException("Invalid 7z archive");
                // return ;
            }
            listing(archive,listOfNames,true);
            test(archive,listOfNames,IInArchive.NExtract_NAskMode_kExtract, "");;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static IInArchive archive;
    private static ArchiveExtractOneFileCallback extractCallbackSpec;
    public static void initArchive(BLOB blob) throws SQLException,
                                                          IOException,
                                                          Invalid7zArchiveException{
        inStreamBLOB istream;        
        istream = new inStreamBLOB(blob);
        archive = new Handler();            
        int ret = archive.Open( istream );
        if (ret != 0) {            
            throw new Invalid7zArchiveException("Invalid 7z archive");            
        }        
        extractCallbackSpec = new ArchiveExtractOneFileCallback();
        extractCallbackSpec.DEBUG=false;
        
        extractCallbackSpec.Init(archive);
    }
    public static BLOB getFile(Integer idx) throws SQLException, IOException {
        BLOB tmpBLOB;
        OracleDriver oracleDriver = new oracle.jdbc.driver.OracleDriver();
        java.sql.Connection conn = null;
        conn = oracleDriver.defaultConnection();
        tmpBLOB = BLOB.createTemporary(conn, true, BLOB.DURATION_SESSION);
        tmpBLOB.open(BLOB.MODE_READWRITE);       
        extractCallbackSpec.SetStream(tmpBLOB.setBinaryStream(0));
        int[] listExtract=new int[1];
        listExtract[0]=idx-1;
        IArchiveExtractCallback extractCallback = extractCallbackSpec;
        int res = archive.Extract(listExtract, listExtract.length, IInArchive.NExtract_NAskMode_kExtract, extractCallback, "");
        
        if (res != 0) {  
         System.out.println(res);    
        }        
        
        return tmpBLOB;
    }
    public static BLOB getFileByIndex(BLOB blob,Integer idx) throws SQLException,
                                                          IOException,
                                                          Invalid7zArchiveException {      
        
        inStreamBLOB istream;        
        istream = new inStreamBLOB(blob);
        IInArchive archive = new Handler();            
        int ret = archive.Open( istream );
        if (ret != 0) {            
            throw new Invalid7zArchiveException("Invalid 7z archive");            
        }        
        ArchiveExtractOneFileCallback extractCallbackSpec = new ArchiveExtractOneFileCallback();
        extractCallbackSpec.DEBUG=false;
        IArchiveExtractCallback extractCallback = extractCallbackSpec;
        extractCallbackSpec.Init(archive);
        BLOB tmpBLOB;
        OracleDriver oracleDriver = new oracle.jdbc.driver.OracleDriver();
        java.sql.Connection conn = null;
        conn = oracleDriver.defaultConnection();
        tmpBLOB = BLOB.createTemporary(conn, true, BLOB.DURATION_SESSION);
        tmpBLOB.open(BLOB.MODE_READWRITE);       
        extractCallbackSpec.SetStream(tmpBLOB.setBinaryStream(0));
        int[] listExtract=new int[1];
        listExtract[0]=idx-1;
        int res = archive.Extract(listExtract, listExtract.length, IInArchive.NExtract_NAskMode_kExtract, extractCallback, "");
        
        if (res != 0) {  
         System.out.println(res);    
        }
        
        
        return tmpBLOB;
    }
}
