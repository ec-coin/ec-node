package nl.hanze.ec.network;

import junit.framework.TestCase;
import org.junit.Test;

public class ApplicationTest extends TestCase {
    @Test
    public void testRun() {
        String expectedValue = "Hello world!";
        Application app = new Application();
        String actualValue = app.run();
        assertEquals(expectedValue, actualValue);
    }
}