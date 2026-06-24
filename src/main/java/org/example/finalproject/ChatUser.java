package org.example.finalproject;

public class ChatUser {
    private int id;
    private String fullname;
    private String lastMessage;
    private int unreadMessages;
    private boolean pinned;

    public ChatUser(int id, String fullname, String lastMessage, int unreadMessages, boolean pinned) {
        setId(id);
        setFullname(fullname);
        setLastMessage(lastMessage);
        setUnreadMessages(unreadMessages);
        setPinned(pinned);
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

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}
