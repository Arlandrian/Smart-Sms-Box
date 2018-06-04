package com.example.alo_i.smartsmsbox;


import java.sql.Date;

public class Person {
    private String number;
    private String name;
    private String lastMessage;
    private Date lastMessageDate;

    public Person(String name, String lastMessage, Date lastMessageDate) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
    }

    public Person(String number, String name, String lastMessage, Date lastMessageDate) {
        this.number = number;
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Person clone(){
        return new Person(this.number,this.name,this.lastMessage,this.lastMessageDate);
    }
}
