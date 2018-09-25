package app.dialogos.ros.plugin;

import com.clt.dialogos.plugin.PluginRuntime;
import org.ros.node.*;
import org.ros.node.topic.Publisher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

public class ROSPluginRuntime implements PluginRuntime {

    NodeMainExecutor nodeMainExecutor;
    ROSNodeMain nodeMain;

    ROSPluginRuntime(String rosMasterURI, String rosIP, Collection<String> publishableTopics, Collection<String> subscribedTopics) {
        URI masterURI = null;
         try {
            masterURI = new URI(rosMasterURI);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(rosIP, masterURI);
        nodeMain = new ROSNodeMain(publishableTopics, subscribedTopics);
        nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        nodeMainExecutor.execute(nodeMain, nodeConfiguration);
        // wait until connected:
        try {
            while (nodeMain.connectedNode == null) {
                Thread.sleep(30); // enable the system to connect to ROS (FIXME: dirty hack! better check connection status than wait for fixed period!)
            }
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.err.println("plugin runtime has started");
    }

    public void sendMessage(String topic, String message) {
        //System.err.println("sending " + message + " on topic " + topic);
        Publisher<std_msgs.String> myPublisher = nodeMain.connectedNode.newPublisher(topic, std_msgs.String._TYPE);
        std_msgs.String msg = myPublisher.newMessage();
        msg.setData(message);
        myPublisher.publish(msg);
    }

    @Override
    public void dispose() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        nodeMainExecutor.shutdownNodeMain(nodeMain);
    }

}
