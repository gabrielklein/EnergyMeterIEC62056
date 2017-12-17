package ch.nuage.energymeter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.openmuc.j62056.DataMessage;

public class Main {

	/**
	 * Try to read and return property file. In case of an error, return null;
	 *
	 * @param args
	 * @return
	 */
	private static Properties getProperties(String[] args) {
		Properties prop = new Properties();
		InputStream input = null;
		try {

			File f = args.length > 0 ? new File(args[0]) : new File("j62056.properties");
			if (!f.exists()) {
				throw new Exception("File that contains properties doesn't exists: " + f);
			}

			System.out.println("Loading from property: " + f);
			input = new FileInputStream(f);
			prop.load(input);

		} catch (Throwable ex) {
			System.out.println("Cannot load property file.");
			ex.printStackTrace();
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (prop.containsKey("serial_port")) {
			System.out.println("Property file is not valid. searial_port is missing");
			return null;
		}

		return prop;

	}

	/**
	 * Main method.
	 *
	 * @param args
	 * @throws IOException
	 * @throws MqttException
	 */
	public static void main(String[] args) throws IOException, MqttException {

		System.out.println("Starting...");

		Properties prop = getProperties(args);
		if (prop == null) {
			return;
		}

		// ----------------------
		// Connect to energy meter

		System.out.println("Initializing communication to energy meter...");
		EnergyMeterWrapper energyMeterWrapper = new EnergyMeterWrapper(prop);
		if (!energyMeterWrapper.connect()) {
			System.out.println("Cannot connect to the device or energy meter");
			return;
		}

		DataMessage dataMessage = energyMeterWrapper.read();
		System.out.println(dataMessage);

		// ----------------------
		// Connect to MQTT
		MqttWrapper mqttWrapper = new MqttWrapper(prop);
		if (!mqttWrapper.connect()) {
			System.out.println("Cannot connect to MQTT server");
			return;
		}

		if (!mqttWrapper.publishGeneral("Manufacturer: " + dataMessage.getManufacturerId())) {
			System.out.println("Cannot connect to MQTT server");
		}

		if (!mqttWrapper.publishGeneral("MeterId: " + dataMessage.getMeterId())) {
			System.out.println("Cannot connect to MQTT server");
		}

		// ----------------------
		// Create dataPublishMessage
		DataPublishMessage dataPublishMessage = new DataPublishMessage(prop);

		// ----------------------
		// Success, start the main loop
		MainLoop mainLoop = new MainLoop(prop, energyMeterWrapper, mqttWrapper, dataPublishMessage);
		mainLoop.loop();

	}

}
