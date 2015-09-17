package transfer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
	ServerSocket socketToAccept;
	ServerSocket socketToSend;
	private Lock lock;
	private ExecutorService executorService1;
	private ExecutorService executorService2;
	private final int POOL_SIZE = 10;
	public Server() throws IOException{
		 lock = new ReentrantLock(true);
		 socketToAccept =new ServerSocket(2020);// to accept
		 socketToSend =new ServerSocket(2021);// to send
	     executorService1=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*POOL_SIZE);
	     executorService2=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*POOL_SIZE);
	}
	public static void main(String[] args){
		Server server;
		try {
			server = new Server();
			Thread aThread = new Thread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					server.acceptPaint();
				}});
			Thread sThread = new Thread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					server.sendPaint();
				}});
			aThread.start();
			sThread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("server develop fail");;
		}
		
	}
	public void acceptPaint(){
		while(true){
			System.out.println("ready to accept");
			try {
				Socket connectionSocket = socketToAccept.accept();
				executorService1.execute(new HandlerToAccept(connectionSocket,lock));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.print("problem");
			}
			
		}
	}
	public void sendPaint(){
		while(true){
			Socket connectionSocket = null;
			System.out.println("ready to send");
			try {
				connectionSocket = socketToSend.accept();
				executorService2.execute(new HandlerToSend(connectionSocket));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
