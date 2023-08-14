package io.jenkins.plugins.nodeparameterfolderinherited;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterDefinition;
import org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterValue;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Defines a node parameter for jobs that inherits its configuration from the parent folders.
 */
public class NodeParameterFolderInheritedDefinition extends SimpleParameterDefinition {
    private static final Logger LOGGER = Logger.getLogger(NodeParameterFolderInheritedDefinition.class.getName());

    private final String jobName;

    /**
     * Constructs a new instance of NodeParameterFolderInheritedDefinition.
     *
     * @param name    The name of the parameter.
     * @param jobName The name of the job. This parameter will be used to fetch the parent job (expected to be a folder)
     *                and retrieve the NodeParameterProperty from its properties.
     */
    @DataBoundConstructor
    public NodeParameterFolderInheritedDefinition(String name, String jobName) {
        super(name);
        this.jobName = jobName;
        LOGGER.log(Level.FINE, "Created with jobName " + jobName);
    }

    private static NodeParameterProperty getNodeParameterPropertyFromFolder(List<AbstractFolderProperty<?>> list) throws IllegalStateException {
        List<NodeParameterProperty> foundProperties = list.stream()
                .filter(property -> property instanceof NodeParameterProperty)
                .map(property -> (NodeParameterProperty) property)
                .collect(Collectors.toList());

        if (foundProperties.isEmpty()) {
            throw new IllegalStateException("No instance of NodeParameterProperty found");
        } else if (foundProperties.size() > 1) {
            throw new IllegalStateException("More than one instance of NodeParameterProperty found");
        }

        return foundProperties.get(0);
    }

    private static AbstractFolder<?> getParentFolderFromJob(Item item) throws IllegalStateException {
        ItemGroup<? extends Item> itemGroup = item.getParent();
        if (!(itemGroup instanceof AbstractFolder)) {
            throw new IllegalStateException("Parent of Item is not a Folder");
        }
        return (AbstractFolder<?>) itemGroup;
    }

    /**
     * Creates a NodeParameterValue based on the input JSON object.
     * This implementation is directly borrowed from Node Label Parameter plug-in implementation.
     * See <a href="https://github.com/jenkinsci/nodelabelparameter-plugin/">https://github.com/jenkinsci/nodelabelparameter-plugin/</a>. MIT license.
     *
     * @param req The StaplerRequest object.
     * @param jo  The input JSON object.
     * @return A NodeParameterValue instance.
     * @see NodeParameterDefinition#createValue(StaplerRequest, JSONObject)
     */
    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        // as String from UI: {"labels":"built-in","name":"HOSTN"}
        // as JSONArray: {"name":"HOSTN","value":["built-in","host2"]}
        // as String from script: {"name":"HOSTN","value":"built-in"}
        final String name = jo.getString("name");
        // JENKINS-28374 also respect 'labels' to allow rebuilds via rebuild plugin
        final Object joValue = jo.get("value") == null
                ? (jo.get("labels") == null ? jo.get("label") : jo.get("labels"))
                : jo.get("value");

        List<String> nodes = new ArrayList<>();
        if (joValue instanceof String) {
            nodes.add((String) joValue);
        } else if (joValue instanceof JSONArray) {
            JSONArray ja = (JSONArray) joValue;
            for (Object strObj : ja) {
                nodes.add((String) strObj);
            }
        }

        NodeParameterValue value = new NodeParameterValue(name, nodes, new AllNodeEligibility());
        value.setDescription(getDescription());
        return value;
    }

    /**
     * Creates a NodeParameterValue based on the provided String.
     * This implementation is directly borrowed from Node Label Parameter plug-in implementation.
     * See <a href="https://github.com/jenkinsci/nodelabelparameter-plugin/">https://github.com/jenkinsci/nodelabelparameter-plugin/</a>. MIT license.
     *
     * @param value The String value to create the parameter from.
     * @see NodeParameterDefinition#createValue(String)
     */
    @Override
    public ParameterValue createValue(String value) {
        return new NodeParameterValue(getName(), getDescription(), value);
    }

    /**
     * Retrieves the list of allowed nodes for the parameter, falling back to all available nodes if necessary.
     *
     * @return A list of allowed node names.
     */
    @SuppressWarnings("unused")
    public List<String> getAllowedNodesOrAll() {
        List<String> result;
        try {
            Item item = getJobFromName();
            AbstractFolder<?> folder = getParentFolderFromJob(item);
            NodeParameterProperty foundProperty = getNodeParameterPropertyFromFolder(folder.getProperties());

            LOGGER.log(Level.FINE, "Found NodeParameter property for job \"{0}\", folder \"{1}\"", new Object[]{item.getName(), folder.getName()});
            result = foundProperty.getAllowedNodes();
        } catch (IllegalStateException e) {
            LOGGER.log(Level.FINE, "Falling back to all available nodes choice: " + e.getMessage());
            result = NodeParameterDefinition.getSlaveNames();
        }
        return result;
    }

    private Item getJobFromName() throws IllegalStateException {
        Item item = Jenkins.get().getItemByFullName(this.jobName);
        if (item == null) {
            throw new IllegalStateException("Item not found");
        }
        return item;
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends ParameterDefinition.ParameterDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return "Node - Folder Inherited";
        }
    }
}
