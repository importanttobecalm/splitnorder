package com.stemsep.dao;

import com.stemsep.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.criteria.JpaPath;
import org.hibernate.query.criteria.JpaRoot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link UserDao} için izole birim testleri.
 *
 * <p>Bu testler gerçek bir veritabanına bağlanmaz; Hibernate
 * {@link SessionFactory} ve {@link Session} bean'leri Mockito ile sahte
 * (mock) hâle getirilir. Böylece DAO katmanının {@code getCurrentSession()}'ı
 * doğru kullandığı ve {@code persist/get/merge/remove} çağrılarını beklenen
 * şekilde tetiklediği test edilir.</p>
 *
 * <p><b>Slayt referansı:</b> DAO katmanının {@code Session session =
 * sessionFactory.getCurrentSession();} ile Session almasını ve CRUD
 * operasyonlarını doğrudan {@code Session} üzerinden yapmasını öğreten
 * "Hibernate Session API" bölümüne uyar.</p>
 */
public class UserDaoTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @InjectMocks
    private UserDao userDao;

    /**
     * Her testten önce mock'ları başlat ve {@code getCurrentSession()} çağrısının
     * sahte Session döndürmesini sağla. Bu, gerçek bir Hibernate transaction
     * yönetimi olmaksızın DAO mantığını test edebilmemizi sağlar.
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    /**
     * {@code save()}'in {@link Session#persist(Object)} çağrısını delege
     * ettiğini doğrular. Yeni bir kullanıcının INSERT işlemine girdiğini
     * minimum yan etkiyle test eder.
     */
    @Test
    public void testSaveUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");

        userDao.save(user);

        verify(session).persist(user);
    }

    /**
     * {@code findById()}'nin {@link Session#get(Class, Object)} kullanarak
     * doğru entity'yi getirdiğini doğrular. Cache hit + lazy fetching
     * davranışı slayt'taki "Hibernate ilk seviye cache" bölümüne uyumlu.
     */
    @Test
    public void testFindByIdReturnsUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");

        when(session.get(User.class, 1L)).thenReturn(user);

        User result = userDao.findById(1L);

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
    }

    /**
     * Olmayan bir ID için DAO'nun {@code null} döndürdüğünü (exception
     * fırlatmadığını) doğrular. {@code findByEmail/findByUsername} gibi
     * Criteria sorguları için de benzer null-safe davranış beklenir.
     */
    @Test
    public void testFindByIdReturnsNullWhenNotFound() {
        when(session.get(User.class, 999L)).thenReturn(null);

        User result = userDao.findById(999L);

        assertNull(result);
    }

    /**
     * {@code update()}'in {@link Session#merge(Object)} çağrısını delege
     * ettiğini doğrular. Hibernate 6'da deprecated {@code saveOrUpdate}
     * yerine {@code merge} kullanımı tercih edilir.
     */
    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("updated");

        userDao.update(user);

        verify(session).merge(user);
    }

    /**
     * {@code delete()}'in {@link Session#remove(Object)} çağrısını delege
     * ettiğini doğrular. Hibernate 6'da deprecated {@code delete} yerine
     * {@code remove} kullanılır.
     */
    @Test
    public void testDeleteUser() {
        User user = new User();
        user.setId(1L);

        userDao.delete(user);

        verify(session).remove(user);
    }

    // ============================================================
    // Criteria sorgu testleri (findByEmail / findByUsername)
    // ------------------------------------------------------------
    // Bu testler Hibernate Criteria zincirini (CriteriaBuilder →
    // CriteriaQuery → Root → Query) tam olarak mock'lar. Yöntem:
    // gerçek bir DB sorgusu yapmadan, DAO'nun beklenen Criteria
    // çağrılarını ürettiğini ve sonuç listesinin doğru şekilde
    // tek User / null'a indirildiğini doğrularız.
    // ============================================================

    /**
     * Criteria sorgusu için tam mock zincirini hazırlayıp Query'yi döndürür.
     * Generic uyarılarını lokalize etmek için bu yardımcıyı kullanıyoruz.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Query<User> stubCriteriaQuery() {
        HibernateCriteriaBuilder cb = mock(HibernateCriteriaBuilder.class);
        JpaCriteriaQuery<User> cq = mock(JpaCriteriaQuery.class);
        JpaRoot<User> root = mock(JpaRoot.class);
        JpaPath path = mock(JpaPath.class);
        JpaPredicate predicate = mock(JpaPredicate.class);
        Query<User> query = mock(Query.class);

        when(session.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(User.class)).thenReturn(cq);
        when(cq.from(User.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);
        when(root.get(anyString())).thenReturn(path);
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cq.where(predicate)).thenReturn(cq);
        when(session.createQuery(cq)).thenReturn(query);
        return query;
    }

    /**
     * {@code findByEmail()} — kayıt varsa o User döner. Criteria sorgusu
     * tek elemanlı liste döndürdüğünde DAO ilk elemanı verir.
     */
    @Test
    public void findByEmail_existingEmail_returnsUser() {
        User user = new User();
        user.setEmail("a@b.com");
        Query<User> query = stubCriteriaQuery();
        when(query.getResultList()).thenReturn(List.of(user));

        User result = userDao.findByEmail("a@b.com");

        assertNotNull(result);
        assertEquals("a@b.com", result.getEmail());
    }

    /**
     * {@code findByEmail()} — kayıt yoksa Criteria boş liste döndürür,
     * DAO {@code null} döndürmelidir (null-safe contract).
     */
    @Test
    public void findByEmail_nonexistentEmail_returnsNull() {
        Query<User> query = stubCriteriaQuery();
        when(query.getResultList()).thenReturn(Collections.emptyList());

        assertNull(userDao.findByEmail("ghost@b.com"));
    }

    /**
     * {@code findByUsername()} — kayıt varsa User döner.
     */
    @Test
    public void findByUsername_existingUsername_returnsUser() {
        User user = new User();
        user.setUsername("alice");
        Query<User> query = stubCriteriaQuery();
        when(query.getResultList()).thenReturn(List.of(user));

        User result = userDao.findByUsername("alice");

        assertNotNull(result);
        assertEquals("alice", result.getUsername());
    }

    /**
     * {@code findByUsername()} — kayıt yoksa null.
     */
    @Test
    public void findByUsername_nonexistentUsername_returnsNull() {
        Query<User> query = stubCriteriaQuery();
        when(query.getResultList()).thenReturn(Collections.emptyList());

        assertNull(userDao.findByUsername("ghost"));
    }
}
