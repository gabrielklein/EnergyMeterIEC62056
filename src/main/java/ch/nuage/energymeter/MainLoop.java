package ch.nuage.energymeter;

import java.util.Properties;

import org.openmuc.j62056.DataMessage;

public class MainLoop {

	private final DataPublishMessage dataPublishMessage;
	private final EnergyMeterWrapper energyMeterWrapper;
	private final MqttWrapper mqttWrapper;

	private final int sleep_fail;
	private final int sleep_loop;

	public MainLoop(Properties prop, EnergyMeterWrapper energyMeterWrapper, MqttWrapper mqttWrapper,
			DataPublishMessage dataPublishMessage) {
		this.energyMeterWrapper = energyMeterWrapper;
		this.mqttWrapper = mqttWrapper;
		this.dataPublishMessage = dataPublishMessage;

		this.sleep_fail = PropUtils.getIntFromProperty(prop, "sleep_fail", 30000);
		this.sleep_loop = PropUtils.getIntFromProperty(prop, "sleep_loop", 30000);
	}

	public void loop() {

		while (true) {

			try {

				// Read message
				DataMessage m = energyMeterWrapper.read();

				// Send message
				dataPublishMessage.publish(m, mqttWrapper);

			} catch (Throwable t) {
				t.printStackTrace();
				try {
					Thread.sleep(sleep_fail);
				} catch (InterruptedException e) {
					//
				}
			}

			try {
				Thread.sleep(sleep_loop);
			} catch (InterruptedException e) {
				//
			}

		}

	}

}
