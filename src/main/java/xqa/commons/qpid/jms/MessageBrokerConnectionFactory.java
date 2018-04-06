package xqa.commons.qpid.jms;


import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import java.util.Hashtable;

class MessageBrokerConnectionFactory {
    static public ConnectionFactory messageBroker(final String messageBrokerHost, final int messageBrokerPort) throws Exception {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("connectionfactory.url.amqp", "amqp://" + messageBrokerHost + ":" + messageBrokerPort);
        env.put("java.naming.factory.initial", "org.apache.qpid.jms.jndi.JmsInitialContextFactory");

        return (ConnectionFactory) new InitialContext(env).lookup("url.amqp");
    }
}
