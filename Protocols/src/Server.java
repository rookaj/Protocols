import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

//1byte to 64kb for graph
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
    		byte[] clientInput = new byte[5];
    		
    		DataInputStream in = new DataInputStream(conn.getInputStream());

            clientInput[0] = in.readByte();
            clientInput[1] = in.readByte();
            clientInput[2] = in.readByte();
            clientInput[3] = in.readByte();
            clientInput[4] = in.readByte();
            ByteBuffer byteBuffer = ByteBuffer.wrap(clientInput, 1, 4);

            int totalSize = byteBuffer.getInt();
            System.out.println("About to read " + totalSize + " bytes");

            byte[] messageByte = new byte[totalSize];
            //The following code shows in detail how to read from a TCP socket
            String s = "";
            while(s.length() < totalSize)
            {
                count = in.read(messageByte);
                s += new String(messageByte, 0, count);

            }
           
    		conn.close();
    	} catch (IOException e) {
    		System.err.println(e);
    	}
    }
}
