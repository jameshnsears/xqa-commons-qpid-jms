package xqa;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import javax.jms.JMSException;

import org.apache.qpid.jms.message.JmsObjectMessage;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.apache.qpid.jms.message.facade.JmsMessageFacade;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsObjectMessageFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import xqa.commons.qpid.jms.MessageMakerHelper;

public class MessageMakerHelperTest {
    @Test
    public void jmsObjectMessage() throws JMSException {
        AmqpJmsObjectMessageFacade amqpJmsObjectMessageFacade = mock(AmqpJmsObjectMessageFacade.class);
        when(amqpJmsObjectMessageFacade.getType()).thenReturn("subject");

        JmsObjectMessage jmsObjectMessage = mock(JmsObjectMessage.class);
        when(jmsObjectMessage.getFacade()).thenReturn(amqpJmsObjectMessageFacade);
        jmsObjectMessage.setJMSType("subject");
        Assertions.assertEquals("subject", MessageMakerHelper.getSubject(jmsObjectMessage));

        Serializable serializable = mock(Serializable.class);
        when(jmsObjectMessage.getObject()).thenReturn(serializable);
        when(serializable.toString()).thenReturn("body");
        Assertions.assertEquals("body", MessageMakerHelper.getBody(jmsObjectMessage));
    }

    @Test
    public void jmsTextMessage() throws JMSException {
        JmsMessageFacade jmsMessageFacade = mock(JmsMessageFacade.class);
        when(jmsMessageFacade.getType()).thenReturn("subject");

        JmsTextMessage jmsTextMessage = mock(JmsTextMessage.class);
        when(jmsTextMessage.getFacade()).thenReturn(jmsMessageFacade);
        jmsTextMessage.setJMSType("subject");

        Assertions.assertEquals("subject", MessageMakerHelper.getSubject(jmsTextMessage));

        when(jmsTextMessage.getText()).thenReturn("body");
        Assertions.assertEquals("body", MessageMakerHelper.getBody(jmsTextMessage));
    }
}
