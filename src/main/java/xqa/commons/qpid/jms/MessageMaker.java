package xqa.commons.qpid.jms;

import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsBytesMessageFacade;

import javax.jms.*;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class MessageMaker {
    public static Message createMessage(Session session,
                                        Destination destination,
                                        String correlationId,
                                        String body) throws JMSException {
        return getBytesMessage(session, correlationId, destination, body);
    }

    public static Message createMessage(Session session,
                                        Destination destination,
                                        String correlationId,
                                        String subject,
                                        String body) throws JMSException {
        BytesMessage message = getBytesMessage(session, correlationId, destination, body);
        message.setJMSType(subject);
        return message;
    }

    public static Message createMessage(Session session,
                                        Destination destination,
                                        Destination replyTo,
                                        String correlationId,
                                        String body) throws JMSException {
        BytesMessage message = getBytesMessage(session, correlationId, destination, body);
        message.setJMSReplyTo(replyTo);
        return message;
    }

    private static BytesMessage getBytesMessage(Session session,
                                                String correlationId,
                                                Destination destination,
                                                String body) throws JMSException {
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

    public static String getBody(Message message) throws JMSException, UnsupportedEncodingException {
        JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) message;
        jmsBytesMessage.reset();
        byte[] byteData;
        byteData = new byte[(int) jmsBytesMessage.getBodyLength()];
        jmsBytesMessage.readBytes(byteData);
        return new String(byteData, "UTF-8");
    }
}
