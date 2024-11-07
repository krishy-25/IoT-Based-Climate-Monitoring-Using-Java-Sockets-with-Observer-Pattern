import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Sensor {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java Sensor <type> <initial value> <host name> <port number>");
            System.exit(1);
        }

        int type = Integer.parseInt(args[0]);
        double value = Double.parseDouble(args[1]);
        String hostName = args[2];
        int portNumber = Integer.parseInt(args[3]);

        if (type < Observable.TEMP_ZONE1 || type > Observable.HUMIDITY_ZONE2) {
            System.out.println("Error: Simulation only works with sensor types 0 to 3.");
            System.exit(1);
        }

        try (Socket deviceSocket = new Socket(hostName, portNumber);
             DataOutputStream out = new DataOutputStream(deviceSocket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(deviceSocket.getInputStream()))) {

            String fromServer;

            while ((fromServer = in.readLine()) != null) {
                System.out.print("Sensor " + type + " received: " + fromServer);

                if ("ID?".equals(fromServer)) {
                    out.writeInt(type);
                } else if ("VAL?".equals(fromServer)) {
                    if (Math.random() <= 0.5) {
                        value += 1;
                    } else {
                        value -= 1;
                    }
                    out.writeDouble(value);
                } else if ("SETNORM".equals(fromServer)) {
                    value = (type == Observable.TEMP_ZONE1 || type == Observable.TEMP_ZONE2) ? Observable.NORMAL_TEMP : Observable.NORMAL_HUMIDITY;
                    out.writeDouble(value);
                }

                System.out.println(". Value = " + value);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
