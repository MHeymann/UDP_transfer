#! /bin/bash

if [ "$1" = "send" ] 
then
java filesender.Sender
elif [ "$1" = "receive" ] 
then
java filereceiver.Receiver
else 
java -Xms512m -Xmx512m ShareFile 
fi
