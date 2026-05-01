package com.stemsep.dao;

import com.stemsep.model.Stem;
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
        Session session = getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Stem> criteriaQuery = criteriaBuilder.createQuery(Stem.class);
        Root<Stem> root = criteriaQuery.from(Stem.class);

        Predicate predicateJobId = criteriaBuilder.equal(root.get("job").get("id"), jobId);
        criteriaQuery.select(root).where(predicateJobId);

        Query<Stem> query = session.createQuery(criteriaQuery);
        return query.getResultList();
    }

    public Stem findByJobIdAndType(Long jobId, String stemType) {
        Session session = getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Stem> criteriaQuery = criteriaBuilder.createQuery(Stem.class);
        Root<Stem> root = criteriaQuery.from(Stem.class);

        Predicate predicateJobId = criteriaBuilder.equal(root.get("job").get("id"), jobId);
        Predicate predicateStemType = criteriaBuilder.equal(root.get("stemType"), stemType);
        criteriaQuery.select(root).where(criteriaBuilder.and(predicateJobId, predicateStemType));

        Query<Stem> query = session.createQuery(criteriaQuery);
        List<Stem> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}
