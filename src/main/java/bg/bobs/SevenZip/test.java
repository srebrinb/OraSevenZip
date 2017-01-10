package bg.bobs.SevenZip;
import com.swemel.sevenzip.UpdateItem;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Vector;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleResultSet;

public class test {
    public static void main(String[] args) throws SQLException, IOException {
    
    java.sql.Connection conn = null;
    DriverManager.registerDriver (new oracle.jdbc.OracleDriver());
    conn=DriverManager.getConnection("jdbc:oracle:thin:@nisd.adm.bnet:1521:nist", "java_utils", "j");
    
    
         OracleCallableStatement call_stmt = (OracleCallableStatement) conn.prepareCall(
                "call test_7zip.getfiles(?)");
        call_stmt.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
        call_stmt.execute();        
        System.out.println("init");
        OracleResultSet cursor = (OracleResultSet) call_stmt.getCursor(1);
        SevenZipStmtInStream instance;
        instance = new SevenZipStmtInStream();
        instance.init(cursor);
        // TODO review the generated test code and remove the default call to fail.
        byte[] data=new byte[3];
        //instance.read(data);      
        int ret=instance.read(data);
        //System.out.println(new String(data));
        while(ret != -1){
          ret=instance.read(data);          
        }        
         Vector<UpdateItem> updateItems = instance.getFiles();
         for(int i=0;i<updateItems.size();i++){
             UpdateItem ui = updateItems.get(i);      
             System.out.print(ui.getName());
             System.out.print(" ");
             System.out.print(ui.getSize());
             System.out.println();
         }
        
        
    }
}
