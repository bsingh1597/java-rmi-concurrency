#!/bin/bash
if [[ -n $1 ]]; then
    curl -X GET  https://download.oracle.com/java/17/archive/jdk-17.0.5_linux-x64_bin.tar.gz -o jdk-17.0.5_linux-x64_bin.tar.gz
    tar -xf jdk-17.0.5_linux-x64_bin.tar.gz
fi


#compile codes
jdk-17.0.5/bin/javac -d target assignment/adcs/RemoteStringArray.java assignment/adcs/RemoteStringArrayServer.java assignment/adcs/StringArrayClient.java

#start the rmiregistry
cd target
../jdk-17.0.5/bin/rmiregistry &
cd ../

#run code
jdk-17.0.5/bin/java  -classpath target assignment.adcs.RemoteStringArrayServer 2>&1

