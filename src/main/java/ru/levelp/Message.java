package ru.levelp;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "Messages")
@Table
public class Message {
    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(optional = false)
    private Client clientName;

    @Column(nullable = false)
    private String message;
}
