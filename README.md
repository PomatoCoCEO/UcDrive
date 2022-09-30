# UcDrive

### 1st Project for the Distributed Systems course, in the 3rd year of the Bachelor's degree in Informatics Engineering from the University of Coimbra

FTP thread-based server with redundancy, implemented in Java. The architecture includes a primary and a secondary server that exchange heartbeats and files through UDP.
The users register and type their commands, which are passed via TCP.
