package mcjty.enigma.web;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Web extends Thread{

	public Web() {
		this.setDaemon(true);
		this.setPriority(Thread.MIN_PRIORITY);
		this.start();
	}
	
	public void run(){
		ServerSocket ss = null;
		Socket socket = null;

		try {
			ss = new ServerSocket(Data.webPort);
			System.out.println("WEBServer Started on port " + Data.webPort);
			
			while (true) {
				try{
					socket = ss.accept();
					String myText = Data.webResponse;

					String response = "HTTP/1.1 200 OK\r\n" +
			                    "Server: Minecraft/Enigma\r\n" +
			                    "Content-Type: application/json\r\n" +
			                    "Content-Length: " + myText.length() + "\r\n" +
			                    "Connection: close\r\n\r\n";
			        String result = response + myText;
			        socket.getOutputStream().write(result.getBytes());
			        socket.getOutputStream().flush();
			        socket.close();
					} catch (Exception e) {
						socket.close();
					}
				}
			
		} catch (IOException e1) {
		}finally{
			try {
				ss.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
