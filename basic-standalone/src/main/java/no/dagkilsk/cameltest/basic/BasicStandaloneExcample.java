package no.dagkilsk.cameltest.basic;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import javax.jms.ConnectionFactory;




/**
 * Created by IntelliJ IDEA.
 * User: dag
 * Date: 9/30/11
 * Time: 11:40 AM
 */
public class BasicStandaloneExcample {

    private CamelContext context;

    String testDirPath = "file:///home/dag/test";

    public BasicStandaloneExcample() {
        context = new DefaultCamelContext();
        try {
            context.addRoutes(new RouteBuilder() {

                public void configure() {

                    from("test-jms:queue:test.queue").to( testDirPath );

                    // set up a listener on the file component
                    from("file:///home/dag/test").process(new Processor() {
                        public void process(Exchange e) {
                            System.out.println("Received exchange: " + e.getIn());
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Adding the jms component.
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        context.addComponent("test-jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

        /*
        The above works with any JMS provider. If we know we are using ActiveMQ we can use an even simpler form using the activeMQComponent() method while specifying the brokerURL used to connect to ActiveMQ
        camelContext.addComponent("activemq", activeMQComponent("vm://localhost?broker.persistent=false"));
        */
    }

    public void start() throws Exception {


        /*
            In normal use, an external system would be firing messages or events directly into Camel through one if its
            Components but we are going to use the ProducerTemplate which is a really easy way for testing your configuration: */

            /*
            Next you must start the camel context. If you are using Spring to configure the camel context
            this is automatically done for you; though if you are using a pure Java approach then you
            just need to call the start() method

            This will start all of the configured routing rules.
            */
            context.start();




    }


    public void sendTenMessage() {
        ProducerTemplate template = context.createProducerTemplate();
        for (int i = 0; i < 10; i++) {
            String message = "Test Message: " + i;
            System.out.println("Trying to send msg: " + message);
            template.sendBody("test-jms:queue:test.queue", message);
        }
    }


    public void stop() throws Exception {
        context.stop();
    }



}
