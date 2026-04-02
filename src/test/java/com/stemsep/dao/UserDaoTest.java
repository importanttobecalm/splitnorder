package com.stemsep.dao;

import com.stemsep.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UserDaoTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @InjectMocks
    private UserDao userDao;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    public void testSaveUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");

        userDao.save(user);

        verify(session).persist(user);
    }

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

    @Test
    public void testFindByIdReturnsNullWhenNotFound() {
        when(session.get(User.class, 999L)).thenReturn(null);

        User result = userDao.findById(999L);

        assertNull(result);
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("updated");

        userDao.update(user);

        verify(session).merge(user);
    }

    @Test
    public void testDeleteUser() {
        User user = new User();
        user.setId(1L);

        userDao.delete(user);

        verify(session).remove(user);
    }
}
