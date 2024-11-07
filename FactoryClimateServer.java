import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FactoryClimateServer implements Observable {
	static int NUM_SENSORS = 4;
	static int sensorPortNumber;
	static int observerPortNumber;
	static PrintWriter[] toSensors = new PrintWriter[NUM_SENSORS];
	static DataInputStream[] fromSensors = new DataInputStream[NUM_SENSORS];
	static double[] currentConditions = new double[NUM_SENSORS];
	static List<ObjectOutputStream> observerStreams = new ArrayList<>();
	static int sensorCount = 0;
	static int timerRunCount = 0;

	static class SensorConnectThread extends Thread {
		Socket sensorSocket;
		public SensorConnectThread (Socket socket) {
			super("SensorServerThread");
			sensorSocket = socket;
			sensorCount++;
		}
		public void run() {
			try {
				var outText = new PrintWriter(sensorSocket.getOutputStream(), true);
				var inData = new DataInputStream(sensorSocket.getInputStream());
				outText.println("ID?");
				int index = inData.readInt();
				System.out.println("SensorServerThread " + index);
				if(index < Observable.TEMP_ZONE1 || index > Observable.HUMIDITY_ZONE2)
					System.out.println("Error Sensor index out of range\n"
							+"Restart the whole simulation");
				toSensors[index] = outText;
				fromSensors[index] = inData;
			} catch (IOException e) {
				System.err.println("Could not listen on port " + sensorPortNumber);
				System.exit(-1);
			}	
		}
	}

	static TimerTask task = new TimerTask() {
		String[] alerts = 
			{"ALERT: Temp Sensor 0 disconnected "
			+ "USE manual control for HVAC system 0", 
			"ALERT: Temp Sensor 1 disconnected "
			+ "USE manual control for HVAC system 0",
			"ALERT: Humidity Sensor 2 disconnected "
			+ "USE manual control for HVAC system 1", 
			"ALERT: Humidity Sensor 3 disconnected "
			+ "USE manual control for HVAC system 1"
			};
		@Override
		public void run() {
			timerRunCount++;
			if(timerRunCount > 20) {
				timerRunCount = 0;
				for(int i = 0; i < NUM_SENSORS; i++) {
					toSensors[i].println("SETNORM");
					try {
						currentConditions[i] = fromSensors[i].readDouble();
					} catch (IOException e) {
						System.out.println(alerts[i]);
					}
				}
			} else {
				for(int i = 0; i < NUM_SENSORS; i++) {
					toSensors[i].println("VAL?");
					try {
						currentConditions[i] = fromSensors[i].readDouble();
					} catch (IOException e) {
						System.out.println(alerts[i]);
					}
				}
			}
			System.out.println("TimerTask has " + Arrays.toString(currentConditions));
			Iterator<ObjectOutputStream> iter = observerStreams.iterator();
			while(iter.hasNext()) {
				try {
					// without this copy trick, Java does not re-serialize
					// the array--I only found one comment about static
					// variables not being re-serialized online
					double[] copy = currentConditions.clone();
					iter.next().writeObject(copy);
				} catch(IOException e) {
					System.out.println("Removing HVAC stream");
					iter.remove();
				}
			}
		}
	};

	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Usage is: java FactoryClimateServer <sensor port number> <observer port number");
			System.exit(1);
		}

		int sensorPortNumber = Integer.parseInt(args[0]);
		int observerPortNumber  = Integer.parseInt(args[1]);

		// connect sensors
		try (ServerSocket sensorServerSocket = new ServerSocket(sensorPortNumber)) {
			while (sensorCount < NUM_SENSORS) {
				System.out.println("Listen for a sensor " + sensorCount);
				new SensorConnectThread(sensorServerSocket.accept()).start();
				System.out.println("Accepted a sensor " + sensorCount);
			}
		} catch (IOException e) {
			System.err.println("Could not listen on port " + sensorPortNumber);
			System.exit(-1);
		}			
		System.out.println("Done collecting sensors");
		try (ServerSocket observerServerSocket = new ServerSocket(observerPortNumber)) {
			Timer utilTimer = new Timer();
			utilTimer.schedule(task, 100, 5000);
			while(true) {
				Socket observerSocket = observerServerSocket.accept();
				var objectOut = new ObjectOutputStream(observerSocket.getOutputStream());
				observerStreams.add(objectOut);
			}

		} catch (IOException e) {
			System.err.println("ServerTread could not listen on port " + sensorPortNumber);
			System.exit(-1);
		}			
	}
}