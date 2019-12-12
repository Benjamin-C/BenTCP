package benjamin.BenTCP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A client to connect to a TCP server
 * @author Benjamin Crall
 *
 */
public class TCPClient {
	
	private volatile InputStream is;
	
	private volatile OutputStream os;
	
	private TCPSetupStream setupStream;
	
	private InetAddress ip;
	private int port;

	private Listener listen;
	private Socket cientConn;
	
	private String myName;
	
	public TCPOnDataArrival odr;
	
	private long waitTime;
	
	/**
	 * Creates a {@link TCPClient}
	 * 
	 * @param onDataArrival a {@link TCPOnDataArrival} to run when data arrives
	 * @param setupStream 	a {@link TCPSetupStream} to help with setup
	 */
	public TCPClient(TCPOnDataArrival onDataArrival, TCPSetupStream setupStream) {
		this("", -1, onDataArrival, setupStream, 1, "");
	}
	
	/**
	 * Creates a {@link TCPClient}
	 * 
	 * @param onDataArrival a {@link TCPOnDataArrival} to run when data arrives
	 * @param setupStream 	a {@link TCPSetupStream} to help with setup
	 * @param name 			a String to name the client
	 */
	public TCPClient(TCPOnDataArrival onDataArrival, TCPSetupStream setupStream, String name) {
		this("", -1, onDataArrival, setupStream, 1, name);
	}
	
	/**
	 * Creates a {@link TCPClient}
	 * 
	 * @param onDataArrival a {@link TCPOnDataArrival} to run when data arrives
	 * @param setupStream 	a {@link TCPSetupStream} to help with setup
	 * @param name 			a String to name the client
	 * @param wait 			a long delay in ms between data checks
	 */
	public TCPClient(TCPOnDataArrival onDataArrival, TCPSetupStream setupStream, long wait) {
		this("", -1, onDataArrival, setupStream, wait, "");
	}
	
	/**
	 * Creates a {@link TCPClient}
	 * 
	 * @param ipAdderss 	a String of the IP address to connect to
	 * @param portNumber 	the int port number to connect to at the destination ip
	 * @param onDataArrival a {@link TCPOnDataArrival} to run when data arrives
	 * @param setupStream 	a {@link TCPSetupStream} to help with setup
	 * @param name 			a String to name the client
	 */
	public TCPClient(String ipAdderss, int portNumber, TCPOnDataArrival onDataArrival, TCPSetupStream setupStream, String name) {
		this(ipAdderss, portNumber, onDataArrival, setupStream, 1, name);
	}
	
	/**
	 * Creates a {@link TCPClient}
	 * 
	 * @param ipAdderss 	a String of the IP address to connect to
	 * @param portNumber 	the int port number to connect to at the destination ip
	 * @param onDataArrival a {@link TCPOnDataArrival} to run when data arrives
	 * @param setupStream 	a {@link TCPSetupStream} to help with setup
	 * @param wait 			a long delay in ms between data checks
	 * @param name 			a String to name the client
	 */
	public TCPClient(String ipAdderss, int portNumber, TCPOnDataArrival onDataArrival, TCPSetupStream setupStream, long wait, String name) {
		this.setupStream = setupStream;
		myName = name;
		waitTime = wait;
		port = portNumber;
		odr = onDataArrival;
		ip = null;
		if(!ipAdderss.equals("")) {
			try {
				ip = InetAddress.getByAddress(ipToInt(ipAdderss));
			} catch (UnknownHostException e) {
			}
		}
		
    	pickIP();
		tryToConnect();
    }
	
	/**
	 * Tries to connect to the TCP server at IP:port
	 */
	public void tryToConnect() {
		setupStream.write("Connecting to " + ip.toString() + ":" + port);
		try {
			cientConn = new Socket(ip, port);
			is = cientConn.getInputStream();
			os = cientConn.getOutputStream();
			setupStream.write("Ready");
			listen = new Listener(waitTime, is, odr);
			startListening();
			
		} catch (IOException e) {
			if(e.getMessage().contains("Connection refused")) {
				setupStream.write("Connection refused. Please try again.");
				boolean moveOn = false;
				while(!moveOn) {
					setupStream.write("Would you like to try a new IP? (y/n)");
					String ans = setupStream.read().toUpperCase();
					if(ans.length() > 0) {
						if(ans.charAt(0) == 'Y') {
							ip = null;
							port = -1;
							pickIP();
							tryToConnect();
							moveOn = true;
						}
						if(ans.charAt(0) == 'N') {
							port = -1;
							pickIP();
							tryToConnect();
							moveOn = true;
						}
					}
				}
			} else {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Starts the {@link Listener
	 */
	public void startListening() {
		listen.startListening();
		if(!myName.equals("")) {
			listen.setName(myName + "Listener");
		}
	}
	
	/**
	 * Stops the Listener
	 */
	public void stopListening() {
		listen.stopListening();
	}
	
	/**
	 * Closes the TCP connection
	 */
	public void close() {
		listen.stopListening();
		try {
			cientConn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Converts a {@link String} ip to an int ip. Supports xxx.xxx.xxx.xxx or localhost
	 * 
	 * @param ip 	the {@link String} ip to convert
	 * @return the 	int new ip
	 */
	private byte[] ipToInt(String ip) {
		setupStream.write(ip);
		if(ip.equals("localhost")) {
			try {
				return Inet4Address.getLocalHost().getAddress();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			byte[] ipout = new byte[4];
			for(int i = 0; i < 4; i++) {
				String substr = "";
				if(ip.indexOf('.') < 0) {
					substr = ip;
					ip = "";
				} else {
					substr = ip.substring(0, ip.indexOf('.'));
					ip = ip.substring(ip.indexOf('.') + 1);
				}
				int data = -1;
				try {
					data = Integer.parseInt(substr);
				} catch(NumberFormatException e) {
					setupStream.write("That's not a number! -> " + substr);
					return null;
				}
				if(data >= 0 && data < 256) {
					ipout[i] = (byte) data;
					setupStream.write(Double.toString(data));
				} else {
					setupStream.write("Please give me a valid number");
					return null;
				}
			}
			if(ip.length() > 0) {
				setupStream.write("Too long!");
				return null;
			}
			return ipout;
		}
		return null;
	}
	
	/**
	 * Gets the {@link OutputStream} from the server
	 * 
	 * @return the {@link OutputStream}
	 */
	public OutputStream getOutputStream() {
		return os;
	}
	
	/**
	 * Converts a byte to a string in hex
	 * 
	 * @param in the byte to convert
	 * @return the {@link String} representation in hex
	 */
	@SuppressWarnings("unused")
	private String toHex(byte in) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(String.format("%02X", in));
	    return sb.toString();
	}
	
	/**
	 * Gets the IP from the {@link TCPSetupStream}
	 */
	public void pickIP() {
		while(ip == null) {
    		setupStream.write("Please input an IP");
    		String in = setupStream.read();
    		try {
				ip = Inet4Address.getByAddress(ipToInt(in));
			} catch (UnknownHostException e) {
				setupStream.write("Please try again");
			}
    	}
    	setupStream.write("Connecting to " + ip.toString());
    	while(port < 0) {
    		setupStream.write("Please input a port number");
    		try {
    			port = Integer.parseInt(setupStream.read());
    			if(port < 0 || port > 65535) {
    				port = -1;
    				setupStream.write("Please provide a valid number");
    			}
    		} catch (NumberFormatException e) {
    			setupStream.write("Please give me a number");
    		}
    		System.out.println("Client.java:210 Trying to use port " + port);
    	}
	}
	
}
