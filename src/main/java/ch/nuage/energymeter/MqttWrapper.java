package ch.nuage.energymeter;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttWrapper {

	private static final String CLIENT_ID = "EnergyMeter";

	private static int QOS = 0; // no check if delivered
	private final String broker;
	private MqttClient client;
	private final MqttConnectOptions connOpts;

	private final String mqtt_host;
	private final String mqtt_password;

	private final int mqtt_port;
	private final String mqtt_topic_general;

	private final String mqtt_user;

	private final MemoryPersistence persistence = new MemoryPersistence();

	public MqttWrapper(Properties prop) {

		mqtt_host = PropUtils.getStringFromProperty(prop, "mqtt_host", "localhost");
		mqtt_port = PropUtils.getIntFromProperty(prop, "mqtt_port", 1883);
		mqtt_user = PropUtils.getStringFromProperty(prop, "mqtt_user", "");
		mqtt_password = PropUtils.getStringFromProperty(prop, "mqtt_password", "");

		mqtt_topic_general = PropUtils.getStringFromProperty(prop, "mqtt_topic_general", "");

		broker = "tcp://" + mqtt_host + ":" + mqtt_port;

		connOpts = new MqttConnectOptions();

		connOpts.setCleanSession(true);

		if (mqtt_user.length() > 0) {
			connOpts.setUserName(mqtt_user);
		}
		if (mqtt_password.length() > 0) {
			connOpts.setPassword(mqtt_password.toCharArray());
		}

	}

	/**
	 * Try to connect to mqtt client
	 *
	 * @return
	 * @throws MqttException
	 * @throws UnknownHostException
	 */
	public boolean connect() throws MqttException, UnknownHostException {

		client = new MqttClient(broker, CLIENT_ID, persistence);
		return publishGeneral("EnergyMeter is starting up " + new Date());
	}

	/**
	 * Publish a "general information"
	 *
	 * @param info
	 * @return
	 */
	public boolean publish(String topic, String info) {

		if ((info == null) || (topic == null) || (topic.length() == 0)) {
			return true;
		}

		// -----

		System.out.println("Publish to " + topic + ": " + info);

		try {
			if (!client.isConnected()) {
				System.out.println("Connecting to client");
				client.connect(connOpts);
			}

			MqttMessage message = new MqttMessage(info.getBytes());
			message.setQos(QOS);
			client.publish(topic, message);

			// client.disconnect();
		} catch (MqttException me) {
			System.out.print("Error sending message: ");
			System.out.print("reason " + me.getReasonCode());
			System.out.print(",msg " + me.getMessage());
			System.out.print(",loc " + me.getLocalizedMessage());
			System.out.print(",cause " + me.getCause());
			System.out.print(",excep " + me);
			me.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Publish a "general information"
	 *
	 * @param info
	 * @return
	 */
	public boolean publishGeneral(String info) {
		return publish(mqtt_topic_general, info);
	}

}
