package ch.nuage.energymeter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmuc.j62056.DataMessage;
import org.openmuc.j62056.DataSet;

import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

public class DataPublishMessage {

	private class TopicMessage {

		private final String messages[];

		private final String topic;

		public TopicMessage(String topic, String[] messages) {
			this.topic = topic;
			this.messages = messages;
			for (int i = 0; i < messages.length; i++) {
				this.messages[i] = this.messages[i].trim();
			}
		}

		public String[] getMessages() {
			return messages;
		}

		public String getTopic() {
			return topic;
		}

	}

	/**
	 * Calculate
	 *
	 * @param string
	 * @return
	 */
	private static double calculate(String v) {
		ExpressionBuilder e = new ExpressionBuilder(v);
		e = e.function(new Function("sign") {
			@Override
			public double apply(double... arg0) {
				if (arg0.length == 0) {
					return 0;
				}
				return arg0[0] >= 0 ? 1 : -1;
			}
		});
		double result = e.build().evaluate();
		return result;
	}

	private final String mqtt_number_format;
	private final String mqtt_sep;

	private final TopicMessage[] topicMessages;

	public DataPublishMessage(Properties prop) throws IOException {

		List<TopicMessage> topicMessageTemp = new ArrayList<TopicMessage>();

		mqtt_sep = PropUtils.getStringFromProperty(prop, "mqtt_sep", "|");
		mqtt_number_format = PropUtils.getStringFromProperty(prop, "mqtt_number_format", "%.2f");

		for (int i = 0; i < 100; i++) {

			String topic = PropUtils.getStringFromProperty(prop, "mqtt_topic_" + i, "");
			String message = PropUtils.getStringFromProperty(prop, "mqtt_mess_" + i, "");

			String messages[] = message.split(";");

			if ((topic.length() > 0) && (message.length() > 0)) {
				topicMessageTemp.add(new TopicMessage(topic, messages));
			}

		}

		topicMessages = topicMessageTemp.toArray(new TopicMessage[topicMessageTemp.size()]);

	}

	/**
	 * Publish DataMessages on mqttWrapper
	 *
	 * @param m
	 * @param mqttWrapper
	 */
	public void publish(DataMessage m, MqttWrapper mqttWrapper) {

		List<DataSet> ds = m.getDataSets();

		Map<String, String> hm = new HashMap<String, String>();
		for (DataSet d : ds) {
			hm.put(d.getAddress(), d.getValue());
			try {
				hm.put(d.getAddress(), "" + Double.parseDouble(d.getValue()));
			} catch (Throwable t) {
				//
			}
		}
		// ds.get(0).getAddress();
		// ds.get(0).getValue();

		for (TopicMessage topicMessage : topicMessages) {
			String topic = topicMessage.getTopic();
			publish(hm, topic, topicMessage.getMessages(), mqttWrapper);
		}

	}

	/**
	 * Publish and transform a message on topic
	 *
	 * @param m
	 * @param topic
	 * @param message
	 * @param mqttWrapper
	 */
	private void publish(Map<String, String> m, String topic, String[] messages, MqttWrapper mqttWrapper) {

		if (messages.length == 0) {
			System.out.println("No message to send on " + topic);
			return;
		}

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < messages.length; i++) {

			String message = messages[i];

			// IPV4_PATTERN =
			// "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$"
			if (message.matches("\\{[0-9\\.]*\\}") || message.matches("\\{+\\.+\\}")) {
				// It's simply an entry like {18.2.5}
				String s = m.get(message.substring(1, message.length() - 1));
				if (s != null) {
					try {
						s = String.format(mqtt_number_format, Double.parseDouble(s));
						message = s;
					} catch (Throwable t) {
						System.out.println("It's not a double: " + s);
						t.printStackTrace();
					}
				} else {
					message = "Not found: " + message;
				}
			} else {
				// Ugly implementation :)

				String m2 = message;
				try {
					for (String key : m.keySet()) {
						if (m2.indexOf("{" + key + "}") >= 0) {
							m2 = m2.replace("{" + key + "}", m.get(key));
						}
					}
					m2 = String.format(mqtt_number_format, calculate(m2));
				} catch (Throwable t) {
					System.out.println("Cannot parse: " + message);
					t.printStackTrace();
				}
				if (m2 != null) {
					message = m2;
				} else {
					message = "Problems with: " + message;
				}
			}

			sb.append(message);

			if (i < (messages.length - 1)) {
				sb.append(mqtt_sep);
			}
		}

		mqttWrapper.publish(topic, sb.toString());

	}

}
