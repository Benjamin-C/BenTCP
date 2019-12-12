package benjamin.BenTCP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A server to interact with TCP clients
 * 
 * @author Benjamin Crall
 *
 */
public class TCPServer {
	
	private volatile InputStream is;
	
	private volatile OutputStream os;

	private TCPSetupStream setupStream;
	private int port;
	
	private Listener listen;
	private Socket cientConn;
	
	public TCPOnDataArrival odr;
	
	private long waitTime;
	
	/**
	 * Sets up a TCP Server. Uses setupStream to pick port
	 * 
	 * @param onDataArrival	an {@link TCPOnDataArrival} interface to do something when data arrives
	 * @param setupStream	a {@link TCPSetupStream} used to initialize the server
	 */
	public TCPServer(TCPOnDataArrival onDataArrival, TCPSetupStream setupStream) {
		this(-1, onDataArrival, setupStream, 1);
	}
	
	/**
	 * Sets up a TCP Server. Uses setupStream to pick port
	 * 
	 * @param onDataArrival	an {@link TCPOnDataArrival} interface to do something when data arrives
	 * @param setupStream	a {@link TCPSetupStream} used to initialize the server
	 * @param wait 			a long delay in ms between checking for new data
	 */
	public TCPServer(TCPOnDataArrival onDataArrival, TCPSetupStream setupStream, long wait) {
		this(-1, onDataArrival, setupStream, wait);
	}
	
	/**
	 * Sets up a TCP Server on a port
	 * 
	 * @param portNumber	an int that sets the port
	 * @param onDataArrival	an {@link TCPOnDataArrival} interface to do something when data arrives
	 * @param setupStream	a {@link TCPSetupStream} used to initialize the server
	 * @param wait 			a long delay in ms between checking for new data
	 */
	public TCPServer(int portNumber, TCPOnDataArrival onDataArrival, TCPSetupStream setupStream, long wait) {
		this.setupStream = setupStream;
		port = portNumber;
		odr = onDataArrival;
		
		waitTime = wait;
		while(cientConn == null) {
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
	    	}
	    	tryToSetupServer();
		}
    	
		try {
			is = cientConn.getInputStream();
			os = cientConn.getOutputStream();
			listen = new Listener(waitTime,  is,  odr);
			setupStream.write("Ready");
			startListening();
			
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    }
	
	/**
	 * Starts listening for incoming data
	 */
	public void startListening() {
		listen.startListening();
	}
	
	/**
	 * Stops listening for incoming data
	 */
	public void stopListening() {
		listen.stopListening();
	}

	/**
	 * Closes the server
	 */
	public void close() {
		listen.stopListening();
		listen.interrupt();
		try {
			cientConn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setupStream.write("Ending");
		setupStream.close();
	}
	
	/**
	 * Converts a {@link String} ip to an int ip. Supports xxx.xxx.xxx.xxx or localhost
	 * 
	 * @param ip 	the {@link String} ip to convert
	 * @return the 	int new ip
	 */
	@SuppressWarnings("unused")
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
	 * Tries to start the TCP server
	 */
	@SuppressWarnings("resource")
	private void tryToSetupServer() {
		try {
			setupStream.write("Trying to start on " + Inet4Address.getLocalHost().toString() + ":" + port);
			cientConn = new ServerSocket(port).accept();
			setupStream.write("Client connectd, ready for data");
		} catch (IOException e) {
			if(e.getMessage().contains("Bind failed")) {
				setupStream.write("You can't use that port");
			} else if(e.getMessage().contains("Address already in use")) {
				setupStream.write("That port is already in use");
			}
			port = -1;
		}
	}
}
