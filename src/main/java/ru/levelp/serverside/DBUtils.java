package ru.levelp.serverside;

import lombok.Data;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

@Data
public class DBUtils {
    private EntityManagerFactory entityManagerFactory;
    private EntityManager manager;

    public DBUtils(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        manager = entityManagerFactory.createEntityManager();
    }

    public void saveMessage(Message message) {
        manager.getTransaction().begin();
        manager.persist(message);
        manager.getTransaction().commit();
    }

    public String showHistory() {
        List<Message> messageList = manager.createQuery("SELECT m from Messages m ORDER BY m.id DESC", Message.class)
                .setMaxResults(10).getResultList();
        StringBuffer history = new StringBuffer();
        history.append("Last 10 messages in chat: " + "\n");
        for (Message message : messageList) {
            history.append(message.getClientName() + ": " + message.getMessage() + "\n");
        }
        history.append("**************************************" + "\n");
        return String.valueOf(history);
    }

    public void closeDBManager(){
        manager.close();
    }
}


