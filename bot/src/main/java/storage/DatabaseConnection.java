package main.java.storage;

import main.java.content.Record;

import java.sql.*;
import java.util.List;

/**
 * Created by Dick Zhou on 3/30/2017.
 * Connect to database to store or retrieve result.
 */
public class DatabaseConnection {
    private static final String driver = "com.mysql.jdbc.Driver";
    private static final String databaseUrl = "jdbc:mysql://qc.hehehey.com:3306/news";
    private static final String user = "root";
    private static final String password = "3.1415926";
    private Connection conn;

    public boolean connect(){
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(databaseUrl, user, password);
            if (!conn.isClosed()) {
                System.out.println("Succeeded connecting to the Database!");
                return true;
            }else
                return false;
        } catch (ClassNotFoundException e){
            e.printStackTrace();
            return false;
        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public void close(){
        try{
            conn.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public boolean query(String sql){
        try{
            conn.createStatement().execute(sql);
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public Record getRecord(String taskId) {
        String sql = "" + taskId;
        Record record = new Record();
        try{
            ResultSet result = conn.createStatement().executeQuery(sql);
            record.setUrl(result.getString("url"));
            record.setContent(result.getString("content"));
           // record.setImages(result.getBinaryStream("content"))//采用二进制流处理;
            record.setReadCount(result.getInt("readcount"));
            record.setParticipateCount(result.getInt("participatecount"));
            record.setCommentCount(result.getInt("commentcount"));
            return record;
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }

    }

    public boolean insert(Record record){
        String sql = "" + record.getTaskId();
        try{
            PreparedStatement insert=
                    conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            if(insert.executeUpdate()==1){
                return true;
            }else return false;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Record> get(String id) {
        return null;
    }

}
