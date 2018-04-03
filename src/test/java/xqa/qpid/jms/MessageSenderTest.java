package xqa.qpid.jms;

import org.junit.Test;

public class MessageSenderTest {
    private String messageBrokerHost = "0.0.0.0";
    private String messageBrokerUsername = "admin";
    private String messageBrokerPassword = "admin";
    private int messageBrokerRetryAttempts = 3;
    private String auditDestination =   "xqa.db.amqp.insert_event";

    @Test
    public void messageSender() throws Exception {
        MessageSender messageSender = new MessageSender(messageBrokerHost,
                messageBrokerUsername,
                messageBrokerPassword,
                messageBrokerRetryAttempts);

        messageSender.sendAuditEvent(auditDestination, "json body");

        messageSender.close();
    }
}

