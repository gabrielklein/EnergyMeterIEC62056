# EnergyMeterIEC62056

This java based project collect data from a IEC 62056 Energy meter and broadcast the information to a MQTT server.

Nothing more, no fancy features.

# Dependencies

This project depends on the following libraries

* jrxtx and j62056 (org.openmuc): Collect data from the energy meter

* exp4j (net.objecthunter): Allow some simple expressions in the config (like (A + B) /3 )

* mqtt-client (mqtt-client): Wrapper to MQTT

# Current status

It's an "over-the-weekend" project - but it should be enough to avoid you spending your week-end on it.

# Compatible hardware

This project was tested with <a href="https://shop.weidmann-elektronik.de/index.php?page=product&info=24">IR Schreib/Lesekopf USB (Optokopf)</a>

This project is probably compatible with other devices.

# Compatibility

This project should be compatible with all environments that support java.

This project works fine on Raspberry. I run it on a Raspberry A.

# Compile this project

Download the project using 
>git clone https://github.com/gabrielklein/EnergyMeterIEC62056.git

or another way.

For now you need to compile this project first.

Just install maven on your computer and run
> mvn package

You should have some important files
> target/EnergyMeter.jar

> target/extra-libs/xp4j-0.4.8.jar

> target/extra-libs/j62056-2.1.0.jar

> target/extra-libs/jrxtx-1.0.0.jar

> target/extra-libs/mqtt-client-0.4.0.jar

These files are all you need.

# Setup a linux to start to collect energy data

It's similar to the next section :)

# Setup a raspberry to start to collect energy data

You need first to install a linux environment on the raspberry

Then install some libraries and java
> sudo apt-get install librxtx-java

> sudo apt-get update & sudo apt-get install openjdk-7-jre-headless librxtx-java

You may be interested to add the user that will run this program to "dialout"
> sudo adduser gabriel dialout

You may need to copy all files in
> /usr/lib/jni

To

> /usr/lib

I suggest to shutdown the HDMI interface to lower a bit the energy usage (should be done at every restart)
> /usr/bin/tvservice -o

Copy on your target computer the jar of this project
> target/EnergyMeter.jar

And all files in
> target/extra-libs/*

First run the part that collect data from your energy meter until it works
> java -cp "j62056-2.1.0.jar:jrxtx-1.0.0.EnergyMeter.jar" org.openmuc.j62056.app.Reader -p /dev/ttyUSB0 -d 250 -v 

You should see a dump of the data that could be collected from your energy meter. If this dump is too slow, you may have issues having "real time" data.

Copy
>j62056.properties.sample

To 
>Your raspberry as j62056.properties

And configure this file to fit your environment.

If you don't know yet how to configure mqtt\_mess_*, just leave if like that.

Create a start.sh that contain
> cd /home/whereyourscriptislocated

> java -cp "exp4j-0.4.8.jar:j62056-2.1.0.jar:jrxtx-1.0.0.jar:mqtt-client-0.4.0.jar:EnergyMeter.jar" ch.nuage.energymeter.Main >/tmp/EnergyMeter.log

Don't forget to make it executable :)
> chmod 755 ./start.sh

And execute the script. You should see the data of your energy meter in /tmp/EnergyMeter.log.
> ./start.sh

If you are interested to start the client when your raspberry start, start the client when the raspberry starts.
> crontab -e

Add this line at the end of this file
>@reboot /home/whereyourscriptislocated/start.sh

Now your module should be pushing data to your mqtt server.

# Listen to MQTT messages

You need to install mosquitto-clients
> apt-get install mosquitto-clients

And start listening message on multiple channels (in this example the channels in the default configuration.)
> mosquitto_sub -h 192.168.1.123 -t /home/energy/meter/info -t /home/energy/meter/total -t /home/energy/meter/volts -t /home/energy/meter/amps -t /home/energy/meter/kws -t /home/energy/meter/powfactors

# Suggestion

Try to configure openHAB2 to listen and process these messages.

