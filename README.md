# kinemic-example-android

This is an example android application which shows how to interact with the Kinemic Publisher.
This project also provides some template and utility classes which can help you get started with a new android gesture enabled application.
To start a new project based on this, simply clone this project. 

## Structure

The first activity (MainActivity) listens to some publisher events and updates some UI elements in it's response.
The 'Rotate RL' gesture opens the drawing activity. The drawing activity shows you how to use the airmouse events.

## Templates

Subclass the GestureActivity or AdvancedGestureActivity to build a gesture enabled activity. These activities manage 
the connection to the publisher, and receive events from the stream.

### GestureActivity 
This simler class used AndroidBroadcasts to communictate one way. This solution does not need zeromq.

### AdvancedGestureActivty
The advanced template used zeromq directly to communicate with the publisher. It also provides a backchannel to reset the orientation. (IMPORTANT for irmouse usage). The advanced gesture activity also provides some settings (ip for publisher, ...) and can connect to any visible publisher in the current network.
