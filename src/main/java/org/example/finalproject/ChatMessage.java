package org.example.finalproject;

public class ChatMessage {
    private int senderID;
    private String content;
    private boolean isSeen;
    private String createdDate;

    public ChatMessage(int senderID, String content, boolean isSeen, String createdDate) {
        setSenderID(senderID);
        setContent(content);
        setSeen(isSeen);
        setCreatedDate(createdDate);
    }

    public int getSenderID() {
        return senderID;
    }
    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSeen() {
        return isSeen;
    }
    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
