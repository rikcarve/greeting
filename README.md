# greeting
Greeting service

## zookeeper and curator
The greeting service uses the curator library to register itself in zookeeper.
This is done in ServiceRegistrarListener.

Zookeper needs to be running on localhost:2181
