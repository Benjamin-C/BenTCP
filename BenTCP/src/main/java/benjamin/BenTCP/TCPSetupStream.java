package benjamin.BenTCP;

import java.util.Scanner;

public interface TCPSetupStream {
	
	/**
	 * Writes data to the {@link TCPSetupStream}
	 * 
	 * @param str the {@link String} data to write
	 */
	public abstract void write(String str);
	/**
	 * Requests input from the {@link TCPSetupStream}
	 * 
	 * @return the {@link String} that was given
	 */
	public abstract String read();
	/**
	 * Closes the {@link TCPSetupStream}
	 */
	public abstract void close();
	
	/**
	 * Gets the default {@link TCPSetupStream}.<br/>
	 * {@link TCPSetupStream#write(String)} prints to {@link System#out}, {@link TCPSetupStream#read()} reads form scan.nextLine, {@link TCPSetupStream#close()} does nothing
	 */
	public static TCPSetupStream defaultSetupStream(Scanner scan) {
		return new TCPSetupStream() {
			
			public void write(String str) {
				System.out.println(str);
			}
			
			public String read() {
				return scan.nextLine();
			}
			
			public void close() {
			}
		};
	}
	
}
