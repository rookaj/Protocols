import java.net.Socket;

public class Client {

    public static final double g = Math.pow(2, 30);
    public static final double m = Math.pow(2, 20);
    
    public static Socket getSocket(String host, int port) throws Exception
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
    
    public static void main(String[] args)
    {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String transport = args[2];
        int msgSize = Integer.parseInt(args[3]);
        String ack = args[4];

        String response = "";
        if(transport.equals("TCP")) {
            if(ack.equals("streaming")) {
                
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
