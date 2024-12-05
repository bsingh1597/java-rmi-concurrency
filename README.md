# java-rmi-concurrency

### Compile
javac -d target assignment/adcs/RemoteStringArray.java assignment/adcs/RemoteStringArrayServer.java assignment/adcs/StringArrayClient.java

## Start the rmiregistry from target dir

## Start Server 
java -classpath target assignment.adcs.RemoteStringArrayServer assignment/adcs/server-conf.txt

## Start Client 
java  -classpath target assignment.adcs.StringArrayClient assignment/adcs/client-conf2.txtv
