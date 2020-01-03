package app.dialogos.ros.plugin;

import com.clt.diamant.*;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.ListType;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.ListValue;
import com.clt.script.exp.values.RealValue;
import com.clt.script.exp.values.StringValue;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ROSInputNode extends Node {

    /** the topic to listen to */
    private static final String TOPIC = "rosTopic";
    /** the variable to write the result into (as a list) */
    private static final String RESULT_VAR = "resultVar";
    private static final String WAIT_FOR_MESSAGE = "waitForMessage";
    private static final String TIMEOUT = "timeout";

    public ROSInputNode() {
        addEdge(); // have one port for an outgoing edge
        setProperty(TOPIC, ""); // avoid running into null-pointers later
        setProperty(RESULT_VAR, "");
        setProperty(WAIT_FOR_MESSAGE, Boolean.TRUE);
        setProperty(TIMEOUT, "");
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
            try {
                String timeoutString = getProperty(TIMEOUT).toString();
                long timeout = -1;
                if (!"".equals(timeoutString) && timeoutString != null) {
                    try {
                        Value v = this.parseExpression(timeoutString).evaluate();
                        if (v instanceof IntValue) {
                            timeout = ((IntValue) v).getInt();
                        } else if (v instanceof RealValue) {
                            timeout = (int) ((RealValue) v).getReal();
                        }
                    } catch (Exception e) {
                        throw new NodeExecutionException(this, "unable to interpret timeout", e);
                    }
                    messages.add(runtime.nodeMain.subscriptionMessageQueues.get(topic).poll(timeout, TimeUnit.MILLISECONDS));
                } else
                    // this call will block until a message is received:
                    messages.add(runtime.nodeMain.subscriptionMessageQueues.get(topic).take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        runtime.nodeMain.subscriptionMessageQueues.get(topic).drainTo(messages);
        ListValue messageList = new ListValue(messages.stream().map(StringValue::new).collect(Collectors.toList()));
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
        return this.getGraph().getAllVariables(Graph.LOCAL).
                stream().filter(slot -> slot.getType() instanceof ListType).collect(Collectors.toList());
    }

    @Override
    public JComponent createEditorComponent(Map<String, Object> properties) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JPanel horiz = new JPanel();
        horiz.add(new JLabel("ROS topic "));
        horiz.add(NodePropertiesDialog.createTextField(properties, TOPIC));
        p.add(horiz);
        horiz = new JPanel();
        horiz.add(new JLabel("return list of results to: "));
        horiz.add(NodePropertiesDialog.createComboBox(properties, RESULT_VAR,
                getListVariables())
        );
        p.add(horiz);
        JCheckBox waitBox = NodePropertiesDialog.createCheckBox(properties, WAIT_FOR_MESSAGE, "wait for at least one message");
        p.add(waitBox);
        final JPanel fhoriz = new JPanel();
        fhoriz.add(new JLabel("wait no longer than: "));
        fhoriz.add(NodePropertiesDialog.createTextField(properties, TIMEOUT));
        fhoriz.add(new JLabel(" milliseconds"));
        waitBox.addItemListener(l -> {
            for (Component c : fhoriz.getComponents()) {
                c.setEnabled(waitBox.isSelected());
            }
        });
        p.add(fhoriz);
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
