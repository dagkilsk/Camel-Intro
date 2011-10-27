package no.dagkilsk.cameltest.basic;

import org.junit.*;

/**
 * Created by IntelliJ IDEA.
 * User: dag
 * Date: 9/30/11
 * Time: 3:45 PM
 */
public class BasicStandaloneExcampleTest {

     @Test()
     public void testSendTenMessages() throws Exception {
         BasicStandaloneExcample basicCamelExample = new BasicStandaloneExcample();
         basicCamelExample.start();
         basicCamelExample.sendTenMessage();
         Thread.sleep(5000);
         basicCamelExample.stop();
     }

}
