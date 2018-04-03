package xqa.commons.qpid.jms;

import java.text.MessageFormat;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.codec.digest.DigestUtils;

public class MessageLogger {
    public static String log(final Direction direction, final Message message, final boolean useTextDigest)
            throws JMSException {

        String commonLogString = MessageFormat.format(
                "{0} jmsTimestamp={1}; jmsDestination={2}; jmsCorrelationID={3}; jmsReplyTo={4}; subject={5}; jmsExpiration={6}",
                getDirectionArrow(direction), Long.toString(message.getJMSTimestamp()), message.getJMSDestination(),
                message.getJMSCorrelationID(), message.getJMSReplyTo(), MessageMakerHelper.getSubject(message),
                message.getJMSExpiration());

        final String text = MessageMakerHelper.getBody(message);

        if (useTextDigest) {
            commonLogString = commonLogString
                    .concat(MessageFormat.format("; digest(text)={0}", DigestUtils.sha256Hex(text)));
        } else {
            if (!"".equals(text)) {
                commonLogString = commonLogString.concat(MessageFormat.format("; text={0}", text));
            }
        }

        return commonLogString;
    }

    private static String getDirectionArrow(final Direction direction) {
        if (direction == Direction.SEND) {
            return "<";
        } else {
            return ">";
        }
    }

    public enum Direction {
        SEND, RECEIVE
    }
}
