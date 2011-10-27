package no.dagkilsk.cameltest.throttledemo;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import javax.jms.ConnectionFactory;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: dag
 * Date: 10/3/11
 * Time: 7:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThrottleDemo {

    private CamelContext context;

    private Date allThrottledMessagesIsReceivedOnEndpoint;
    private Date allNonThrottledMessagesIsReceivedOnEndpoint;


    public ThrottleDemo() {
        context = new DefaultCamelContext();
    }


    public void setupTheRoutes() {
        //Define Routes.
        try {
            context.addRoutes(new RouteBuilder() {

                public void configure() {

                    // Adding the jms component.
                    ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
                    context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

                    //Throttled route.
                    from("jms:queue:throttled.queue")
                        .throttle(20).process(new Processor() {

                            public void process(Exchange e) {
                                Message message = e.getIn();
                                System.out.println("Message received from throttled queue:" + message.getBody() );
                                String aMsg = (String)e.getIn().getBody();
                                if ( aMsg.contains("[100]") ) {
                                    allThrottledMessagesIsReceivedOnEndpoint = new Date();
                                }
                            }
                        });


                    from("jms:queue:non-throttled.queue").process(new Processor() {

                        public void process(Exchange e) {
                            Message message = e.getIn();
                            System.out.println("Message received from non-throttled queue: " + message.getBody() );
                            String aMsg = (String)e.getIn().getBody();
                            if ( aMsg.contains("[100]") ) {
                                allNonThrottledMessagesIsReceivedOnEndpoint = new Date();
                            }
                        }
                    });



                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean isAllMessagesReceivedOnEndpoint() {
       return ( allThrottledMessagesIsReceivedOnEndpoint != null && allNonThrottledMessagesIsReceivedOnEndpoint != null );
    }


    public void start() throws Exception {
        context.start();
    }


    public void stop() throws Exception {
        context.stop();
    }


    public void send(String[] messages) {
        ProducerTemplate template = context.createProducerTemplate();
        for (int i = 0; i < messages.length; i++) {
            //Put one message on the throttled queue.
            template.sendBody("jms:queue:throttled.queue",messages[i]);

            //Put one message on non-throttled queue.
            template.sendBody("jms:queue:non-throttled.queue",messages[i]);
        }
    }


    public Date getAllNonThrottledMessagesIsReceivedOnEndpoint() {
        return allNonThrottledMessagesIsReceivedOnEndpoint;
    }

    public Date getAllThrottledMessagesIsReceivedOnEndpoint() {
        return allThrottledMessagesIsReceivedOnEndpoint;
    }
}
