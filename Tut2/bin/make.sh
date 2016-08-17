#! /bin/bash

rm -rv ./*.class
rm -rv filereceiver/*.class
rm -rv filesender/*.class
rm -rv layouts/*.class
rm -rv packet/*.class
rm -rv parameters/*.class
echo javac ShareFile.java
javac ShareFile.java
