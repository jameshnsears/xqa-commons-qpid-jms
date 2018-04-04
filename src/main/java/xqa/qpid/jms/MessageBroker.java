package xqa.qpid.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.text.MessageFormat;
import java.util.*;

public class MessageBroker {
    private static final Logger logger = LoggerFactory.getLogger(MessageBroker.class);
    private Connection connection;
    private Session session;
    private Destination temporaryQueue;
    private List<Message> temporayQueueMessages;

    public MessageBroker(String messageBrokerHost, String messageBrokerUsername, String messageBrokerPassword, int messageBrokerRetryAttempts) throws Exception {
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

    public void sendMessage(String messageDestination, String body) throws Exception {
        logger.debug(MessageFormat.format("body={0}", body));

        Destination destination = session.createQueue(messageDestination);

        BytesMessage message = getBytesMessage(body, destination);
        message.setJMSCorrelationID(UUID.randomUUID().toString());

        sendMessage(destination, message);
    }

    public void sendMessageTemporaryQueue(String body) throws Exception {
        logger.debug(MessageFormat.format("body={0}", body));

        BytesMessage message = getBytesMessage(body, temporaryQueue);
        message.setJMSCorrelationID(UUID.randomUUID().toString());

        sendMessage(temporaryQueue, message);
    }

    private void sendMessage(Destination destination, BytesMessage message) throws Exception {
        logger.debug(MessageLogging.log(MessageLogging.Direction.SEND, message, true));

        MessageProducer messageProducer = session.createProducer(destination);
        messageProducer.send(message, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
        messageProducer.close();
    }

    public void sendMessageReplyToTemporaryQueue(String replyToDestination, String correlationId, String body) throws Exception {
        Destination destination = session.createTopic(replyToDestination);

        BytesMessage message = getBytesMessage(body, destination);
        message.setJMSCorrelationID(correlationId);
        message.setJMSReplyTo(temporaryQueue);

        sendMessage(destination, message);
    }

    public void createTemporaryQueue() throws JMSException {
        temporaryQueue = session.createTemporaryQueue();
    }

    public synchronized void receiveMessagesTemporaryQueue(long timeout) throws Exception {
        logger.debug(MessageFormat.format("timeout={0}", timeout));

        MessageConsumer messageConsumer = session.createConsumer(temporaryQueue);

        try {
            logger.debug("receiveMessagesTemporaryQueue.START");
            temporayQueueMessages = new Vector<>();

            Message sizeResponse = messageConsumer.receive(timeout);
            while (sizeResponse != null) {
                logger.debug(MessageLogging.log(MessageLogging.Direction.RECEIVE, sizeResponse, false));
                temporayQueueMessages.add(sizeResponse);
                sizeResponse = messageConsumer.receive(1000);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            logger.debug(MessageFormat.format("receiveMessagesTemporaryQueue.END; temporayQueueMessages.size={0}", temporayQueueMessages.size()));
            messageConsumer.close();
        }
    }

    private BytesMessage getBytesMessage(String body, Destination destination) throws JMSException {
        BytesMessage message = session.createBytesMessage();
        message.setJMSDestination(destination);
        message.setJMSTimestamp(new Date().getTime());
        message.writeBytes(body.getBytes());
        return message;
    }

    public void close() throws Exception {
        session.close();
        connection.close();
    }
}
