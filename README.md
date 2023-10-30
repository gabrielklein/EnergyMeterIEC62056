# EnergyMeterIEC62056

This java based project collect data from a IEC 62056 Energy meter and broadcast the information to a MQTT server.

Nothing more, no fancy features.

# What can be extracted

You can extract many informations from different energy meter without investing a lot of money.

As example

* Total power consumption
* Current power consumption
* Current amps and voltage
* Current KW used
* Power factor

For a budget of about 50$ / 50€

This information can then be sent to a MQTT server with this project.


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

> target/extra-libs/exp4j-0.4.8.jar

> target/extra-libs/j62056-2.2.0.jar

> target/extra-libs/jrxtx-1.0.1.jar

> target/extra-libs/mqtt-client-0.4.0.jar

These files are all you need.

# Setup a linux to start to collect energy data

It's similar to the next section :)

# Tested environment

I use a Raspberry Pi 3 Model B Rev 1.2

> PRETTY_NAME="Raspbian GNU/Linux 11 (bullseye)"

> NAME="Raspbian GNU/Linux"

> VERSION_ID="11"

> VERSION="11 (bullseye)"


# Setup a raspberry to start to collect energy data

You need first to install a linux environment on the raspberry.

Then install some libraries and java
> sudo apt-get install librxtx-java

> sudo apt-get update

> sudo apt-get install openjdk-17-jdk  openjdk-17-jre librxtx-java

You may be interested to add the user that will run this program to "dialout"
> sudo adduser yourname dialout

I suggest to shutdown the HDMI interface to lower a bit the energy usage (should be done at every restart)
> /usr/bin/tvservice -o

Copy on your target computer the jar of this project
> target/EnergyMeter.jar

And all files in
> target/extra-libs/*

Copy
>j62056.properties.sample

To 
>Your raspberry as j62056.properties

And configure this file to fit your environment.

Create a start.sh that contain
> cd /home/whereyourscriptislocated

> java -cp "target/extra-libs/exp4j-0.4.8.jar:target/extra-libs/j62056-2.2.0.jar:target/extra-libs/jrxtx-1.0.1.jar:target/extra-libs/mqtt-client-0.4.0.jar:target/EnergyMeter.jar" ch.nuage.energymeter.Main

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

