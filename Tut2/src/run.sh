#! /bin/bash

if [ "$1" = "send" ] 
then
java filesender.Sender
else 
java ShareFile 
fi
