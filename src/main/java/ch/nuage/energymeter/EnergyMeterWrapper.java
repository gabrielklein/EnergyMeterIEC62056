package ch.nuage.energymeter;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Properties;

import org.openmuc.j62056.DataMessage;
import org.openmuc.j62056.Iec21Port;

public class EnergyMeterWrapper {

	private final int baud_rate;
	private final int baud_rate_change_delay;
	private final String device_address;
	private Iec21Port iec21Port = null;
	private final String request_start_character;
	private final String serial_port;
	private final int timeout;
	private final boolean verbose;

	public EnergyMeterWrapper(Properties prop) throws IOException {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					if (iec21Port != null) {
						iec21Port.close();
					}
				} catch (Throwable t) {
					//
				}
			}
		});

		serial_port = PropUtils.getStringFromProperty(prop, "j62056_serial_port", "/dev/ttyUSB0");
		baud_rate_change_delay = PropUtils.getIntFromProperty(prop, "j62056_baud_rate_change_delay", 250);
		timeout = PropUtils.getIntFromProperty(prop, "j62056_timeout", 5000);
		baud_rate = PropUtils.getIntFromProperty(prop, "j62056_baud_rate", 300);
		verbose = PropUtils.getBooleanFromProperty(prop, "verbose", false);
		device_address = PropUtils.getStringFromProperty(prop, "j62056_device_address", "");
		request_start_character = PropUtils.getStringFromProperty(prop, "j62056_request_start_character", "/?");

	}

	/**
	 * Connect to the iec 62056 device
	 *
	 * @return
	 * @throws IOException
	 */
	public boolean connect() throws IOException {

		try {
			if (iec21Port != null) {
				iec21Port.close();
				Thread.sleep(1000);
			}
		} catch (Throwable t) {
			//
		}

		iec21Port = new Iec21Port.Builder(serial_port).setBaudRateChangeDelay(baud_rate_change_delay)
				.setTimeout(timeout).setInitialBaudrate(baud_rate).enableVerboseMode(verbose).enableFixedBaudrate(false)
				.setDeviceAddress(device_address).setRequestStartCharacters(request_start_character).buildAndOpen();
		return true;
	}

	/**
	 * REad data message from energy meter wrapper.
	 *
	 * @return
	 * @throws InterruptedIOException
	 * @throws IOException
	 */
	public DataMessage read() throws InterruptedIOException, IOException {

		if (iec21Port.isClosed()) {
			System.out.println("EnergyMeter disconnected, need to reconnect");
			connect();
		}

		return iec21Port.read();
	}

}
