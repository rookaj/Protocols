import java.io.*;
import java.net.*;

public class Client {

    public static final int g = (int)Math.pow(2, 30);
    public static final int m = (int)Math.pow(2, 20);
    
    public static Socket getSocket(String host, int port)
    {
        try {
            Socket sock = new Socket(host, port);
            return sock;
        } catch(Exception e) {
            System.err.println("Could not connect to server " + host + "/" + port);
            System.exit(1);
        }
        
        return null;
    }
    
    public static void TCP_streaming(String host, int port, int size) throws Exception
    {
    	int count = g;
    	byte[] bytes = new byte[size];
    	Socket sock = getSocket(host, port);
    	DataOutputStream toServer = new DataOutputStream(sock.getOutputStream());
    	BufferedReader fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    	System.out.println(g);
    	toServer.writeByte(0);//telling server no ack
    	
    	toServer.writeInt(count); //telling server: 1g total size
    	long endTime = 0L;
    	long startTime = System.currentTimeMillis();
    	
    	while(count > 0) {
    		//if(bytes_sent != msg_size) {error}
    		toServer.write(bytes);
    		count -= bytes.length;
    	}
    	endTime = System.currentTimeMillis();
    	long elapsedTime = endTime - startTime;
    	double throughput = ((double)g/(double)elapsedTime)*(1000/(double)m);
    	
    	System.out.println(throughput);
    	sock.close();
    }
    
    public static void main(String[] args)
    {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String transport = args[2]; //TCP or UDP
        String ack = args[3]; //pure streaming or stop-and-wait
        int msgSize = Integer.parseInt(args[4]); //1 byte to 65,536 bytes
        
        String response = "";
        if(transport.equals("TCP")) {
            if(ack.equals("streaming")) {
            	try {
            		TCP_streaming(host, port, msgSize);
            	} catch(Exception e) {
            		System.err.println(e);
            	}
            } else if(ack.equals("stop-and-wait")) {
                
            } else {
                System.err.println("Invalid Acknowledgement Protocol (streaming or stop-and-wait)");
                System.exit(1);
            }
        } else if(transport.equals("UDP")) {
            if(ack.equals("streaming")) {
                
            } else if(ack.equals("stop-and-wait")) {
                
            } else {
                System.err.println("Invalid Acknowledgement Protocol (streaming or stop-and-wait)");
                System.exit(1);
            }
        } else {
            System.err.println("Invalid Transport Protocol (TCP or UDP)");
            System.exit(1);
        }
    }

}
