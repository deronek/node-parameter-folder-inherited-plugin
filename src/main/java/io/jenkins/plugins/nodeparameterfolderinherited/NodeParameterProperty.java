package io.jenkins.plugins.nodeparameterfolderinherited;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines a folder property which allows to select nodes from list of all available nodes.
 * This list can be then used by jobs inside that folder.
 */
public class NodeParameterProperty extends AbstractFolderProperty<AbstractFolder<?>> {
    private static final Logger LOGGER = Logger.getLogger(NodeParameterProperty.class.getName());

    private final List<String> allowedNodes;

    @DataBoundConstructor
    public NodeParameterProperty(List<String> allowedNodes) {
        LOGGER.log(Level.FINE, "Created with allowedNodes = " + allowedNodes.toString());
        this.allowedNodes = allowedNodes;
    }

    public List<String> getAllowedNodes() {
        return allowedNodes;
    }

    @Extension
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends AbstractFolderPropertyDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return "Node Parameter";
        }
    }
}
