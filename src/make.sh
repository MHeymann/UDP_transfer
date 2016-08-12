#! /bin/bash

rm -rv ./*.class
rm -rv filereceiver/*.class
rm -rv filesender/*.class
echo javac ShareFile.java
javac ShareFile.java
