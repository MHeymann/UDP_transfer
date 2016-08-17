#! /bin/bash

if [ "$1" = "send" ] 
then
java tcpsender.Sender
elif [ "$1" = "receive" ] 
then
java tcpreceiver.Receiver
else 
java ShareTCP 
fi
