package org.broker.orbi.topic.message;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author smantzouratos
 */
public class BrokerTopicMessage implements Serializable {
    
    private String msgType;
    private String msgBody;
    private String msgSubject;
    private Date msgDate;
    private String msgTo;

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public String getMsgSubject() {
        return msgSubject;
    }

    public void setMsgSubject(String msgSubject) {
        this.msgSubject = msgSubject;
    }

    public Date getMsgDate() {
        return msgDate;
    }

    public void setMsgDate(Date msgDate) {
        this.msgDate = msgDate;
    }

    public String getMsgTo() {
        return msgTo;
    }

    public void setMsgTo(String msgTo) {
        this.msgTo = msgTo;
    }

    public BrokerTopicMessage() {
    }

    public BrokerTopicMessage(String msgType, String msgBody, String msgSubject, Date msgDate, String msgTo) {
        this.msgType = msgType;
        this.msgBody = msgBody;
        this.msgSubject = msgSubject;
        this.msgDate = msgDate;
        this.msgTo = msgTo;
    }
    
}
