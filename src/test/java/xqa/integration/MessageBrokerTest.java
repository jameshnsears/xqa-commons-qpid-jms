package xqa.integration;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TemporaryQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jameshnsears.configuration.ConfigurationAccessor;
import com.github.jameshnsears.configuration.ConfigurationParameterResolver;
import com.github.jameshnsears.docker.DockerClient;

import xqa.commons.qpid.jms.MessageBroker;
import xqa.commons.qpid.jms.MessageMakerHelper;

@ExtendWith(ConfigurationParameterResolver.class)
public class MessageBrokerTest {
    private static final Logger logger = LoggerFactory.getLogger(MessageBrokerTest.class);
    private DockerClient dockerClient;
    private MessageBroker messageBroker;

    @BeforeEach
    public void startMessageBroker(final ConfigurationAccessor configurationAccessor)
            throws IOException, InterruptedException, MessageBroker.MessageBrokerException {
        dockerClient = new DockerClient();
        dockerClient.pull(configurationAccessor.images());
        dockerClient.startContainers(configurationAccessor);

        messageBroker
                = new MessageBroker("0.0.0.0", 5672, "admin", "admin", 3);
    }

    @AfterEach
    public void stopMessageBroker(final ConfigurationAccessor configurationAccessor) throws IOException, JMSException {
        messageBroker.close();

        dockerClient.rmContainers(configurationAccessor);
    }

    @Test
    public void unableToConnectToMessageBroker() {
        assertThrows(
                MessageBroker.MessageBrokerException.class,
                () -> new MessageBroker("0.0.0.0", 1234, "admin", "admin", 3));
    }

    @Test
    public void sendMessage() {
        try {
            final Message message = MessageMakerHelper.createMessage(messageBroker.getSession(),
                    messageBroker.getSession().createQueue(
                            "xqa.test.destination-00"),
                    UUID.randomUUID().toString(),
                    "body-00");

            messageBroker.sendMessage(message);
        } catch (MessageBroker.MessageBrokerException | JMSException messageBrokerException) {
            Assertions.fail(messageBrokerException.getMessage());
        }
    }

    @Test
    public void createMessageWithSubject() throws JMSException {
        final Message message = MessageMakerHelper.createMessage(messageBroker.getSession(),
                messageBroker.getSession().createQueue(
                        "xqa.test.destination-01"),
                        UUID.randomUUID().toString(),
                "subject-01",
                "body-01");

        Assertions.assertEquals("subject-01", message.getJMSType());
    }

    @Test
    public void sendMessageWithReplyTo() throws JMSException, MessageBroker.MessageBrokerException {
        final Message message = MessageMakerHelper.createMessage(messageBroker.getSession(),
                messageBroker.getSession().createQueue(
                        "xqa.test.destination-02"), messageBroker.createTemporaryQueue(),
                UUID.randomUUID().toString(), "body-02");

        messageBroker.sendMessage(message);

        Assertions.assertTrue(message.getJMSReplyTo().toString().contains("temp-queue://"));
    }

    @Test
    public void receiveMessage() throws JMSException, MessageBroker.MessageBrokerException {
        final TemporaryQueue replyTo = messageBroker.createTemporaryQueue();

        final String correlationId = UUID.randomUUID().toString();

        messageBroker.sendMessage(
                MessageMakerHelper.createMessage(messageBroker.getSession(), replyTo, correlationId, "body-03"));

        final List<Message> messages
                = messageBroker.receiveMessagesTemporaryQueue(replyTo, 2000, 1000);

        Assertions.assertEquals(1, messages.size());
        Assertions.assertEquals(correlationId, messages.get(0).getJMSCorrelationID());
    }
}
