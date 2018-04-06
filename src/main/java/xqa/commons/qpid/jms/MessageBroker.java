package xqa.commons.qpid.jms;

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

    public class MessageBrokerException extends Exception {
        MessageBrokerException(String message) {
            super(message);
        }
    }

    public MessageBroker(
            String messageBrokerHost,
            int messageBrokerPort,
            String messageBrokerUsername,
            String messageBrokerPassword,
            int messageBrokerRetryAttempts) throws MessageBrokerException {
        try {
            ConnectionFactory factory = MessageBrokerConnectionFactory.messageBroker(messageBrokerHost, messageBrokerPort);

            boolean connected = false;
            while (!connected) {
                try {
                    synchronized (this) {
                        connection = factory.createConnection(messageBrokerUsername, messageBrokerPassword);
                    }
                    connected = true;
                } catch (Exception exception) {
                    messageBrokerRetryAttempts--;
                    logger.warn("messageBrokerRetryAttempts=" + messageBrokerRetryAttempts);
                    if (messageBrokerRetryAttempts == 0) {
                        throw exception;
                    }
                    Thread.sleep(2500);
                }
            }

            synchronized (this) {
                connection.start();

                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            }
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            throw new MessageBrokerException(exception.getMessage());
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

    public synchronized List<Message> receiveMessagesTemporaryQueue(TemporaryQueue temporaryQueue, long timeout) throws JMSException, UnsupportedEncodingException {
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
        } finally {
            logger.debug(MessageFormat.format("END; size={0}", messages.size()));
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
