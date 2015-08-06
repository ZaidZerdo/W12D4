package examples;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

	private static Integer primeCounter = 0;
	private static ArrayList<Worker> list;
	private static Object lock = new Object();
	
	// 0 - 1.000.000
	public static void main(String[] args) {
		try {
			ServerSocket server = new ServerSocket(8000);
			System.out.println("Server started.");
			
			int a = 0;
			int b = 200_000;
			int workLoad = 200_000;
			
			list = new ArrayList<>();
			long t = System.currentTimeMillis();
			while (b <= 100_000_000) {
				Socket client = server.accept();
				System.out.println("Got client!");
				
				String ip = client.getInetAddress().getHostAddress().toString();
				System.out.printf("%s got (%d, %d)\n", ip, a, b);
				
				Worker w = new Worker(client, a, b);
				list.add(w);
				
				if (b > 50_000_000) {
					workLoad = 100_000;
				}
				
				a += workLoad;
				b += workLoad;
			}
			
			int tempCounter = 0;
			for (Worker w : list) {
				try {
					if (w != null) {
						w.join();
						tempCounter += w.result;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			server.close();
			System.out.println("Total time: " + (System.currentTimeMillis() - t) + " [ms]");
			System.out.println("a: " + a);
			System.out.println("b: " + b);
			System.out.println("Primes: " + primeCounter);
			System.out.println("Primes: " + tempCounter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class Worker extends Thread {		
		private Socket socket;
		private int a;
		private int b;
		private int result = 0;
		
		public Worker(Socket socket, int a, int b) {
			this.socket = socket;
			this.a = a;
			this.b = b;
			this.start();
		}
		
		@Override
		public void run() {
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			
				writer.write(a + " " + b);
				writer.newLine();
				writer.flush();
				
				long t = System.currentTimeMillis();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				String msg = reader.readLine();
				
				String ip = socket.getInetAddress().getHostAddress().toString();
				try {
					long time = System.currentTimeMillis() - t;
					System.out.println(ip + ": " + msg + " Time: " + time + " [ms]");
					result += Integer.parseInt(msg);
					synchronized (lock) {
						primeCounter += Integer.parseInt(msg);
					}
				} catch (NumberFormatException ex) {
					System.err.println(ip + " napusta kucu velikog BitCampa.");
				}
				
				writer.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			
		}
	}
}
