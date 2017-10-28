import java.io.*;
import java.net.*;

public class Server implements Runnable {
	Socket conn;
	
	Server(Socket sock) {
		this.conn = sock;
	}
	
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        String transport = args[1];
        
        if(transport.equals("TCP")) {
	        ServerSocket svc = new ServerSocket(port, 5);
	        
	        for(;;) {
	        	Socket conn = svc.accept();
	        	new Thread(new Server(conn)).start();
	        }
        } else if(transport.equals("UDP")) {
        	
        } else {
        	System.err.println("Invalid Transport Protocol (TCP or UDP)");
        }
    }
    
    public void run() {
    	try {
    		int count = 0;
    		double expectedSize = 0.0;
    		BufferedReader fromClient = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    		DataOutputStream toClient = new DataOutputStream(conn.getOutputStream());
    		String line;
    		line = fromClient.readLine();
    		String[] input = line.split(" ");
    		int ack = Integer.parseInt(input[0]);
    		expectedSize = Double.parseDouble(input[1]);
    		
    		
    		while((line = fromClient.readLine()) != null) {
    			count += line.length();
    			if(count >= expectedSize) {
    				break;
    			}
    		}
    		
    		conn.close();
    	} catch (IOException e) {
    		System.err.println(e);
    	}
    }
}
