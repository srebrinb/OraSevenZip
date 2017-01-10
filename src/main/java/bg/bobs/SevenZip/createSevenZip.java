package bg.bobs.SevenZip;


import com.swemel.sevenzip.CodeProgressImpl;
import com.swemel.sevenzip.Folder;
import com.swemel.sevenzip.LZMACoderInfo;
import com.swemel.sevenzip.UpdateItem;
import com.swemel.sevenzip.archive.ArchiveDatabase;
import com.swemel.sevenzip.archive.FileItem;
import com.swemel.sevenzip.archive.OutArchive;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Vector;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.driver.OracleDriver;

import oracle.sql.BLOB;

public class createSevenZip {
    public createSevenZip() {
        super();
    }
    private Vector<UpdateItem> updateItems = new Vector<UpdateItem>();
    int currentIndex = 0;
    private outStreamBLOB outStream;
    private CodeProgressImpl cp;
    OracleResultSet cursor;

    public int getProgress() {
        if (cp != null)
            return cp.getProgress();
        else
            return 0;
    }


    /**
     * @param files
     */
    private void generateUpdateItems(File... files) {
        for (File file : files) {
            if (!file.canRead()) {
                System.err.println("Can't read from file:" +
                                   file.getAbsolutePath());
                continue;
            }

            int indexInArchive = -1;
            UpdateItem ui = new UpdateItem();
            ui.setNewProps(true);
            ui.setNewData(true);
            ui.setIndexInArchive(indexInArchive);
            ui.setIndexInClient(currentIndex++);
            ui.setIsAnti(false);
            ui.setSize(0);
            ui.setATimeDefined(false);
            ui.setMTime(file.lastModified());
            ui.setMTimeDefined(file.lastModified() != 0L);
            ui.setMTimeDefined(true);
            ui.setCTimeDefined(false);
            ui.setName(file.getName());
            ui.setFullName(file.getAbsolutePath());
            ui.setIsDir(file.isDirectory());
            ui.setIsAnti(false);
            ui.setSize(file.length());
            if (file.isDirectory())
                ui.setAttrib(16);
            else
                ui.setAttrib(32);
            ui.setAttribDefined(true);
            updateItems.add(ui);
            if (file.isDirectory()) {
                collectFiles(file, file.getName());
            }
        }
    }

    private void generateUpdateItems(String... fileNames) {
        for (int i = 0; i < fileNames.length; i++) {
            File file = new File(fileNames[i]);
            if (!file.canRead()) {
                System.err.println("Can't read from file:" +
                                   file.getAbsolutePath());
                continue;
            }

            int indexInArchive = -1;
            UpdateItem ui = new UpdateItem();
            ui.setNewProps(true);
            ui.setNewData(true);
            ui.setIndexInArchive(indexInArchive);
            ui.setIndexInClient(currentIndex++);
            ui.setIsAnti(false);
            ui.setSize(0);
            ui.setATimeDefined(false);
            ui.setMTime(file.lastModified());
            ui.setMTimeDefined(file.lastModified() != 0L);
            ui.setMTimeDefined(true);
            ui.setATimeDefined(false);
            ui.setCTimeDefined(false);
            ui.setName(file.getName());
            ui.setFullName(file.getAbsolutePath());
            ui.setIsDir(file.isDirectory());
            ui.setIsAnti(false);
            ui.setSize(file.length());
            if (file.isDirectory())
                ui.setAttrib(16);
            else
                ui.setAttrib(32);
            ui.setAttribDefined(true);
            if (file.isDirectory()) {
                collectFiles(file, file.getName());
                updateItems.add(ui);
            }
        }

    }

    private void collectFiles(File file, String path) {
        for (File child : file.listFiles()) {
            if (!child.canRead()) {
                System.err.println("Can't read from file:" +
                                   file.getAbsolutePath());
                continue;
            }

            int indexInArchive = -1;
            UpdateItem ui = new UpdateItem();
            ui.setNewProps(true);
            ui.setNewData(true);
            ui.setIndexInArchive(indexInArchive);
            ui.setIndexInClient(currentIndex++);
            ui.setIsAnti(false);
            ui.setSize(0);
            ui.setATimeDefined(false);
            ui.setMTime(child.lastModified());
            ui.setMTimeDefined(true);
            ui.setATimeDefined(false);
            ui.setCTimeDefined(false);
            ui.setName(path + "/" + child.getName());
            ui.setFullName(child.getAbsolutePath());
            ui.setIsDir(child.isDirectory());
            ui.setIsAnti(false);
            ui.setSize(child.length());
            if (child.isDirectory())
                ui.setAttrib(16);
            else
                ui.setAttrib(32);
            ui.setAttribDefined(true);
            updateItems.add(ui);
            if (child.isDirectory()) {
                collectFiles(child, path + "/" + child.getName());
            }
        }
    }


