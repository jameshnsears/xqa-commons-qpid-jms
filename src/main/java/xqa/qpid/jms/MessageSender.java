package xqa.qpid.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Date;
import java.util.UUID;

public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);
    private Connection connection;
    private Session session;

    public MessageSender(String messageBrokerHost, String messageBrokerUsername, String messageBrokerPassword, int messageBrokerRetryAttempts) throws Exception {
        ConnectionFactory factory = MessageBrokerConnectionFactory.messageBroker(messageBrokerHost);

        boolean connected = false;
        while (connected == false) {
            try {
                connection = factory.createConnection(messageBrokerUsername, messageBrokerPassword);
                connected = true;
            } catch (Exception exception) {
                logger.warn("messageBrokerRetryAttempts=" + messageBrokerRetryAttempts);
                if (messageBrokerRetryAttempts == 0) {
                    throw exception;
                }
                messageBrokerRetryAttempts--;
                Thread.sleep(5000);
            }
        }
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public void sendAuditEvent(String auditDestination, String jsonBody) throws Exception {
        BytesMessage messageSent = sendMessage(
                DestinationType.Queue,
                auditDestination,
                UUID.randomUUID().toString(),
                null,
                null,
                jsonBody,
                DeliveryMode.PERSISTENT);

        logger.debug(MessageLogging.log(MessageLogging.Direction.SEND, messageSent, true));
    }

    public static BytesMessage constructMessage(Session session,
                                                Destination ingest,
                                                String correlationID,
                                                Destination replyTo,
                                                String subject,
                                                String body) throws JMSException {
        BytesMessage message = session.createBytesMessage();
        message.setJMSDestination(ingest);
        message.setJMSCorrelationID(correlationID);
        message.setJMSTimestamp(new Date().getTime());
        message.setJMSReplyTo(replyTo);

        if (subject != null) {
            message.setJMSType(subject);
        }

        if (body != null) {
            message.writeBytes(body.getBytes());
        }

        return message;
    }

    public BytesMessage sendMessage(DestinationType destinationType,
                                    String destinationName,
                                    String correlationID,
                                    Destination replyTo,
                                    String subject,
                                    String body,
                                    int deliveryMode) throws Exception {
        Destination destination;
        if (destinationType == DestinationType.Queue) {
            destination = session.createQueue(destinationName);
        } else {
            destination = session.createTopic(destinationName);
        }
        MessageProducer messageProducer = session.createProducer(destination);

        BytesMessage message = constructMessage(session, destination, correlationID, replyTo, subject, body);

        messageProducer.send(message, deliveryMode, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);

        messageProducer.close();

        return message;
    }


    public void close() throws Exception {
        session.close();
        connection.close();
    }

    public enum DestinationType {
        Queue, Topic
    }
}
