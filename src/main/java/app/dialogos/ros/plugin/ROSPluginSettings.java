package app.dialogos.ros.plugin;

import com.clt.dialogos.plugin.PluginRuntime;
import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.IdMap;
import com.clt.diamant.graph.Graph;
import com.clt.properties.DefaultStringProperty;
import com.clt.properties.Property;
import com.clt.properties.PropertySet;
import com.clt.properties.StringProperty;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class ROSPluginSettings extends PluginSettings {

    StringProperty rosMasterURI = new DefaultStringProperty("ROS_MASTER_URI", "ROS_MASTER_URI", "URI und which ROScore can be reached", "http://127.0.0.1:11311");
    StringProperty rosIP = new DefaultStringProperty("ROS_IP", "ROS_IP","IP under which this node can be reached by other nodes", "127.0.0.1");

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

    @Override
    public void writeAttributes(XMLWriter xmlWriter, IdMap idMap) {
        Graph.printAtt(xmlWriter, rosMasterURI.getID(), rosMasterURI.getValue());
        Graph.printAtt(xmlWriter, rosIP.getID(), rosIP.getValue());
    }

    @Override
    protected void readAttribute(XMLReader xmlReader, String name, String value, IdMap idMap) {
         if (name.equals(rosMasterURI.getID())) {
            rosMasterURI.setValue(value);
        } else if (name.equals(rosIP.getID())) {
            rosIP.setValue(value);
        }
    }

    @Override
    public JComponent createEditor() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.add(new PropertySet<Property<?>>(this.rosMasterURI, this.rosIP).createPropertyPanel(false), BorderLayout.NORTH);
        return p;
    }

    @Override
    protected PluginRuntime createRuntime(Component component) {
        PluginRuntime pr = new ROSPluginRuntime(this.rosMasterURI.getValue(),
                                                this.rosIP.getValue(),
                Collections.unmodifiableSet(publishableTopics.elementSet()),
                Collections.unmodifiableSet(subscribedTopics.elementSet()));
        return pr;
    }
}
