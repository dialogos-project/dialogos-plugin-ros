package app.dialogos.ros.plugin;

import com.clt.dialogos.plugin.PluginRuntime;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.IdMap;
import com.clt.diamant.graph.Graph;
import com.clt.properties.*;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class ROSPluginSettings extends PluginSettings {

    BooleanProperty rosFromEnv = new DefaultBooleanProperty(
            "ROS_FROM_ENVIRONMENT",
            "ROS_FROM_ENVIRONMENT",
            "infer ROS settings from environment variables",
            true
    );
    StringProperty rosMasterURI = new DefaultStringProperty(
            "ROS_MASTER_URI",
            "ROS_MASTER_URI",
            "URI und which ROScore can be reached",
            getenv("ROS_MASTER_URI", "http://127.0.0.1:11311"));
    StringProperty rosIP = new DefaultStringProperty(
            "ROS_IP",
            "ROS_IP",
            "IP under which this node can be reached by other nodes",
            getenv("ROS_IP", "127.0.0.1"));

    transient Multiset<String> publishableTopics = ConcurrentHashMultiset.create(); // publishableTopics are added from a propertyChangeListener in ROSNode
    transient Multiset<String> subscribedTopics = ConcurrentHashMultiset.create(); // publishableTopics are added from a propertyChangeListener in ROSNode

    void changePublTopic(String oldTopic, String newTopic) {
        publishableTopics.remove(oldTopic);
        publishableTopics.add(newTopic);
    }

    void changeSubsTopic(String oldTopic, String newTopic) {
        subscribedTopics.remove(oldTopic);
        subscribedTopics.add(newTopic);
    }

    private static String getenv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public void writeAttributes(XMLWriter xmlWriter, IdMap idMap) {
        Graph.printAtt(xmlWriter, rosFromEnv.getID(), rosFromEnv.getValue());
        Graph.printAtt(xmlWriter, rosMasterURI.getID(), rosMasterURI.getValue());
        Graph.printAtt(xmlWriter, rosIP.getID(), rosIP.getValue());
    }

    @Override
    protected void readAttribute(XMLReader xmlReader, String name, String value, IdMap idMap) {
         if (name.equals(rosMasterURI.getID())) {
             rosMasterURI.setValue(value);
         } else if (name.equals(rosIP.getID())) {
             rosIP.setValue(value);
         } else if (name.equals(rosFromEnv.getID())) {
             rosFromEnv.setValue(Boolean.valueOf(value));
         }
    }

    @Override
    public JComponent createEditor() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        // FIXME: URI/IP should be disabled if FromENV is set. But then I won't be able to use createPropertyPanel
        p.add(new PropertySet<Property<?>>(rosFromEnv, rosMasterURI, rosIP).createPropertyPanel(false), BorderLayout.NORTH);
        return p;
    }

    @Override
    protected PluginRuntime createRuntime(Component component) {
        boolean useEnvironment = rosFromEnv.getValue();
        String masterURI = useEnvironment ? getenv("ROS_MASTER_URI", "http://127.0.0.1:11311") : rosMasterURI.getValue();
        String IP = useEnvironment ? getenv("ROS_IP", "127.0.0.1") : rosIP.getValue();
        PluginRuntime pr = new ROSPluginRuntime(masterURI, IP,
                Collections.unmodifiableSet(publishableTopics.elementSet()),
                Collections.unmodifiableSet(subscribedTopics.elementSet()));
        return pr;
    }
}
