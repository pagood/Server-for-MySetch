package transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.locks.Lock;

import org.json.JSONException;

import util.JsonHelper;
import bean.Sketch;

public class HandlerToAccept implements Runnable{
	private Socket socket;
	private Lock lock;
	private int id;
	public HandlerToAccept(Socket socket,Lock lock){
		this.socket = socket;
		this.lock = lock;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Date date = new Date();
		String time = date.toString();
		BufferedOutputStream bo = null;
		DataInputStream bi = null;
		lock.lock();
		try{
			id = new File("/Users/xiaoyu/Documents/server_sketch/").listFiles().length;
			bo = new BufferedOutputStream(new FileOutputStream(new File("/Users/xiaoyu/Documents/server_sketch/" + id +".png")));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			lock.unlock();
		}
		try {
			bi = new DataInputStream(socket.getInputStream());
			//info first
			
			String jsonString = bi.readUTF();
			Sketch sketch = new Sketch();
			JsonHelper.toJavaBean(sketch, jsonString);
			
			//save into database
			saveToDB(sketch,id);
			
			//then img
			byte[] buf = new byte[1024];
            int temp = 0;
            while((temp = bi.read(buf)) != -1) {
                bo.write(buf,0,temp);
            }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
            if(bi != null) {
                try {
                    bi.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(bo != null) {
                try {
                    bo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
    			socket.close();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
		
	}
	private void saveToDB(Sketch sketch,int id) {
		// TODO Auto-generated method stub
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("succeed");
			java.sql.Connection coon = java.sql.DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/hehe?useUnicode=true&characterEncoding=GBK","root","");
			System.out.println("connection established");
			java.sql.Statement stmt = coon.createStatement();

			String inSql = String.format("insert into sketchs values(%d,%s,%s)", id,Double.parseDouble(sketch.getLongtitude()),Double.parseDouble(sketch.getLatitude()));
			stmt.executeUpdate(inSql);
			
			
			stmt.close();
			coon.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
