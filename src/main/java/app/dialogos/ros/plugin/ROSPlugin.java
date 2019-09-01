package app.dialogos.ros.plugin;

import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.graph.Node;
import com.clt.gui.Images;

import javax.swing.*;
import java.util.Arrays;

public class ROSPlugin implements com.clt.dialogos.plugin.Plugin {

    @Override
    public void initialize() {
        Node.registerNodeTypes(com.clt.speech.Resources.getResources().createLocalizedString("ScriptNode"),
                Arrays.asList(new Class<?>[] { ROSInputNode.class, ROSOutputNode.class })
        );
    }

    @Override
    public String getId() {
        return "dialogos.plugin.ros";
    }

    @Override
    public String getName() {
        return "ROS Plugin";
    }

    @Override
    public Icon getIcon() {
        return Images.load(this, "Robot-Head-Even.png");
    }

    @Override
    public String getVersion() { return "2.1.3"; }   // DO NOT EDIT - This line is updated automatically by the make-release script.

    @Override
    public PluginSettings createDefaultSettings() {
        return new ROSPluginSettings();
    }
}
