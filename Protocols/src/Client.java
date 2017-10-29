import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

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
    
    public static void TCPTimer(String host, int port, int size, int ack) throws Exception
    {
        int count = g/4096;
    	int total = 0;
    	byte[] bytes = new byte[size];
    	int msgCount = 0;
    	
    	Socket sock = getSocket(host, port);
    	DataOutputStream toServer = new DataOutputStream(sock.getOutputStream());
    	DataInputStream fromServer = new DataInputStream(sock.getInputStream());
    	
    	if(ack == 0) {
    	    toServer.writeByte(0); //telling server pure streaming
    	} else if(ack == 1) {
    	    toServer.writeByte(1); //telling server stop-and-wait
    	}
    	
    	toServer.writeInt(count); //telling server: 1g total size
    	long endTime = 0L;
    	long startTime = System.currentTimeMillis();
    	
    	while(count > 0) {
    		//if(bytes_sent != msg_size) {error}
    		toServer.write(bytes);
    		count -= bytes.length;
    		total += bytes.length;
    		msgCount++;
    		if(ack == 1) {
    		    fromServer.readByte();
    		}
    	}
    	
    	endTime = System.currentTimeMillis();
    	long elapsedTime = endTime - startTime;
    	double throughput = ((double)total/(double)elapsedTime)*(1000/(double)m);
    	
        System.out.println("Messages Sent: " + msgCount);
        System.out.println("Bytes Sent: " + total);
        System.out.println("Total Transmit Time: " + elapsedTime + " milliseconds");
    	System.out.println("Throughput: " + throughput);
    	sock.close();
    }
    
    public static void UDPTimer(String host, int port, int size, int ack) throws Exception
    {
        int count = g/4096;
        int total = 0;
        int msgCount = 0;
        
        byte[] ackType = new byte[1];
        byte[] totalSize = ByteBuffer.allocate(4).putInt(count).array();
        if(ack == 0) {
           ackType[0] = 0; //telling server pure streaming
        } else if(ack == 1) {
            ackType[0] = 1; //telling server stop-and-wait
        }
        byte[] bytes = new byte[size];
        byte[] receiveAck = new byte[1];
        DatagramSocket sock = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(host);
        
        DatagramPacket sendAck = new DatagramPacket(ackType, ackType.length, IPAddress, port);
        DatagramPacket sendSize = new DatagramPacket(totalSize, totalSize.length, IPAddress, port);
        DatagramPacket receivePacket = new DatagramPacket(receiveAck, receiveAck.length);
        
        sock.send(sendAck);
        sock.receive(receivePacket);
        sock.send(sendSize);
        sock.receive(receivePacket);
        DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, IPAddress, port);
        
        
        long endTime = 0L;
        long startTime = System.currentTimeMillis();
        
        while(count > 0) {
            sock.send(sendPacket);
            count -= bytes.length;
            total += bytes.length;
            msgCount++;
            if(ack == 1) {
                sock.receive(receivePacket);
            }
            
        }
        
        endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        double throughput = ((double)total/(double)elapsedTime)*(1000/(double)m);
        
        System.out.println("Messages Sent: " + msgCount);
        System.out.println("Bytes Sent: " + total);
        System.out.println("Total Transmit Time: " + elapsedTime + " milliseconds");
        System.out.println("Throughput: " + throughput);
        sock.close();
    }
    
    public static void main(String[] args)
    {
        if(args.length != 5) {
            System.err.println("Invalid Number of Arguments.");
            System.exit(1);
        }
        
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String transport = args[2]; //TCP or UDP
        String ack = args[3]; //pure streaming or stop-and-wait
        int msgSize = Integer.parseInt(args[4]); //1 byte to 65,536 bytes
        
        try {
            if(transport.equals("TCP")) {
                if(ack.equals("streaming")) {
                		TCPTimer(host, port, msgSize, 0);
                } else if(ack.equals("stop-and-wait")) {
                        TCPTimer(host, port, msgSize, 1);
                } else {
                    System.err.println("Invalid Acknowledgement Protocol (streaming or stop-and-wait)");
                    System.exit(1);
                }
            } else if(transport.equals("UDP")) {
                if(ack.equals("streaming")) {
                    UDPTimer(host, port, msgSize, 0);
                } else if(ack.equals("stop-and-wait")) {
                    UDPTimer(host, port, msgSize, 1);
                } else {
                    System.err.println("Invalid Acknowledgement Protocol (streaming or stop-and-wait)");
                    System.exit(1);
                }
            } else {
                System.err.println("Invalid Transport Protocol (TCP or UDP)");
                System.exit(1);
            }
        } catch(Exception e) {
            System.err.println(e);
            System.exit(1);
        }
    }

}