    public createSevenZip(String archiveName,
                          File... files) throws IOException {
        generateUpdateItems(files);
        outStream = new outStreamBLOB(new File(archiveName), "rw");
    }

    public createSevenZip(String archiveName,
                          String... fileNames) throws IOException {
        generateUpdateItems(fileNames);
        outStream = new outStreamBLOB(new File(archiveName), "rw");
    }

    public createSevenZip(String archiveName,
                          OracleResultSet in_cursor) throws IOException {
        cursor = in_cursor;
        outStream = new outStreamBLOB(new File(archiveName), "rw");
    }

    public createSevenZip(OutputStream os,
                          OracleResultSet in_cursor) throws IOException,
                                                            SQLException {
        cursor = in_cursor;
        outStream = new outStreamBLOB(os);
    }

    private static void setMethodProperties(com.swemel.sevenzip.compression.lzma.Encoder encoder,
                                            long inSizeForReduce,
                                            LZMACoderInfo info) {

        boolean tryReduce = false;
        int dictionarySize = 1 << 24;
        int reducedDictionarySize = 1 << 10;
        if (inSizeForReduce != 0) {
            for (; ; ) {
                int step = (reducedDictionarySize >> 1);
                if (reducedDictionarySize >= inSizeForReduce) {
                    tryReduce = true;
                    break;
                }
                reducedDictionarySize += step;
                if (reducedDictionarySize >= inSizeForReduce) {
                    tryReduce = true;
                    break;
                }
                if (reducedDictionarySize >= (3 << 30))
                    break;
                reducedDictionarySize += step;
            }
        }
        if (tryReduce)
            if (reducedDictionarySize < dictionarySize)
                dictionarySize = reducedDictionarySize;
        info.setDictionarySize(dictionarySize);
        encoder.setDictionarySize(dictionarySize);
        encoder.setNumFastBytes(128);
        encoder.setMatchFinder(1);
        encoder.setLcLpPb(3, 0, 2);
    }

    public static boolean isExeExt(String s) {
        if (s.equalsIgnoreCase("exe"))
            return true;
        if (s.equalsIgnoreCase("dll"))
            return true;
        if (s.equalsIgnoreCase("ocx"))
            return true;
        if (s.equalsIgnoreCase("sfx"))
            return true;
        if (s.equalsIgnoreCase("sys"))
            return true;
        return false;

    }

    public void createArchive() throws IOException {
        OutArchive archive = new OutArchive();
        ArchiveDatabase newDatabase = new ArchiveDatabase();
        long kLzmaDicSizeX5 = 1 << 24;
        long numSolidFiles = Long.MAX_VALUE;
        long numSolidBytes = kLzmaDicSizeX5 << 7;
        long complexity = 0;
        long inSizeForReduce2 = 0;
        long inSizeForReduce = 1 << 16;

        archive.create(outStream, false);
        archive.SkipPrefixArchiveHeader();

        {
            com.swemel.sevenzip.compression.lzma.Encoder encoder =
                new com.swemel.sevenzip.compression.lzma.Encoder();
            LZMACoderInfo info = new LZMACoderInfo();
            setMethodProperties(encoder, inSizeForReduce, info);
            cp = new CodeProgressImpl(complexity);

            long fullSize = 0;

            long totalSize = 0;

            Folder folder = new Folder();
            folder.getCoders().clear();
            folder.getPackStreams().clear();
            folder.getCoders().add(info);
            folder.getPackStreams().add(0);
            int numUnpackStreams = 0;


            SevenZipStmtInStream inStream = new SevenZipStmtInStream();
            inStream.init(cursor);
            // inStream.init(updateItems, indices, 0, numSubFiles);
            encoder.code(inStream, outStream, -1, -1, cp);
            cp.setTotalInSize();
            updateItems = inStream.getFiles();
            int numFiles = updateItems.size();
            folder.addUnpackSize(inStream.getFullSize());

            for (int i = 0; i < numFiles; i++) {
                UpdateItem ui = updateItems.get(i);
                FileItem file = new FileItem();
                if (ui.isDir())
                    continue;
                file.setName(ui.getName());
                if (ui.isAttribDefined()) {
                    file.setAttributes(ui.getAttrib());
                    file.setAttributesDefined(true);
                }
                file.setLastAccessTime(ui.getATime());
                file.setAnti(ui.isAnti());
                file.setIsStartPosDefined(false);

                file.setSize(ui.getSize());
                file.setDirectory(ui.isDir());
                file.setHasStream(ui.hasStream());

                if (file.getSize() != 0) {
                    file.setCrcDefined(true);                    
                    file.setFileCRC(inStream.getCrc(i));
                    numUnpackStreams++;
                } else {
                    file.setCrcDefined(false);
                    file.setHasStream(false);
                }
                newDatabase.addMTimeDefined(ui.isMTimeDefined());
                if (ui.isMTimeDefined())
                    newDatabase.addMTime(ui.getMTime());
                newDatabase.addFile(file);
            }
            newDatabase.addFolder(folder);
            newDatabase.addPackSize(outStream.getSize());
            outStream.setSize(0);
            newDatabase.getNumUnPackStreamsVector().add(numUnpackStreams);

        }
        archive.writeDatabase(newDatabase);
        outStream.close();
    }


