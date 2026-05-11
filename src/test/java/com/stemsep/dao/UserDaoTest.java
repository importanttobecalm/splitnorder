package com.stemsep.dao;

import com.stemsep.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
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
}
