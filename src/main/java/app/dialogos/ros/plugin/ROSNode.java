package app.dialogos.ros.plugin;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.util.Map;

/**
 * dangerous naming: this is a DialogOS node that performs ROS tasks
 */
public class ROSNode extends Node {

    /** the topic to be written to */
    public static final String TOPIC = "rosTopic";
    /** expression that evaluates to what to write into the topic */
    public static final String MESSAGE = "rosMessageExpression";

    public ROSNode() {
        this.addEdge(); // have one port for an outgoing edge
        this.setProperty(TOPIC, ""); // avoid running into null-pointers later
        this.setProperty(MESSAGE, "");
        this.addPropertyChangeListener(evt -> {
            if (TOPIC.equals(evt.getPropertyName())) {
                ROSPluginSettings pls = (ROSPluginSettings) this.getPluginSettings(ROSPlugin.class);
                pls.changeTopic(evt.getOldValue().toString(), evt.getNewValue().toString());
            }
        });
    }

    @Override
    public Node execute(WozInterface wozInterface, InputCenter inputCenter, ExecutionLogger executionLogger) {
        ROSPluginRuntime runtime = (ROSPluginRuntime) this.getPluginRuntime(ROSPlugin.class, wozInterface);
        runtime.sendMessage(getProperty(TOPIC).toString(), getProperty(MESSAGE).toString());
        return getEdge(0).getTarget();
    }

    @Override
    protected JComponent createEditorComponent(Map<String, Object> properties) {
        JPanel p = new JPanel();
        JPanel horiz = new JPanel();
        horiz.add(new JLabel("ROS topic"));
        horiz.add(NodePropertiesDialog.createTextField(properties, TOPIC));
        p.add(horiz);
        horiz = new JPanel();
        horiz.add(new JLabel("message expression"));
        horiz.add(NodePropertiesDialog.createTextField(properties, MESSAGE));
        p.add(horiz);
        return p;
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {
        super.writeAttributes(out, uid_map);
        Graph.printAtt(out, TOPIC, this.getProperty(TOPIC).toString());
        Graph.printAtt(out, MESSAGE, this.getProperty(MESSAGE).toString());
    }

    @Override protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {
        if (name.equals(TOPIC) || name.equals(MESSAGE)) {
            this.setProperty(name, value);
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    protected void writeVoiceXML(XMLWriter xmlWriter, IdMap idMap) { }
}
