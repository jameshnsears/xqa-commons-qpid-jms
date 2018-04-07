package xqa.commons.qpid.jms;

import org.apache.commons.codec.digest.DigestUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

public class MessageLogger {
    public static String log(Direction direction,
                             Message message,
                             boolean useTextDigest) throws JMSException, UnsupportedEncodingException {

        String commonLogString = MessageFormat.format(
                "{0} jmsTimestamp={1}; jmsDestination={2}; jmsCorrelationID={3}; jmsReplyTo={4}; subject={5}; jmsExpiration={6}",
                getDirectionArrow(direction),
                Long.toString(message.getJMSTimestamp()),
                message.getJMSDestination(),
                message.getJMSCorrelationID(),
                message.getJMSReplyTo(),
                MessageMaker.getSubject(message),
                message.getJMSExpiration());

        String text = MessageMaker.getBody(message);

        if (useTextDigest) {
            commonLogString = commonLogString.concat(MessageFormat.format("; digest(text)={0}", DigestUtils.sha256Hex(text)));
        } else {
            if (!text.equals("")) {
                commonLogString = commonLogString.concat(MessageFormat.format("; text={0}", text));
            }
        }

        return commonLogString;
    }

    private static String getDirectionArrow(Direction direction) {
        if (direction == Direction.SEND) {
            return "<";
        }
        return ">";
    }

    public enum Direction {
        SEND, RECEIVE
    }
}
