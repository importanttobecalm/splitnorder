package com.stemsep.service;

import com.stemsep.dao.UserDao;
import com.stemsep.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    @Transactional
    public User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        userDao.save(user);
        logger.info("User created: username={}", username);
        return user;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userDao.findById(id);
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Transactional
    public void updateUser(User user) {
        userDao.update(user);
        logger.info("User updated: id={}", user.getId());
    }

    @Transactional
    public void deleteUser(User user) {
        userDao.delete(user);
        logger.info("User deleted: id={}", user.getId());
    }
}
