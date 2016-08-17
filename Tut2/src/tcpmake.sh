#! /bin/bash

rm -v ./ShareTCP.class
rm -rv tcpreceiver/*.class
rm -rv tcpsender/*.class
rm -rv layouts/*.class
echo javac ShareTCP.java
javac ShareTCP.java
