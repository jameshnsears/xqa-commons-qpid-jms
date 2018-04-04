package xqa.qpid.jms;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TemporaryQueue;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageBrokerTest {
    MessageBroker messageBroker;

    @Before
    public void before() throws Exception {
        messageBroker = new MessageBroker("0.0.0.0", 5672, "admin", "admin", 3);
    }

    @After
    public void after() throws Exception {
        messageBroker.close();
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
    public void sendMessageWithReplyTo() throws JMSException, UnsupportedEncodingException {
        Message message = MessageMaker.createMessage(
                messageBroker.getSession(),
                "xqa.test.destination-01",
                messageBroker.createTemporaryQueue(),
                UUID.randomUUID().toString(),
                "body-01");

        messageBroker.sendMessage(message);

        assertTrue(message.getJMSReplyTo().toString().contains("temp-queue://"));
    }

    @Test
    public void receiveMessage() throws JMSException, UnsupportedEncodingException {
        TemporaryQueue replyTo = messageBroker.createTemporaryQueue();

        Message message = MessageMaker.createMessage(
                messageBroker.getSession(),
                replyTo,
                UUID.randomUUID().toString(),
                "body-02");

        messageBroker.sendMessage(message);

        List<Message> messages = messageBroker.receiveMessagesTemporaryQueue(replyTo, 2000);

        assertTrue(messages.size() == 1);
    }
}

