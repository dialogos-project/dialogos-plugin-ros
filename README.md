# A plugin to enable DialogOS to send messages into arbitrary ROS topics

This plugin enables to send messages to ROS topics e.g. to control
robot actions.

Plugin settings control `ROS_MASTER_URI` and `ROS_IP`, 
nodes specify the topics they publish/subscribe to. 

## Publishing messages to ROS topics:

Use a `ROSOutputNode` to send (=publish) a message to any ROS topic.

Messages to be sent can be DialogOS script *expressions*, i.e., if you don't 
want to send just a fixed string, you can enter an expression. 
To force sending fixed strings, try "quotes". 

Messages to be sent are always typed as `std_msgs/String`.

## Subscribing to messages on ROS topics:

Use a `ROSInputNode` to receive messages on any ROS topic (subscription).

Multiple messages may accumulate in the subscribed topic and hence the 
node will return a list of messages into a `List` variable. 
You specify what (list) variable to store the results into.
If you want your node to wait for at least one message (since the last call) 
to appear in the topic, you can set the corresponding check mark in the 
node properties.

Messages to be received must be typed `std_msgs/String`.
