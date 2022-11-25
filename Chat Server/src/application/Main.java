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
	
	public static ExecutorService threadPool;	//threadPool로 thread 수 제한해서 서버 성능 저하 방지해준다.
	public static Vector<Client> clients = new Vector<Client>();	//접속한 클라이언트들을 관리할 수 있게 해줌
	
	ServerSocket serverSocket;
	
	//서버를 구동해서 클라이언트 연결을 기다린다.
	public void startServer(String IP, int port) {	//어떤 IP와 port로 연결하는지 
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
		
		//클라이언트가 접속할 때까지 계속 기다리는 쓰레드
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[클라이언트 접속]"
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
	
	//서버의 작동을 중지시킨다.
	public void stopServer() {
		try {
			Iterator<Client> iterator = clients.iterator();	//작동중인 모든 소켓을 닫아줍니다.
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
		}
		//서버 소켓 객체 닫기
		if(serverSocket != null && !serverSocket.isClosed()) {
			serverSocket.close();
		}
		//쓰레드 풀 종료하기
		if(threadPool != null && !threadPool.isShutdown()) {
			threadPool.shutdown();
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//UI만들고, 프로그램 동작
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("나눔고딕", 15));

		
		Button toggleButton = new Button("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		String IP = "192.168.0.6";
		int port = 9876;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("시작하기")) {
				startServer(IP, port);
				Platform.runLater(() -> {
					String message = String.format("[서버 시작]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("종료하기");
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[서버 종료]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("시작하기");
			});
		}
		
	});
	
		Scene scene = new Scene(root, 400, 400);
		BackgroundFill background_fill = new BackgroundFill(Color.HOTPINK, CornerRadii.EMPTY, Insets.EMPTY);
		Background background = new Background(background_fill);
		root.setBackground(background);
		//이미지 삽입
		Image image = new Image("file:woody.png");
		ImageView iv = new ImageView();
		iv.setImage(image);
		
		iv.setPreserveRatio(true);
		iv.setFitHeight(320);
		iv.setLayoutY(30);	//이미지 위치 조절했어요
		iv.setLayoutX(120);
		root.getChildren().add(iv);
		
		primaryStage.setTitle("우디 TALK[서버]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	// 프로그램의 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
