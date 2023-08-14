package io.jenkins.plugins;

import io.jenkins.plugins.nodeparameterfolderinherited.NodeParameterProperty;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterDefinition;

import java.util.List;

import static org.junit.Assert.*;

public class NodeParameterPropertyTests {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testCreationAllNodes()
    {
        List<String> allowedNodes = NodeParameterDefinition.getSlaveNames();
        NodeParameterProperty prop = new NodeParameterProperty(allowedNodes);
        assertEquals(allowedNodes, prop.getAllowedNodes());
    }
}
