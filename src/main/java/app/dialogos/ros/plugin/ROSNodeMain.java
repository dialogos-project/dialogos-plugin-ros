package app.dialogos.ros.plugin;

import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * dangerous naming: this is a ROS NodeMain that handles DialogOS's ROS stuff
 */
public class ROSNodeMain implements NodeMain {

    Collection<String> publishableTopics; // all the publishableTopics that we might publish to
    Collection<String> subscribedTopics; // all the publishableTopics that we might publish to

    Map<String, BlockingQueue<String>>  subscriptionMessageQueues;

    ConnectedNode connectedNode;

    ROSNodeMain(Collection<String> publishableTopics, Collection<String> subscribedTopics) {
        this.publishableTopics = publishableTopics;
        this.subscribedTopics = subscribedTopics;
        subscriptionMessageQueues = new HashMap<>();
        for (String topic : subscribedTopics) {
            subscriptionMessageQueues.put(topic, new ArrayBlockingQueue<>(100));
        }
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
        for (final String topic : subscribedTopics) {
            final Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber(topic, std_msgs.String._TYPE);
            subscriber.addMessageListener(message -> {
                    java.lang.String input = message.getData();
                    subscriptionMessageQueues.get(topic).offer(input);
                }
            );
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
