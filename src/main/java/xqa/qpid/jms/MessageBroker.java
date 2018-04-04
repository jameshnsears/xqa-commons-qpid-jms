package xqa.qpid.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Vector;

public class MessageBroker {
    private static final Logger logger = LoggerFactory.getLogger(MessageBroker.class);
    private Connection connection;
    private Session session;

    public MessageBroker(
            String messageBrokerHost,
            int messageBrokerPort,
            String messageBrokerUsername,
            String messageBrokerPassword,
            int messageBrokerRetryAttempts) throws Exception {
        ConnectionFactory factory = MessageBrokerConnectionFactory.messageBroker(messageBrokerHost, messageBrokerPort);

        boolean connected = false;
        while (connected == false) {
            try {
                synchronized (this) {
                    connection = factory.createConnection(messageBrokerUsername, messageBrokerPassword);
                }
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

        synchronized (this) {
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        }
    }

    public void sendMessage(Message message) throws JMSException, UnsupportedEncodingException {
        logger.info(MessageLogger.log(MessageLogger.Direction.SEND, message, true));

        MessageProducer messageProducer;
        synchronized (this) {
            messageProducer = session.createProducer(message.getJMSDestination());
        }
        messageProducer.send(message, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
        messageProducer.close();
    }

    public synchronized TemporaryQueue createTemporaryQueue() throws JMSException {
        return session.createTemporaryQueue();
    }

    public synchronized List<Message> receiveMessagesTemporaryQueue(TemporaryQueue temporaryQueue, long timeout) throws JMSException {
        logger.debug(MessageFormat.format("temporaryQueue={0}; timeout={1}", temporaryQueue.toString(), timeout));

        MessageConsumer messageConsumer;
        synchronized (this) {
            messageConsumer = session.createConsumer(temporaryQueue);
        }

        List<Message> messages = new Vector<>();

        try {
            logger.debug("START");

            Message sizeResponse = messageConsumer.receive(timeout);
            while (sizeResponse != null) {
                logger.info(MessageLogger.log(MessageLogger.Direction.RECEIVE, sizeResponse, false));
                messages.add(sizeResponse);
                sizeResponse = messageConsumer.receive(1000);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            logger.debug(MessageFormat.format("END; temporayQueueMessages.size={0}", messages.size()));
            messageConsumer.close();
        }

        return messages;
    }

    public void close() throws Exception {
        synchronized (this) {
            session.close();
            connection.close();
        }
    }

    public synchronized Session getSession() {
        return session;
    }
}
