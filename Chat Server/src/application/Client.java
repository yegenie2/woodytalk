package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//�Ѹ��� Ŭ���̾�Ʈ�� ����� �� �ְ� �ϴ� Ŭ���� ����
public class Client {
	
	Socket socket;	//���ϻ���
	
	public Client(Socket socket) {	
		this.socket = socket;	//�ʱ�ȭ
		receive();
	}
	
	//Ŭ���̾�Ʈ���� �޼����� ���� �޴� �޼ҵ�
	public void receive() {
		Runnable thread = new Runnable() {	//�ϳ��� thread ����.
			@Override
			public void run() {	//run�ȿ��� thread�� ��� �����ϴ��� �������ݴϴ�.
				try {
					while(true) {	
						InputStream in = socket.getInputStream();	//�������޹ް� inputstream��ü���
						byte[] buffer = new byte[512];	//512����Ʈ ���޹���
						int length = in.read(buffer);
						while(length == -1) throw new IOException(); 
						System.out.println("[�޽��� ���� ����]"
								+ socket.getRemoteSocketAddress()
								+ ": "+ Thread.currentThread().getName());
						String message = new String(buffer, 0, length, "UTF-8");
						for(Client client : Main.clients) {
							client.send(message);
						}
					}
				} catch(Exception e) {
					try {
						System.out.println("[�޽��� ���� ����]"
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
	//Ŭ���̾�Ʈ���� �޼����� �����ϴ� �޼ҵ�
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();	//�������� outputstream !!!
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					try {
						System.out.println("[�޽��� �۽� ����]"
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
