package ru.levelp.serverside;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "Messages")
@Table
public class Message {
    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private String message;

    public Message(){}

    public Message(String name, String message){
        this.clientName = name;
        this.message = message;
    }
}
