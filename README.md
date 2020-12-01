# FastWIFIRemoteControl

It was made to replace electronic from RC car toy "LandBuster" and it was working great.
It is not complete solution, only parts.

ESP in intended to control two h-bridges (main motor and some turning wheels motor) and to send ADC reading (it was connected to thermistor becouse main motor overheated).

ESP software creates AP. After connecting with phone to that AP Android application can instantly control ESP. Both phone and ESP uses UDP broadcast so IP addresses are not important.

It have low delays becouse of use AsyncUDP, but still not lowest possible - there are some delays in receiving Android code that can be ommited.

See Arduino code to change: pin numbers and that are they inverting. 

Protect all pins with longer wires against high voltage that can induce (i burned a lot of ESP modules becouse of that). 

My solution was to connect pin to ground with 3.3V zener diode but there may be easier ways.

