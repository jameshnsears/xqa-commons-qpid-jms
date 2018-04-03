package xqa.commons.qpid.jms;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsObjectMessage;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.apache.qpid.jms.message.facade.JmsMessageFacade;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsBytesMessageFacade;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsObjectMessageFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageMakerHelper {
    private static final Logger logger = LoggerFactory.getLogger(MessageMakerHelper.class);

    public static Message createMessage(final Session session,
                                        final Destination destination,
                                        final String correlationId,
                                        final String body)
            throws JMSException {
        return getBytesMessage(session, correlationId, destination, body);
    }

    public static Message createMessage(final Session session,
                                        final Destination destination,
                                        final String correlationId,
                                        final String subject,
                                        final String body) throws JMSException {
        final BytesMessage message = getBytesMessage(session, correlationId, destination, body);
        message.setJMSType(subject);
        return message;
    }

    public static Message createMessage(final Session session,
                                        final Destination destination,
                                        final Destination replyTo,
                                        final String correlationId,
                                        final String body) throws JMSException {
        final BytesMessage message = getBytesMessage(session, correlationId, destination, body);
        message.setJMSReplyTo(replyTo);
        return message;
    }

    private static BytesMessage getBytesMessage(final Session session,
                                                final String correlationId,
                                                final Destination destination,
                                                final String body) throws JMSException {
        final BytesMessage message = session.createBytesMessage();
        message.setJMSDestination(destination);
        message.setJMSTimestamp(new Date().getTime());
        message.setJMSCorrelationID(correlationId);
        message.writeBytes(body.getBytes());
        return message;
    }

    public static String getSubject(final Message message) {
        if (message instanceof JmsBytesMessage) {
            logger.debug("JmsBytesMessage");
            final JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) message;
            final AmqpJmsBytesMessageFacade facade = (AmqpJmsBytesMessageFacade) jmsBytesMessage.getFacade();
            return facade.getType();
        } else if (message instanceof JmsObjectMessage) {
            logger.debug("JmsObjectMessage");
            final JmsObjectMessage jmsObjectMessage = (JmsObjectMessage) message;
            final AmqpJmsObjectMessageFacade facade = (AmqpJmsObjectMessageFacade) jmsObjectMessage.getFacade();
            return facade.getType();
        } else {
            logger.debug("JmsTextMessage");
            final JmsTextMessage jmsTextMessage = (JmsTextMessage) message;
            final JmsMessageFacade facade = jmsTextMessage.getFacade();
            return facade.getType();
        }
    }

    public static String getBody(final Message message) throws JMSException {
        if (message instanceof JmsBytesMessage) {
            logger.debug("JmsBytesMessage");
            final JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) message;
            jmsBytesMessage.reset();
            byte[] byteData;
            byteData = new byte[(int) jmsBytesMessage.getBodyLength()];
            jmsBytesMessage.readBytes(byteData);
            return new String(byteData, StandardCharsets.UTF_8);
        } else if (message instanceof JmsObjectMessage) {
            logger.debug("JmsObjectMessage");
            final JmsObjectMessage jmsObjectMessage = (JmsObjectMessage) message;
            final Serializable serializable = jmsObjectMessage.getObject();
            return new String(serializable.toString().getBytes(), StandardCharsets.UTF_8);
        } else {
            logger.debug("JmsTextMessage");
            final JmsTextMessage jmsTextMessage = (JmsTextMessage) message;
            return new String(jmsTextMessage.getText().getBytes(), StandardCharsets.UTF_8);
        }
    }
}
