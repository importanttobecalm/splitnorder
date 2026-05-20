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

import java.time.LocalDateTime;
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

    public Job findByPublicId(String publicId) {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Job> cq = cb.createQuery(Job.class);
        Root<Job> root = cq.from(Job.class);
        cq.select(root).where(cb.equal(root.get("publicId"), publicId));
        return session.createQuery(cq).uniqueResult();
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

    /** {@code createdAt < cutoff} olan tüm job'ları döner — retention job için. */
    public List<Job> findOlderThan(LocalDateTime cutoff) {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Job> cq = cb.createQuery(Job.class);
        Root<Job> root = cq.from(Job.class);
        cq.select(root).where(cb.lessThan(root.get("createdAt"), cutoff));
        return session.createQuery(cq).getResultList();
    }

    public Long sumOriginalFileSizeByUserId(Long userId) {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Job> root = cq.from(Job.class);
        cq.select(cb.sum(root.get("originalFileSize")))
          .where(cb.equal(root.get("user").get("id"), userId));
        Long result = session.createQuery(cq).uniqueResult();
        return result != null ? result : 0L;
    }
}
