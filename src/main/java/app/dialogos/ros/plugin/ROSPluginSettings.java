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
import org.ros.node.Node;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Set;

public class ROSPluginSettings extends PluginSettings {

    static final boolean DEFAULT_ROS_FROM_ENV = true;
    BooleanProperty rosFromEnv = new DefaultBooleanProperty(
            "ROS_FROM_ENVIRONMENT", null, null,
            DEFAULT_ROS_FROM_ENV) {
        @Override public String getName() {
            return Resources.getString("ROS_FROM_ENVIRONMENT");
        }
        @Override public String getDescription() {
            return Resources.getString("ROS_FROM_ENVIRONMENT_DESCRIPTION");
        }
    };
    static final String DEFAULT_ROS_MASTER_URI = "http://127.0.0.1:11311";
    StringProperty rosMasterURI = new DefaultStringProperty(
            "ROS_MASTER_URI", null, null,
            DEFAULT_ROS_MASTER_URI) {
        @Override public String getName() {
            return Resources.getString("ROS_MASTER_URI");
        }
        @Override public String getDescription() {
            return Resources.getString("ROS_MASTER_URI_DESCRIPTION");
        }
    };
    static final String DEFAULT_ROS_IP = "127.0.0.1";
    StringProperty rosIP = new DefaultStringProperty(
            "ROS_IP", null, null,
            DEFAULT_ROS_IP) {
        @Override public String getName() {
            return Resources.getString("ROS_IP");
        }
        @Override public String getDescription() {
            return Resources.getString("ROS_IP_DESCRIPTION");
        }
    };

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
        if (rosFromEnv.getValue() != (DEFAULT_ROS_FROM_ENV))
            Graph.printAtt(xmlWriter, rosFromEnv.getID(), rosFromEnv.getValue());
        if (!rosMasterURI.getValue().equals(DEFAULT_ROS_MASTER_URI))
            Graph.printAtt(xmlWriter, rosMasterURI.getID(), rosMasterURI.getValue());
        if (!rosIP.getValue().equals(DEFAULT_ROS_IP))
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

    @Override
    public boolean isRelevantForNodes(Set<Class<? extends com.clt.diamant.graph.Node>> nodeTypes) {
        return nodeTypes.contains(ROSInputNode.class) ||
                nodeTypes.contains(ROSOutputNode.class);
    }

}
