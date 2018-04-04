package xqa.qpid.jms;

import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsObjectMessage;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.apache.qpid.jms.message.facade.JmsMessageFacade;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsBytesMessageFacade;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsObjectMessageFacade;

import javax.jms.*;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class MessageMaker {
    public static Message createMessage(Session session,
                                        String destination,
                                        String correlationId,
                                        String body) throws JMSException {
        BytesMessage message = getBytesMessage(session, correlationId, body, session.createQueue(destination));
        return message;
    }

    public static Message createMessage(Session session,
                                        String destination,
                                        TemporaryQueue replyTo,
                                        String correlationId,
                                        String body) throws JMSException {
        BytesMessage message = getBytesMessage(session, correlationId, body, session.createQueue(destination));
        message.setJMSReplyTo(replyTo);
        return message;
    }

    public static Message createMessage(Session session,
                                        TemporaryQueue destination,
                                        String correlationId,
                                        String body) throws JMSException {
        BytesMessage message = getBytesMessage(session, correlationId, body, destination);
        return message;
    }

    private static BytesMessage getBytesMessage(Session session,
                                                String correlationId,
                                                String body,
                                                Destination destination) throws JMSException {
        BytesMessage message = session.createBytesMessage();
        message.setJMSDestination(destination);
        message.setJMSTimestamp(new Date().getTime());
        message.setJMSCorrelationID(correlationId);
        message.writeBytes(body.getBytes());
        return message;
    }

    public static String getSubject(Message message) {
        if (message instanceof JmsBytesMessage) {
            JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) message;
            AmqpJmsBytesMessageFacade facade = (AmqpJmsBytesMessageFacade) jmsBytesMessage.getFacade();
            return facade.getType();
        } else if (message instanceof JmsObjectMessage) {
            JmsObjectMessage jmsObjectMessage = (JmsObjectMessage) message;
            AmqpJmsObjectMessageFacade facade = (AmqpJmsObjectMessageFacade) jmsObjectMessage.getFacade();
            return facade.getType();
        } else {
            JmsTextMessage jmsTextMessage = (JmsTextMessage) message;
            JmsMessageFacade facade = jmsTextMessage.getFacade();
            return facade.getType();
        }
    }

    public static String getTextFromMessage(Message message) throws JMSException, UnsupportedEncodingException {
        if (message instanceof JmsBytesMessage) {
            JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) message;
            jmsBytesMessage.reset();
            byte[] byteData;
            byteData = new byte[(int) jmsBytesMessage.getBodyLength()];
            jmsBytesMessage.readBytes(byteData);
            return new String(byteData, "UTF-8");
        } else if (message instanceof JmsObjectMessage) {
            JmsObjectMessage jmsObjectMessage = (JmsObjectMessage) message;
            Serializable serializable = jmsObjectMessage.getObject();
            return new String(serializable.toString().getBytes(), "UTF-8");
        } else {
            JmsTextMessage jmsTextMessage = (JmsTextMessage) message;
            return new String(jmsTextMessage.getText().getBytes(), "UTF-8");
        }
    }
}
