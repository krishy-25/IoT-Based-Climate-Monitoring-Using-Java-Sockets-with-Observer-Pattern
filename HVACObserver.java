import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class HVACObserver {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java HVACObserver <observer id> <host name> <observer port number>");
            System.exit(1);
        }

        int observerId = Integer.parseInt(args[0]);
        String hostName = args[1];
        int observerPortNumber = Integer.parseInt(args[2]);

        if (observerId < Observable.TEMP_ZONE1 || observerId > Observable.TEMP_ZONE2) {
            System.out.println("Error: Simulation only works with observer IDs 0 and 1.");
            System.exit(1);
        }

        try (Socket deviceSocket = new Socket(hostName, observerPortNumber);
             ObjectInputStream in = new ObjectInputStream(deviceSocket.getInputStream())) {

            while (true) {
                double[] values = (double[]) in.readObject();
                System.out.println("Observer " + observerId + ": " + Arrays.toString(values));

                if (observerId == Observable.TEMP_ZONE1) {
                    if (values[Observable.TEMP_ZONE1] < Observable.NORMAL_TEMP) {
                        System.out.println("Heater on in Zone 1");
                    } else {
                        System.out.println("A/C on in Zone 1");
                    }

                    if (values[Observable.HUMIDITY_ZONE1] < Observable.NORMAL_HUMIDITY) {
                        System.out.println("Humidifier on in Zone 1");
                    } else {
                        System.out.println("Dehumidifier on in Zone 1");
                    }
                } else if (observerId == Observable.TEMP_ZONE2) {
                    if (values[Observable.TEMP_ZONE2] < Observable.NORMAL_TEMP) {
                        System.out.println("Heater on in Zone 2");
                    } else {
                        System.out.println("A/C on in Zone 2");
                    }

                    if (values[Observable.HUMIDITY_ZONE2] < Observable.NORMAL_HUMIDITY) {
                        System.out.println("Humidifier on in Zone 2");
                    } else {
                        System.out.println("Dehumidifier on in Zone 2");
                    }
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
