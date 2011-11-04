package no.dagkilsk.cameltest.throttledemo;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.log4j.Logger;

import javax.jms.ConnectionFactory;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;


/**
 * Created by IntelliJ IDEA.
 * User: dag
 * Date: 11/2/11
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThrottleDemoServlet extends HttpServlet {


    private static Logger logger = Logger.getLogger( ThrottleDemoServlet.class );
    private DefaultCamelContext context;
    private Date allThrottledMessagesIsReceivedOnEndpoint;
    private Date allNonThrottledMessagesIsReceivedOnEndpoint;


    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        logger.info("Got a request: " + req );


        context = new DefaultCamelContext();

        
        
        startCamelContext();

        addRoutes();

        Date start = new Date();
        
        sendMessages();

        //Wait for message processing to finish, but max 10 seconds.
        int count = 0;
        while ( allNonThrottledMessagesIsReceivedOnEndpoint == null || allThrottledMessagesIsReceivedOnEndpoint == null )  {
            if ( count == 10 ) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            count++;
        }
        
        stopCamelContext();

        long nonThrottledProcessTime = getDiff( start, allNonThrottledMessagesIsReceivedOnEndpoint );
        long throttledProcessTime = getDiff( start, allThrottledMessagesIsReceivedOnEndpoint );
                
        //Format a reply.
        String reply = "Non throttled message processing: " + nonThrottledProcessTime + " ms.";
        reply += "\nThrottled message processing: " + throttledProcessTime + " ms.";

        res.setContentType("text");
        PrintWriter printWriter = res.getWriter();
        printWriter.print(reply);
        printWriter.close();




    }

    private long getDiff(Date start, Date end) {
        long diff = end.getTime() - start.getTime();
        return diff;
    }


    private void startCamelContext() {
        try {
            context.start();
            logger.info("DefaultCamelContext started.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void stopCamelContext() {
        try {
            context.stop();
            logger.info("DefaultCamelContext started.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addRoutes() {
        
         try {
            context.addRoutes(new RouteBuilder() {

                public void configure() {

                    // Adding the jms component.
                    ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");

                    /*
                        //Try to work with JBossMQ
                        ConnectionFactory jbossMQconnectionFactory; //TODO must get the jmsconnectionfactory from jbossmq.

                        InitialContext iniCtx = new InitialContext();
                        Object tmp = iniCtx.lookup("java:comp/env/jms/QCF");
                        QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;

                    */



                    context.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

                    //Throttled route.
                    from("jms:queue:throttled.queue")
                        .throttle(20).process(new Processor() {

                            public void process(Exchange e) {
                                Message message = e.getIn();
                                logger.info("Message received from throttled queue:" + message.getBody() );
                                String aMsg = (String)e.getIn().getBody();
                                if ( aMsg.contains("[100]") ) {
                                    allThrottledMessagesIsReceivedOnEndpoint = new Date();
                                    logger.info("Throttled messages finished processing: " + allThrottledMessagesIsReceivedOnEndpoint);
                                }
                            }
                        });


                    from("jms:queue:non-throttled.queue").process(new Processor() {

                        public void process(Exchange e) {
                            Message message = e.getIn();
                            logger.info("Message received from non-throttled queue: " + message.getBody() );
                            String aMsg = (String)e.getIn().getBody();
                            if ( aMsg.contains("[100]") ) {
                                allNonThrottledMessagesIsReceivedOnEndpoint = new Date();
                                logger.info("Non-Throttled messages finished processing: " + allNonThrottledMessagesIsReceivedOnEndpoint);
                            }
                        }
                    });



                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        

    }


    private void sendMessages() {
        ProducerTemplate template = context.createProducerTemplate();
        for (int i = 1; i <= 100; i++) {
            //Put one message on the throttled queue.
            String message = "This is test message nr: [" + i + "]";
            template.sendBody("jms:queue:throttled.queue",message);

            //Put one message on non-throttled queue.
            template.sendBody("jms:queue:non-throttled.queue",message);
        }

    }














}
