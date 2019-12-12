package benjamin.BenTCP;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Listener extends Thread {

	private volatile long waitTime;
	private volatile boolean running;
	
	private InputStream is;
	private TCPOnDataArrival odr;
	
	/**
	 * Creates a {@link List} for TCP data
	 * 
	 * @param delay 	the long delay in ms between checks for data
	 * @param instream 	the {@link InputStream} to write data to
	 * @param onData 	a {@link TCPOnDataArrival} to run when data arrives
	 */
	public Listener(long delay, InputStream instream, TCPOnDataArrival onData) {
		waitTime = delay;
		running = false;
		is = instream;
		odr = onData;
	}
	
	@Override
	public void run() {
		while(running) {
			try {
				if(is.available() > 0) {
					List<Byte> dataIn = new ArrayList<Byte>();
					while(is.available() > 0) {
						dataIn.add((byte) is.read());
					}
					byte[] data = new byte[dataIn.size()];
					for(int i = 0; i < dataIn.size(); i++) {
						data[i] = dataIn.get(i);
					}
					odr.onDataArrived(data);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			synchronized(this) {
		        try {
		            this.wait(waitTime);
		        } catch (InterruptedException e) { }
		    }
		}
	}
	
	@Override
	@Deprecated
	public void start() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Starts the {@link Listener} listening
	 */
	public void startListening() {
		running = true;
		super.start();
	}

	/**
	 * Stops the {@link Listener} listening
	 */
	public void stopListening() {
		running = false;
	}
}
