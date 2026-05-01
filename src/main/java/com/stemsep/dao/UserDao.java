package com.stemsep.dao;

import com.stemsep.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
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
        Session session = getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        Predicate predicateUsername = criteriaBuilder.equal(root.get("username"), username);
        criteriaQuery.select(root).where(predicateUsername);

        Query<User> query = session.createQuery(criteriaQuery);
        List<User> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public User findByEmail(String email) {
        Session session = getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        Predicate predicateEmail = criteriaBuilder.equal(root.get("email"), email);
        criteriaQuery.select(root).where(predicateEmail);

        Query<User> query = session.createQuery(criteriaQuery);
        List<User> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public void update(User user) {
        getCurrentSession().merge(user);
    }

    public void delete(User user) {
        getCurrentSession().remove(user);
    }
}
