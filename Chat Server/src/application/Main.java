package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class Main extends Application {
	
	public static ExecutorService threadPool;	//threadPool�� thread �� �����ؼ� ���� ���� ���� �������ش�.
	public static Vector<Client> clients = new Vector<Client>();	//������ Ŭ���̾�Ʈ���� ������ �� �ְ� ����
	
	ServerSocket serverSocket;
	
	//������ �����ؼ� Ŭ���̾�Ʈ ������ ��ٸ���.
	public void startServer(String IP, int port) {	//� IP�� port�� �����ϴ��� 
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
		} catch (Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		//Ŭ���̾�Ʈ�� ������ ������ ��� ��ٸ��� ������
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[Ŭ���̾�Ʈ ����]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
					} catch (Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
				
			}
			
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	//������ �۵��� ������Ų��.
	public void stopServer() {
		try {
			Iterator<Client> iterator = clients.iterator();	//�۵����� ��� ������ �ݾ��ݴϴ�.
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
		}
		//���� ���� ��ü �ݱ�
		if(serverSocket != null && !serverSocket.isClosed()) {
			serverSocket.close();
		}
		//������ Ǯ �����ϱ�
		if(threadPool != null && !threadPool.isShutdown()) {
			threadPool.shutdown();
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//UI�����, ���α׷� ����
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("�������", 15));

		
		Button toggleButton = new Button("�����ϱ�");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		String IP = "192.168.0.6";
		int port = 9876;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("�����ϱ�")) {
				startServer(IP, port);
				Platform.runLater(() -> {
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
			});
		}
		
	});
	
		Scene scene = new Scene(root, 400, 400);
		BackgroundFill background_fill = new BackgroundFill(Color.HOTPINK, CornerRadii.EMPTY, Insets.EMPTY);
		Background background = new Background(background_fill);
		root.setBackground(background);
		//�̹��� ����
		Image image = new Image("file:woody.png");
		ImageView iv = new ImageView();
		iv.setImage(image);
		
		iv.setPreserveRatio(true);
		iv.setFitHeight(320);
		iv.setLayoutY(30);	//�̹��� ��ġ �����߾��
		iv.setLayoutX(120);
		root.getChildren().add(iv);
		
		primaryStage.setTitle("��� TALK[����]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	// ���α׷��� ������
	public static void main(String[] args) {
		launch(args);
	}
}
