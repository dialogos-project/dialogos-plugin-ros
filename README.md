# A plugin to enable DialogOS to send messages into arbitrary ROS topics

This plugin enables to send messages to ROS topics e.g. to control
robot actions.

Plugin settings control `ROS_MASTER_URI` and `ROS_IP`, nodes specify
the topic that they send to and the message to be sent. Messages 
to be sent can be DialogOS script *expressions*, i.e., if you don't 
want to send just a fixed string, you can type an expression. To force
sending fixed strings, try "quotes".
Messages are always typed as `std_msgs/String`.
