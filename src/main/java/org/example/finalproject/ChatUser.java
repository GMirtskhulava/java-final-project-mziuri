package org.example.finalproject;

public class ChatUser {
    private int id;
    private String fullname;
    private String lastMessage;

    public ChatUser(int id, String fullname, String lastMessage) {
        setId(id);
        setFullname(fullname);
        setLastMessage(lastMessage);
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }


}
