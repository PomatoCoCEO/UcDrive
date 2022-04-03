#!/usr/bin/zsh
mv PrimaryServer/PrimaryServer.jar . 
mv Client/Client.jar . 
rm -r Client PrimaryServer SecondaryServer 
./project_config.sh