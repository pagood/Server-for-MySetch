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
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import org.json.JSONException;

import util.DistanceUtil;
import util.JsonHelper;
import bean.Sketch;

public class HandlerToSend implements Runnable {
	private static final double RADIUS = 5.0;
	private Socket socket;
	private double longtitude;
	private double latitude;
	private final String PATH = "/Users/xiaoyu/Documents/server_sketch";
	public HandlerToSend(Socket socket){
		this.socket = socket;
		 
	}
	//first transfer the size of file to seprate the file,using 8 bytes
	public byte[] serialize(int num){
		 byte[] ans = new byte[4];
		 ans[0] = (byte)(num & 0xff);
		 ans[1] = (byte)((num & 0xff00) >> 8);
		 ans[2] = (byte)((num & 0xff0000) >> 16);
		 ans[3] = (byte)((num & 0xff000000) >> 24);
		 return ans;
		 
	 }
	@Override
	public void run() {
		// TODO Auto-generated method stub
		BufferedInputStream bi = null;
		BufferedOutputStream bo = null;
		try {
			//first accept the info of location
			DataInputStream di = new DataInputStream(socket.getInputStream());
			//info first
			
			String jsonString = di.readUTF();
			Sketch sketch = new Sketch();
			JsonHelper.toJavaBean(sketch, jsonString);
			longtitude = Double.parseDouble(sketch.getLongtitude());
			latitude = Double.parseDouble(sketch.getLatitude());
			System.out.println("接收坐标成功 lon is" + longtitude + " lat is " + latitude);
			//then image
			bo = new BufferedOutputStream(socket.getOutputStream());
			
			//traversal the DB,find out the right sketch and write
			ArrayList<String> paths = traverserDB();
			
			
			for(int i = 0 ;i < paths.size();i ++){
				File file = new File(PATH + "/" + paths.get(i));
				System.out.println(paths.get(i));
				int len = (int)file.length();
				System.out.println(len);
				byte[] size = serialize(len);
				bo.write(size);
				bi = new BufferedInputStream(new FileInputStream(file));
				byte[] buf = new byte[1024];
				int temp = 0;
				while((temp = bi.read(buf)) != -1) {
	                bo.write(buf,0,temp);
	            }
				bo.flush();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private ArrayList<String> traverserDB() {
		// TODO Auto-generated method stub
		ArrayList<String> paths = new ArrayList<>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			java.sql.Connection coon = java.sql.DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/hehe?useUnicode=true&characterEncoding=GBK","root","");
			java.sql.Statement stmt = coon.createStatement();
			
			String sql = "select * from sketchs";
			java.sql.ResultSet res = stmt.executeQuery(sql);
			
			while(res.next()){
				System.out.println("add !" + DistanceUtil.getDistance(latitude, longtitude, res.getDouble("latitude"), res.getDouble("longtitude")) + res.getDouble("latitude") + " " +res.getDouble("longtitude"));
				
				if(DistanceUtil.getDistance(latitude, longtitude, res.getDouble("latitude"), res.getDouble("longtitude")) < RADIUS){
					paths.add(res.getInt("id") + ".png");
					
				}
			}
			
			res.close();
			stmt.close();
			coon.close();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return paths;
	}


}
