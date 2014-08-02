alerttown
=========

A crime reporting app

This is a app/server based crime reporting app.

The app gathers data from users, and sends to server. The server has a database which gets updated, and the changes relayed to all other users in real time.


Needs the following libraries to compile:

1. google paly services library (comes with SDK)
2. google cluster marker library (https://developers.google.com/maps/documentation/android/utility/marker-clustering)

Further, you will need to setup a database on your server for communications. Sample PHP for calling server is included in the server folder. 


A Desktop companion app with Google Maps v3 (javascript) is included and provided in the desktop folder.
