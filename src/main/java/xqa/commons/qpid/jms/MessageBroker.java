package xqa.commons.qpid.jms;

import java.text.MessageFormat;
import java.util.List;
import java.util.Vector;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageBroker {
    private static final Logger logger = LoggerFactory.getLogger(MessageBroker.class);
    private Connection connection;
    private Session session;

    public MessageBroker(final String host,
                         final int port,
                         final String username,
                         final String password,
                         final int retryAttempts) throws MessageBrokerException, InterruptedException {
        try {
            final ConnectionFactory factory = MessageBrokerConnectionHelper.messageBroker(host, port);
            int remainingRetryAttempts = retryAttempts;

            boolean connected = false;
            while (!connected) {
                try {
                    synchronized (this) {
                        connection = factory.createConnection(username, password);
                    }
                    connected = true;
                } catch (JMSException jmsException) {
                    remainingRetryAttempts--;
                    logger.warn(String.format("messageBrokerRemainingRetryAttempts=%s", remainingRetryAttempts));
                    if (remainingRetryAttempts == 0) {
                        throw jmsException;
                    }
                    Thread.sleep(2500);
                }
            }

            synchronized (this) {
                connection.start();

                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            }
        } catch (NamingException | JMSException exception) {
            logger.error(exception.getMessage());
            throw new MessageBrokerException(exception);
        }
    }

    public void sendMessage(final Message message) throws JMSException, MessageBrokerException {
        logger.info(MessageLogger.log(MessageLogger.Direction.SEND, message, true));

        try {
            MessageProducer messageProducer;
            synchronized (this) {
                messageProducer = session.createProducer(message.getJMSDestination());
            }
            messageProducer.send(message, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY,
                    Message.DEFAULT_TIME_TO_LIVE);
            messageProducer.close();
        } catch (JMSException jmsException) {
            logger.error(jmsException.getMessage());
            throw new MessageBrokerException(jmsException);
        }
    }

    public synchronized TemporaryQueue createTemporaryQueue() throws JMSException {
        return session.createTemporaryQueue();
    }

    public synchronized List<Message> receiveMessagesTemporaryQueue(final TemporaryQueue temporaryQueue,
                                                                    final long firstTimeout,
                                                                    final long secondaryTimeout)
            throws JMSException {
        logger.debug(MessageFormat.format("temporaryQueue={0}; firstTimeout={1}; secondaryTimeout={2}",
                temporaryQueue.toString(), firstTimeout, secondaryTimeout));

        final List<Message> messages = new Vector<>();
        MessageConsumer messageConsumer = null;

        try {
            logger.debug("START");
            messageConsumer = session.createConsumer(temporaryQueue);

            Message sizeResponse = messageConsumer.receive(firstTimeout);
            while (sizeResponse != null) {
                logger.info(MessageLogger.log(MessageLogger.Direction.RECEIVE, sizeResponse, false));
                messages.add(sizeResponse);
                sizeResponse = messageConsumer.receive(secondaryTimeout);
            }
        } catch (JMSException jmsException) {
            logger.error(jmsException.getMessage());
        } finally {
            logger.debug(MessageFormat.format("END; size={0}", messages.size()));
            if (messageConsumer != null) {
                messageConsumer.close();
            }
        }

        return messages;
    }

    public void close() throws JMSException {
        synchronized (this) {
            session.close();
            connection.close();
        }
    }

    public synchronized Session getSession() {
        return session;
    }

    public class MessageBrokerException extends Exception {
        private static final long serialVersionUID = 7353042691246117463L;

        MessageBrokerException(final Throwable cause) {
            super(cause);
        }
    }
}
