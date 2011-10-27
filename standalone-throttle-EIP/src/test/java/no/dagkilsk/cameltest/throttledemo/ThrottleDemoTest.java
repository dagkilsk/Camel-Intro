package no.dagkilsk.cameltest.throttledemo;

import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: dag
 * Date: 10/3/11
 * Time: 7:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThrottleDemoTest {

    @Test()
    public void testNonThrottledSmsMessages() throws Exception {

        ThrottleDemo throttleDemo = new ThrottleDemo();
        throttleDemo.setupTheRoutes();

        throttleDemo.start();

        String[] messages = create100Messages();
        Date testStart = new Date();

        //This will put 100 messages on a throttled queue ( 20 messages a second ) and 100 messages on a non-throttled queue.
        throttleDemo.send(messages);

        while ( ! throttleDemo.isAllMessagesReceivedOnEndpoint() ) {
            Thread.sleep( 2 * 1000); //Sleep 2 seconds.
        }

        Date allNonThrottledMessagesReceived = throttleDemo.getAllNonThrottledMessagesIsReceivedOnEndpoint();
        Date allThrottledMessagesReceived = throttleDemo.getAllThrottledMessagesIsReceivedOnEndpoint();

        long durationNonThrottledMessagesInMillis = allNonThrottledMessagesReceived.getTime() - testStart.getTime();
        long durationThrottledMessagesInMillis = allThrottledMessagesReceived.getTime() - testStart.getTime();

        assertTrue("All non-throttled messages should finish processing before all the throttled messages",
                allNonThrottledMessagesReceived.before(allThrottledMessagesReceived));
        assertTrue("Duration of the messages sent on non-throttled queue should be less that 2 seconds.",
                durationNonThrottledMessagesInMillis < 2000 );
        assertTrue("Duration of the messages sent on the throttled queue should be more that 4 seconds.",
                durationThrottledMessagesInMillis > 4000 );

        throttleDemo.stop();

    }


    private String[] create100Messages() {
        String[] messages = new String[100];
        for (int i = 0; i < messages.length; i++) {
            messages[i] = "This is test message nr: [" + (i + 1) + "]";
        }
        return messages;
    }



}
