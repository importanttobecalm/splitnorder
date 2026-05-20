package com.stemsep.dao;

import com.stemsep.model.MixedTrack;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MixedTrackDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public void save(MixedTrack mix) {
        getCurrentSession().persist(mix);
    }

    public MixedTrack findById(Long id) {
        return getCurrentSession().get(MixedTrack.class, id);
    }

    public MixedTrack findByPublicId(String publicId) {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MixedTrack> cq = cb.createQuery(MixedTrack.class);
        Root<MixedTrack> root = cq.from(MixedTrack.class);
        cq.select(root).where(cb.equal(root.get("publicId"), publicId));
        return session.createQuery(cq).uniqueResult();
    }

    public List<MixedTrack> findByJobId(Long jobId) {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MixedTrack> cq = cb.createQuery(MixedTrack.class);
        Root<MixedTrack> root = cq.from(MixedTrack.class);
        cq.select(root)
          .where(cb.equal(root.get("job").get("id"), jobId))
          .orderBy(cb.desc(root.get("createdAt")));
        return session.createQuery(cq).getResultList();
    }

    public void delete(MixedTrack mix) {
        getCurrentSession().remove(mix);
    }

    /**
     * Kullanıcının tüm mix'lerinin toplam boyutu — depo kotasına eklenir.
     * Slayt 8.pdf "sum Projeksiyonu" birebir.
     */
    public Long sumFileSizeByUserId(Long userId) {
        Session session = getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MixedTrack> root = cq.from(MixedTrack.class);
        cq.select(cb.sum(root.get("fileSize")))
          .where(cb.equal(root.get("job").get("user").get("id"), userId));
        Long result = session.createQuery(cq).uniqueResult();
        return result != null ? result : 0L;
    }
}
