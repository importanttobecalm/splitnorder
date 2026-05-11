package com.stemsep.dao;

import com.stemsep.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.select(root).where(cb.equal(root.get("username"), username));
        List<User> results = session.createQuery(cq).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public User findByEmail(String email) {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.select(root).where(cb.equal(root.get("email"), email));
        List<User> results = session.createQuery(cq).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public User findByGoogleId(String googleId) {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.select(root).where(cb.equal(root.get("googleId"), googleId));
        List<User> results = session.createQuery(cq).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public User findByVerificationToken(String token) {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.select(root).where(cb.equal(root.get("verificationToken"), token));
        List<User> results = session.createQuery(cq).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public void update(User user) {
        getCurrentSession().merge(user);
    }

    public void delete(User user) {
        getCurrentSession().remove(user);
    }
}
