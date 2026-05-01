package com.stemsep.dao;

import com.stemsep.model.Job;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
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
        Session session = getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Job> criteriaQuery = criteriaBuilder.createQuery(Job.class);
        Root<Job> root = criteriaQuery.from(Job.class);

        Predicate predicateSessionId = criteriaBuilder.equal(root.get("sessionId"), sessionId);
        Order orderByCreatedAtDesc = criteriaBuilder.desc(root.get("createdAt"));
        criteriaQuery.select(root).where(predicateSessionId).orderBy(orderByCreatedAtDesc);

        Query<Job> query = session.createQuery(criteriaQuery);
        return query.getResultList();
    }

    public List<Job> findAll() {
        Session session = getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Job> criteriaQuery = criteriaBuilder.createQuery(Job.class);
        Root<Job> root = criteriaQuery.from(Job.class);

        Order orderByCreatedAtDesc = criteriaBuilder.desc(root.get("createdAt"));
        criteriaQuery.select(root).orderBy(orderByCreatedAtDesc);

        Query<Job> query = session.createQuery(criteriaQuery);
        return query.getResultList();
    }

    public void delete(Job job) {
        getCurrentSession().remove(job);
    }
}
