package tangiblegame;
public class StopWatch {
	private long startTime;
	private long endTime;
	
	/**
	 * Starts the stopwatch
	 */
	void start(){
		startTime = System.nanoTime();		
	}
	
	/**
	 * Stops the stopwatch
	 */
	void stop(){
		endTime = System.nanoTime();
	}
	
	/**
	 * Gets the elapsed time between the start() and stop() calls.
	 * @return The elapsed time in milliseconds
	 */
	long getElapsedTime(){
		return (long) ((endTime - startTime)/(Math.pow(10,6)));
	}
	
	/**
	 * Formats the elapsed time
	 * @param elapsedTime Elapsed time in milliseconds
	 * @return String with the formatted output
	 */
	static String formatElapsedTime(long elapsedTime){		
		return String.format("Time spent: %3d",elapsedTime%1000);
	}
	
	/**
	 * Gets the elapsed time already formatted
	 * @return String with the formatted output
	 */
	String getStrElapsedTime(){
		long elapsed = (long) ((endTime - startTime)/(Math.pow(10,6)));
		return String.format("Time spent: %d",elapsed);
	}
}
