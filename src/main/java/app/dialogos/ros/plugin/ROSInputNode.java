package app.dialogos.ros.plugin;

import com.clt.diamant.*;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.script.exp.types.ListType;
import com.clt.script.exp.values.ListValue;
import com.clt.script.exp.values.StringValue;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ROSInputNode extends Node {

    /** the topic to listen to */
    public static final String TOPIC = "rosTopic";
    /** the variable to write the result into (as a list) */
    private static final String RESULT_VAR = "resultVar";
    private static final String WAIT_FOR_MESSAGE = "waitForMessage";

    public ROSInputNode() {
        addEdge(); // have one port for an outgoing edge
        setProperty(TOPIC, ""); // avoid running into null-pointers later
        setProperty(RESULT_VAR, "");
        setProperty(WAIT_FOR_MESSAGE, Boolean.FALSE);
        addPropertyChangeListener(evt -> {
            if (TOPIC.equals(evt.getPropertyName())) {
                ROSPluginSettings pls = (ROSPluginSettings) this.getPluginSettings(ROSPlugin.class);
                pls.changeSubsTopic(evt.getOldValue().toString(), evt.getNewValue().toString());
            }
        });
    }

    @Override
    public Node execute(WozInterface wozInterface, InputCenter inputCenter, ExecutionLogger executionLogger) {
        ROSPluginRuntime runtime = (ROSPluginRuntime) this.getPluginRuntime(ROSPlugin.class, wozInterface);
        String topic = getProperty(TOPIC).toString();
        // return results as a list
        List<String> messages = new ArrayList<>();
        if (getBooleanProperty(WAIT_FOR_MESSAGE)) {
            // this call will block until a message is received:
            try {
                messages.add(runtime.nodeMain.subscriptionMessageQueues.get(topic).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        runtime.nodeMain.subscriptionMessageQueues.get(topic).drainTo(messages);
        ListValue messageList = new ListValue(messages.stream().map(m -> new StringValue(m)).collect(Collectors.toList()));
        // set variable to result of query
        String varName = getProperty(RESULT_VAR).toString();
        Slot var = getSlot(varName);
        var.setValue(messageList);
        return getEdge(0).getTarget();
    }

    /** get the variable slot from the graph that matches the name */
    private Slot getSlot(String name) {
        List<Slot> slots = getListVariables();
        for (Slot slot : slots) {
            if (name.equals(slot.getName()))
                return slot;
        }
        throw new NodeExecutionException(this, "unable to find list variable with name " + name);
    }

    private List<Slot> getListVariables() {
        List<Slot> slots = this.getGraph().getAllVariables(Graph.LOCAL).
                stream().filter(slot -> slot.getType() instanceof ListType).collect(Collectors.toList());
        return slots;
    }

    @Override
    public JComponent createEditorComponent(Map<String, Object> properties) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JPanel horiz = new JPanel();
        horiz.add(new JLabel("ROS topic"));
        horiz.add(NodePropertiesDialog.createTextField(properties, TOPIC));
        p.add(horiz);
        horiz = new JPanel();
        horiz.add(new JLabel("return list of results to:"));
        horiz.add(NodePropertiesDialog.createComboBox(properties, RESULT_VAR,
                getListVariables())
        );
        p.add(horiz);
        p.add(NodePropertiesDialog.createCheckBox(properties, WAIT_FOR_MESSAGE, "wait for at least one message"));
        return p;
    }

    @Override
    public void writeAttributes(XMLWriter out, IdMap uid_map) {
        super.writeAttributes(out, uid_map);
        Graph.printAtt(out, TOPIC, this.getProperty(TOPIC).toString());
        Graph.printAtt(out, RESULT_VAR, this.getProperty(RESULT_VAR).toString());
        Graph.printAtt(out, WAIT_FOR_MESSAGE, this.getProperty(WAIT_FOR_MESSAGE).toString());
    }

    @Override
    public void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {
        if (name.equals(TOPIC) || name.equals(RESULT_VAR)) {
            setProperty(name, value);
        } else if (name.equals(WAIT_FOR_MESSAGE)) {
            setProperty(name, Boolean.valueOf(value));
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    public void writeVoiceXML(XMLWriter xmlWriter, IdMap idMap) { }

}
