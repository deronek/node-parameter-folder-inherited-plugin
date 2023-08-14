package io.jenkins.plugins;

import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.mig82.folders.properties.FolderProperties;
import io.jenkins.plugins.nodeparameterfolderinherited.NodeParameterFolderInheritedDefinition;
import io.jenkins.plugins.nodeparameterfolderinherited.NodeParameterProperty;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GetNodeParameterPropertyFromFolderTests {
    private static List<AbstractFolderProperty<?>> getPropertyList() {
        List<AbstractFolderProperty<?>> propertyList = new ArrayList<>();
        FolderProperties<?> folderProperties = new FolderProperties<>();
        propertyList.add(folderProperties);
        return propertyList;
    }

    private Method getNodeParameterPropertyMethod() throws NoSuchMethodException {
        Method method = NodeParameterFolderInheritedDefinition.class.getDeclaredMethod("getNodeParameterPropertyFromFolder", List.class);
        method.setAccessible(true);
        return method;
    }

    @Test
    public void testGetNodeParameterProperty() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<AbstractFolderProperty<?>> propertyList = getPropertyList();
        NodeParameterProperty nodeParameterProperty = new NodeParameterProperty(new ArrayList<>(Arrays.asList("built-in", "node1")));
        propertyList.add(nodeParameterProperty);

        assertEquals(nodeParameterProperty, getNodeParameterPropertyMethod().invoke(null, propertyList));
    }

    @Test
    public void testGetNodeParameterProperty_moreThanOne() {
        List<AbstractFolderProperty<?>> propertyList = getPropertyList();
        NodeParameterProperty nodeParameterProperty = new NodeParameterProperty(new ArrayList<>(Arrays.asList("built-in", "node1")));
        propertyList.add(nodeParameterProperty);

        NodeParameterProperty nodeParameterProperty2 = new NodeParameterProperty(new ArrayList<>(Arrays.asList("built-in", "node2", "node3")));
        propertyList.add(nodeParameterProperty2);

        try {
            getNodeParameterPropertyMethod().invoke(null, propertyList);
            fail("Expected an IllegalStateException to be thrown");
        } catch (InvocationTargetException e) {
            Throwable actualException = e.getCause(); // Unwrap the actual exception thrown
            assertTrue(actualException instanceof IllegalStateException);
            String expectedMessage = "More than one instance of NodeParameterProperty found";
            String actualMessage = actualException.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        } catch (IllegalAccessException | NoSuchMethodException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testGetNodeParameterProperty_noParameter() {
        List<AbstractFolderProperty<?>> propertyList = getPropertyList();

        try {
            getNodeParameterPropertyMethod().invoke(null, propertyList);
            fail("Expected an IllegalStateException to be thrown");
        } catch (InvocationTargetException e) {
            Throwable actualException = e.getCause(); // Unwrap the actual exception thrown
            assertTrue(actualException instanceof IllegalStateException);
            String expectedMessage = "No instance of NodeParameterProperty found";
            String actualMessage = actualException.getMessage();
            assertTrue(actualMessage.contains(expectedMessage));
        } catch (IllegalAccessException | NoSuchMethodException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
}
