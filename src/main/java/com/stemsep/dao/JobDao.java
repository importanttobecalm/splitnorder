package com.stemsep.dao;

import com.stemsep.model.Job;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public List<Job> findByUserId(Long userId) {
        return findByUserIdAndQuery(userId, null);
    }

    public List<Job> findByUserIdAndQuery(Long userId, String query) {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Job> cq = cb.createQuery(Job.class);
        Root<Job> root = cq.from(Job.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("user").get("id"), userId));
        if (query != null && !query.trim().isEmpty()) {
            String pattern = "%" + query.trim().toLowerCase(new Locale("tr", "TR")) + "%";
            predicates.add(cb.like(cb.lower(root.get("originalFilename")), pattern));
        }
        cq.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.desc(root.get("createdAt")));

        return session.createQuery(cq).getResultList();
    }

    public List<Job> findAll() {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Job> cq = cb.createQuery(Job.class);
        Root<Job> root = cq.from(Job.class);
        cq.select(root).orderBy(cb.desc(root.get("createdAt")));
        return session.createQuery(cq).getResultList();
    }

    public void delete(Job job) {
        getCurrentSession().remove(job);
    }
}
