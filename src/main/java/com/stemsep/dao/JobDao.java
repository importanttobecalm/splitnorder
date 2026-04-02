package com.stemsep.dao;

import com.stemsep.model.Job;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JobDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public void save(Job job) {
        getCurrentSession().persist(job);
    }

    public Job findById(Long id) {
        return getCurrentSession().get(Job.class, id);
    }

    public void update(Job job) {
        getCurrentSession().merge(job);
    }

    public List<Job> findBySessionId(String sessionId) {
        return getCurrentSession()
                .createQuery("FROM Job j WHERE j.sessionId = :sid ORDER BY j.createdAt DESC", Job.class)
                .setParameter("sid", sessionId)
                .getResultList();
    }

    public List<Job> findAll() {
        return getCurrentSession()
                .createQuery("FROM Job j ORDER BY j.createdAt DESC", Job.class)
                .getResultList();
    }

    public void delete(Job job) {
        getCurrentSession().remove(job);
    }
}
