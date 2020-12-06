#!/bin/sh

Running_Container=$(docker ps -a -q)
echo $Running_Container+ test


if [ ! -z "$Running_Container" ]
then
#echo "containers are running, so stopping them"
docker stop $Running_Container
fi
