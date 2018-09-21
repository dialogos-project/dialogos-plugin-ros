package app.dialogos.ros.plugin;

import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;

import java.util.Collection;

/**
 * dangerous naming: this is a ROS NodeMain that handles DialogOS's ROS stuff
 */
public class ROSNodeMain implements NodeMain {

    Collection<String> publishableTopics; // all the topics that we might publish to

    ConnectedNode connectedNode;

    ROSNodeMain(Collection<String> publishableTopics) {
        this.publishableTopics = publishableTopics;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("DialogOS_plugin");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        for (String topic : publishableTopics) {
            // pre-heat all possible publishers (it doesn't seem necessary to keep them around, but in principle I could do that as well)
            connectedNode.newPublisher(topic, std_msgs.String._TYPE);
        }
        this.connectedNode = connectedNode;
    }

    @Override
    public void onShutdown(Node node) {
       // node.shutdown();
    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }
}
