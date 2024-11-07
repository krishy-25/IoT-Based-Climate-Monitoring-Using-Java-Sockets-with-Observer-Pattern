# IoT-Based Climate Monitoring Using Java Sockets

This project is a climate monitoring system using Java Sockets and the Observer Pattern. It simulates a warehouse with two zones, where sensors and HVAC units act as observers, connecting to a central server that collects data and sends updates every five seconds.

- **FactoryClimateServer**: Collects data and distributes temperature and humidity readings.
- **Sensor**: Simulates temperature and humidity readings and responds to server commands.
- **HVACObserver**: Adjusts heating, cooling, humidification, or dehumidification to maintain 70°F and 40% humidity.

- Real time communication between server and clients using Java sockets.
- Observer Pattern for responsive design.
- Secure resource management with Java's try-with-resources.
