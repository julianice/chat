package ru.levelp.serverside;

import lombok.Data;

import javax.persistence.EntityManager;

@Data
public class DBTransaction {
    public DBTransaction(EntityManager manager, Message message) {
        manager.getTransaction().begin();
        manager.persist(message);
        manager.getTransaction().commit();
    }
}
