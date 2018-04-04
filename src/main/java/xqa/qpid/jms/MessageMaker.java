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
        JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) message;
        AmqpJmsBytesMessageFacade facade = (AmqpJmsBytesMessageFacade) jmsBytesMessage.getFacade();
        return facade.getType();
    }

    public static String getTextFromMessage(Message message) throws JMSException, UnsupportedEncodingException {
        JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) message;
        jmsBytesMessage.reset();
        byte[] byteData;
        byteData = new byte[(int) jmsBytesMessage.getBodyLength()];
        jmsBytesMessage.readBytes(byteData);
        return new String(byteData, "UTF-8");
    }
}
