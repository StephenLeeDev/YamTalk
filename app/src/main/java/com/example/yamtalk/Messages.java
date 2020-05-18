package com.example.yamtalk;

public class Messages {

    private String message;
    private boolean seen;
    private long time;
    private String type;
    private String sender_uid;
    private String receiver_uid;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender_uid() {
        return sender_uid;
    }

    public void setSender_uid(String sender_uid) {
        this.sender_uid = sender_uid;
    }

    public String getReceiver_uid() {
        return receiver_uid;
    }

    public void setReceiver_uid(String receiver_uid) {
        this.receiver_uid = receiver_uid;
    }

    public Messages(String message, boolean seen, long time, String type, String sender_uid, String receiver_uid) {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
        this.sender_uid = sender_uid;
        this.receiver_uid = receiver_uid;
    }

    public Messages() {

    }

}
