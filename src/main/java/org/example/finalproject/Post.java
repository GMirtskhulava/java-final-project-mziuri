package org.example.finalproject;

import org.example.finalproject.exceptions.ActiveUserNotFoundException;
import org.example.finalproject.exceptions.NoPostContentException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class Post {

    private int ID;
    private int authorID;
    private String content;
    private LocalDateTime createdDate;

    public Post(int ID, int authorID, String content, LocalDateTime createdDate) {
        setID(ID);
        setAuthorID(authorID);
        setContent(content);
        setCreatedDate(createdDate);
    }
    public Post(int authorID, String content, LocalDateTime createdDate) {
        setAuthorID(authorID);
        setContent(content);
        setCreatedDate(createdDate);
    }

    public static Post createUserPost(User user, Post post) throws SQLException {
        if(user == null) throw new ActiveUserNotFoundException("Active user not found");
        if(post == null) throw new NoPostContentException("Post is null");
        if(post.getContent() == null || post.getContent().length() < 3) throw new NoPostContentException("Post length must be minimum 3 characters");

        PreparedStatement preparedStatement = MySQL.connection.prepareStatement("INSERT INTO posts (authorID, content, createdDate) VALUES (?, ?, ?)");
        preparedStatement.setInt(1, user.getID());
        preparedStatement.setString(2, post.getContent());
        preparedStatement.setObject(3, post.getCreatedDate());
        int rows = preparedStatement.executeUpdate();
        if(rows > 0) {
            return post;
//            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
//
//            if(generatedKeys.next()) {
//                return new Post(
//                        generatedKeys.getInt(1),
//                        user.getID(),
//                        post.getContent(),
//                        post.getCreatedDate()
//                );
//            }
        }
        return null;
    }


    public int getID() {
        return ID;
    }
    public void setID(int ID) {
        this.ID = ID;
    }

    public int getAuthorID() {
        return authorID;
    }
    public void setAuthorID(int authorID) {
        this.authorID = authorID;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
