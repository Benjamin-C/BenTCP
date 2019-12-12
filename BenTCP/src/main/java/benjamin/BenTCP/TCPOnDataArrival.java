package benjamin.BenTCP;

public interface TCPOnDataArrival {

	/**
	 * Called when TCP data arrives
	 * 
	 * @param data an array of byte contaning the data
	 */
	public abstract void onDataArrived(byte[] data);
	
	/**
	 * Default {@link TCPOnDataArrival}. Prints all data to {@link System#out}
	 */
	public static TCPOnDataArrival defaultOnDataArrival = new TCPOnDataArrival() {
		public void onDataArrived(byte[] data) {
			// TODO Auto-generated method stub
			String dataString = "";
			for(int i = 0; i < data.length; i++) {
				dataString = dataString + (char) data[i];
			}
			System.out.println("Recived: " + dataString);
		}
	};
	
}
