#!/usr/bin/bash
mkdir -p Client/com/client/config
mkdir -p Client/com/client/Data

PRIMARY="PrimaryServer/com/server"
PRIMARY_RUN="$PRIMARY/primary/runfiles"
PRIMARY_DATA="$PRIMARY/primary/data"

mkdir -p "$PRIMARY_RUN/usr"
mkdir -p "$PRIMARY_DATA"
mkdir -p "$PRIMARY_DATA/teste"
echo -n "127.0.0.1 8000
127.0.0.1 8001" > Client/com/client/config/config
echo 'Example file' > Client/com/client/Data/example_file

echo -n "127.0.0.1
8000
8100
8200
8300" > $PRIMARY_RUN/PConfig
echo -n "127.0.0.1
8001
8101
8201
8301" > $PRIMARY_RUN/SConfig
echo -n "teste
p
teste
/"> "$PRIMARY_RUN/usr/teste"

cp PrimaryServer.jar SecondaryServer.jar
mv PrimaryServer.jar PrimaryServer
mv Client.jar Client

cp -r PrimaryServer SecondaryServer
mv SecondaryServer.jar SecondaryServer
rm SecondaryServer/PrimaryServer.jar 
SECONDARY="SecondaryServer/com/server"
cat "$PRIMARY_RUN/SConfig" > "$SECONDARY/primary/runfiles/PConfig"
cat "$PRIMARY_RUN/PConfig" > "$SECONDARY/primary/runfiles/SConfig"
