package xqa.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xqa.commons.qpid.jms.MessageBroker;
import xqa.commons.qpid.jms.MessageMaker;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TemporaryQueue;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MessageBrokerTest {
    private MessageBroker messageBroker;

    @Before
    public void before() throws Exception {
        messageBroker = new MessageBroker("0.0.0.0", 5672, "admin", "admin", 3);
    }

    @After
    public void after() throws Exception {
        messageBroker.close();
    }

    @Test
    public void unableToConnectToMessageBroker() {
        assertThrows(MessageBroker.MessageBrokerException.class,
            ()-> new MessageBroker("0.0.0.0", 1234, "admin", "admin", 2));
    }


    @Test
    public void sendMessage() throws JMSException, UnsupportedEncodingException {
        Message message = MessageMaker.createMessage(
                messageBroker.getSession(),
                "xqa.test.destination-00",
                UUID.randomUUID().toString(),
                "body-00");

        messageBroker.sendMessage(message);
    }

    @Test
    public void createMessageWithSubject() throws JMSException {
        Message message = MessageMaker.createMessageWithSubject(
                messageBroker.getSession(),
                "xqa.test.destination-01",
                UUID.randomUUID().toString(),
                "subject-01",
                "body-01");

        assertEquals("subject-01", message.getJMSType());
    }

    @Test
    public void sendMessageWithReplyTo() throws JMSException, UnsupportedEncodingException {
        Message message = MessageMaker.createMessage(
                messageBroker.getSession(),
                "xqa.test.destination-02",
                messageBroker.createTemporaryQueue(),
                UUID.randomUUID().toString(),
                "body-02");

        messageBroker.sendMessage(message);

        assertTrue(message.getJMSReplyTo().toString().contains("temp-queue://"));
    }

    @Test
    public void receiveMessage() throws JMSException, UnsupportedEncodingException {
        TemporaryQueue replyTo = messageBroker.createTemporaryQueue();

        String correlationId = UUID.randomUUID().toString();

        Message message = MessageMaker.createMessage(
                messageBroker.getSession(),
                replyTo,
                correlationId,
                "body-02");

        messageBroker.sendMessage(message);

        List<Message> messages = messageBroker.receiveMessagesTemporaryQueue(replyTo, 2000);

        assertEquals(1, messages.size());
        assertEquals(correlationId, messages.get(0).getJMSCorrelationID());
    }
}

