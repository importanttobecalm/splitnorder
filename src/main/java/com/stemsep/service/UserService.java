package com.stemsep.service;

import com.stemsep.dao.JobDao;
import com.stemsep.dao.UserDao;
import com.stemsep.exception.InvalidCredentialsException;
import com.stemsep.exception.UserNotFoundException;
import com.stemsep.exception.UsernameExistsException;
import com.stemsep.model.Job;
import com.stemsep.model.User;
import com.stemsep.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private JobDao jobDao;

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

    @Transactional
    public User updateUsername(Long userId, String newUsername) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new UserNotFoundException("user not found");
        }
        String trimmed = newUsername == null ? "" : newUsername.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("USERNAME_EMPTY");
        }
        if (!trimmed.equals(user.getUsername())) {
            User existing = userDao.findByUsername(trimmed);
            if (existing != null) {
                throw new UsernameExistsException("username taken");
            }
            user.setUsername(trimmed);
            userDao.update(user);
            logger.info("Username updated: userId={}, newUsername={}", userId, trimmed);
        }
        return user;
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new UserNotFoundException("user not found");
        }
        if (!"LOCAL".equals(user.getAuthProvider())) {
            throw new InvalidCredentialsException("invalid credentials");
        }
        if (!PasswordHasher.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("invalid credentials");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("PASSWORD_TOO_SHORT");
        }
        user.setPasswordHash(PasswordHasher.sha256(newPassword));
        userDao.update(user);
        logger.info("Password changed: userId={}", userId);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new UserNotFoundException("user not found");
        }
        List<Job> jobs = jobDao.findByUserId(userId);
        for (Job job : jobs) {
            jobDao.delete(job);
        }
        userDao.delete(user);
        logger.info("Account deleted: userId={}, jobsRemoved={}", userId, jobs.size());
    }
}
