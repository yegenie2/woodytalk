package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//한명의 클라이언트랑 통신할 수 있게 하는 클래스 생성
public class Client {
	
	Socket socket;	//소켓생성
	
	public Client(Socket socket) {	
		this.socket = socket;	//초기화
		receive();
	}
	
	//클라이언트에게 메세지를 전달 받는 메소드
	public void receive() {
		Runnable thread = new Runnable() {	//하나의 thread 생성.
			@Override
			public void run() {	//run안에서 thread가 어떻게 동작하는지 정의해줍니다.
				try {
					while(true) {	
						InputStream in = socket.getInputStream();	//내용전달받게 inputstream객체사용
						byte[] buffer = new byte[512];	//512바이트 전달받음
						int length = in.read(buffer);
						while(length == -1) throw new IOException(); 
						System.out.println("[메시지 수신 성공]"
								+ socket.getRemoteSocketAddress()
								+ ": "+ Thread.currentThread().getName());
						String message = new String(buffer, 0, length, "UTF-8");
						for(Client client : Main.clients) {
							client.send(message);
						}
					}
				} catch(Exception e) {
					try {
						System.out.println("[메시지 수신 오류]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName()); 
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}
	//클라이언트에게 메세지를 전송하는 메소드
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();	//보낼때는 outputstream !!!
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					try {
						System.out.println("[메시지 송신 오류]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
				
			}
			
		};
		Main.threadPool.submit(thread);
		
	}
}
