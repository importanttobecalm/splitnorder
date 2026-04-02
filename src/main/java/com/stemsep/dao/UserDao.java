package com.stemsep.dao;

import com.stemsep.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public void save(User user) {
        getCurrentSession().persist(user);
    }

    public User findById(Long id) {
        return getCurrentSession().get(User.class, id);
    }

    public User findByUsername(String username) {
        List<User> results = getCurrentSession()
                .createQuery("FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public User findByEmail(String email) {
        List<User> results = getCurrentSession()
                .createQuery("FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public void update(User user) {
        getCurrentSession().merge(user);
    }

    public void delete(User user) {
        getCurrentSession().remove(user);
    }
}
