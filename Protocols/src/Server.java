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
        if(args.length != 2) {
            System.err.println("Invalid Arguments.");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String transport = args[1];
        
        if(transport.equals("TCP")) {
            ServerSocket servSock = new ServerSocket(port, 5);
            try {
    	        for(;;) {
    	        	Socket conn = servSock.accept();
    	        	new Thread(new Server(conn)).start();
    	        }
	        } catch(Exception e) {
	            servSock.close();
	        } finally {
	            servSock.close();
	        }
        } else if(transport.equals("UDP")) {
            try {
                UDPRun(port);
            } catch(Exception e) {
                System.err.println(e);
            }
        } else {
        	System.err.println("Invalid Transport Protocol (TCP or UDP)");
        	System.exit(1);
        }
    }
    
    public static void UDPRun(int port) throws Exception
    {
        int msgCount = 0;
        String ackProtocol = "Pure Streaming";
        DatagramSocket servSock = new DatagramSocket(port);
        
        byte[] clientAck = new byte[1];
        byte[] clientSize = new byte[5];
        
        DatagramPacket recvAck = new DatagramPacket(clientAck, clientAck.length);
        DatagramPacket recvSize = new DatagramPacket(clientSize, clientSize.length);
       
        
        servSock.receive(recvAck);
       
        InetAddress IPAddress = recvAck.getAddress();
        int clientPort = recvAck.getPort();
        DatagramPacket sendAck = new DatagramPacket(clientSize, clientSize.length, IPAddress, clientPort);
        byte ack = recvAck.getData()[0];
        
        if(ack == 1) {
            byte[] ackArr = {ack};
            sendAck = new DatagramPacket(ackArr, ackArr.length, IPAddress, clientPort);
            ackProtocol = "Stop-and-Wait";
            
            servSock.receive(recvSize);
        } else {
            servSock.send(sendAck);
            servSock.receive(recvSize);
            servSock.send(sendAck);
        }
        
        ByteBuffer byteBuffer = ByteBuffer.wrap(recvSize.getData(), 0, 4);
        int totalSize = byteBuffer.getInt();

       
        

        
        
        byte[] msg = new byte[totalSize];
        String s = "";
        
        servSock.setSoTimeout(1000);

        try{
            while(s.length() < totalSize) {
                  DatagramPacket receivePacket = new DatagramPacket(msg, msg.length);
                  servSock.receive(receivePacket);
                  s += new String(receivePacket.getData(), 0, receivePacket.getLength());
                  msgCount++;
                  
                  if(ack == 1) {
                      servSock.send(sendAck);
                  }
    
            }
        } catch(SocketTimeoutException e) {
            System.out.println("Socket Closed Due to Timeout from Client.");
        }
        
        servSock.close();
        
        System.out.println("Acknowledgement Protocol: " + ackProtocol);
        System.out.println("Messages Read: " + msgCount);
        System.out.println("Bytes Read: " + s.length());
        

    }
    
    public void run()
    {
    	try {
    		int count = 0;
    		int totalSize = 0;
    		int ack = 0;
    		int msgCount = 0;
    		byte[] clientInput = new byte[5];
    		String ackProtocol = "Pure Streaming";
    		
    		DataInputStream fromClient = new DataInputStream(conn.getInputStream());
    		DataOutputStream toClient = new DataOutputStream(conn.getOutputStream());
    		
            clientInput[0] = fromClient.readByte();
            clientInput[1] = fromClient.readByte();
            clientInput[2] = fromClient.readByte();
            clientInput[3] = fromClient.readByte();
            clientInput[4] = fromClient.readByte();
            ByteBuffer byteBuffer = ByteBuffer.wrap(clientInput, 1, 4);

            totalSize = byteBuffer.getInt();
            
            
            if(clientInput[0] == 1) {
                ack = 1;
                ackProtocol = "Stop-and-Wait";
            } else {
                toClient.writeByte(1); //Send acknowledgement of Instructions
            }

            byte[] msg = new byte[totalSize];
            String s = "";
            while(s.length() < totalSize)
            {
                count = fromClient.read(msg);
                s += new String(msg, 0, count);
                msgCount++;
                
                if(ack == 1) {
                    toClient.writeByte(1);
                }
            }
           
    		conn.close();
    		
    		System.out.println("Acknowledgement Protocol: " + ackProtocol);
    		System.out.println("Messages Read: " + msgCount);
    		System.out.println("Bytes Read: " + s.length());
    	} catch (IOException e) {
    		System.err.println(e);
    	}
    }
}
