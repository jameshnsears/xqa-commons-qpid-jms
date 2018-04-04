package xqa.qpid.jms;

import org.junit.Test;

public class MessageBrokerTest {
    private String destination = "xqa.test.destination";

    @Test
    public void messageSender() throws Exception {
        MessageBroker messageBroker = new MessageBroker("0.0.0.0", "admin", "admin", 3);

        messageBroker.sendMessage(destination, "body-0");

        messageBroker.createTemporaryQueue();
        messageBroker.sendMessageReplyToTemporaryQueue(destination, "correlationId", "body-1");
        messageBroker.sendMessageTemporaryQueue("body-2");

        messageBroker.receiveMessagesTemporaryQueue(2000);

        messageBroker.close();
    }
}