    public static void main(String[] args) {
        createSevenZip sz;

        try {
            //   sz = new createSevenZip("D:\\incubator\\commpressors\\test.7z", "D:\\incubator\\commpressors\\test\\normalFolder");
            //       sz = new createSevenZip("D:\\incubator\\commpressors\\test.7z", "E:\\efTools\\SOPHARMA\\tests\\db\\1");

            java.sql.Connection conn = null;
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
            conn =DriverManager.getConnection("jdbc:oracle:thin:@isdbd.dev.srv:1521/EIPDARCH.dev.srv",
                            "eipp_arch", "eipp_arch");


        /*    OracleCallableStatement call_stmt =
                (OracleCallableStatement)conn.prepareCall("call Compressor.filesForCompress(?)");
            call_stmt.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
            call_stmt.execute();

            System.out.println("init");
            OracleResultSet cursor = (OracleResultSet)call_stmt.getCursor(1);
        */
        String query = "select * from (\n" + 
                    "       select d.id filename,\n" + 
                    "         d.dcont filecont         \n" + 
                    "       from docs d\n" + 
                    "       where \n" + 
                    "            d.dtype='INV'\n" + 
                    "       order by d.id\n" + 
                    "      )        \n" + 
                    "      where rownum <100000";
           OracleStatement stmt = (OracleStatement)conn.createStatement();
           OracleResultSet cursor = (OracleResultSet)stmt.executeQuery(query);
            
            sz = new createSevenZip("D:\\incubator\\commpressors\\test_100K_2.7z", cursor);
            sz.createArchive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static OracleDriver oracleDriver = new oracle.jdbc.driver.OracleDriver();
    private static java.sql.Connection conn = null;    

    public static oracle.sql.BLOB Create7ZIP() throws SQLException,
                                                      IOException {
        return Create7ZIP("test_7zip.getfiles");
    }

    /**
     * @param proccessProc  name PLSQL procedure getFiles(oc in out Trefcursor);
     *  cursor columns  varchar filename, blob filecont
     * @return BLOB compress files
     * @throws SQLException
     * @throws IOException
     */
    public static oracle.sql.BLOB Create7ZIP(String proccessProc) throws SQLException,
                                                                         IOException {
        conn = oracleDriver.defaultConnection();
        conn.setAutoCommit(false);
        OracleCallableStatement call_stmt =
            (OracleCallableStatement)conn.prepareCall("call " + proccessProc + "(?)");
        call_stmt.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
        call_stmt.execute();
        BLOB dest_blob = BLOB.createTemporary(conn, false, BLOB.DURATION_CALL);
        dest_blob.open(BLOB.MODE_READWRITE);
        OracleResultSet cursor = (OracleResultSet)call_stmt.getCursor(1);
        OutputStream os = dest_blob.setBinaryStream(0);
        createSevenZip sz = new createSevenZip(os, cursor);
        sz.createArchive();
        call_stmt.close();
        return dest_blob;
    }
}
