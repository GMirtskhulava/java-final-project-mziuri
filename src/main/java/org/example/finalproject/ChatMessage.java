package org.example.finalproject;

public class ChatMessage {
    private int senderID;
    private String content;
    private String createdDate;

    public ChatMessage(int senderID, String content, String createdDate) {
        setSenderID(senderID);
        setContent(content);
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

    public String getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
