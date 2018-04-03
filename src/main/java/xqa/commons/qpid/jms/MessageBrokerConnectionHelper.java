package xqa.commons.qpid.jms;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

class MessageBrokerConnectionHelper {
    public static ConnectionFactory messageBroker(final String messageBrokerHost, final int messageBrokerPort)
            throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("connectionfactory.url.amqp", "amqp://" + messageBrokerHost + ":" + messageBrokerPort);
        env.put("java.naming.factory.initial", "org.apache.qpid.jms.jndi.JmsInitialContextFactory");

        return (ConnectionFactory) new InitialContext(env).lookup("url.amqp");
    }
}
