package com.stemsep.dao;

import com.stemsep.model.Stem;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StemDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public void save(Stem stem) {
        getCurrentSession().persist(stem);
    }

    public Stem findById(Long id) {
        return getCurrentSession().get(Stem.class, id);
    }

    public List<Stem> findByJobId(Long jobId) {
        return getCurrentSession()
                .createQuery("FROM Stem s WHERE s.job.id = :jobId", Stem.class)
                .setParameter("jobId", jobId)
                .getResultList();
    }

    public Stem findByJobIdAndType(Long jobId, String stemType) {
        List<Stem> results = getCurrentSession()
                .createQuery("FROM Stem s WHERE s.job.id = :jobId AND s.stemType = :type", Stem.class)
                .setParameter("jobId", jobId)
                .setParameter("type", stemType)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}
