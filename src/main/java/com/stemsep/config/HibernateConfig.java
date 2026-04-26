package com.stemsep.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import java.util.Properties;

import static org.hibernate.cfg.AvailableSettings.*;

/**
 * Hibernate konfigürasyonu — ders7-8-9.md §7.7 kalıbına uygun.
 * hibernate.properties dosyasından okur (mysql.* + hibernate.* + hibernate.c3p0.*).
 */
@Configuration
public class HibernateConfig {

    @Autowired
    private Environment env;

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
        factoryBean.setHibernateProperties(buildHibernateProperties());
        factoryBean.setPackagesToScan("com.stemsep.model");
        return factoryBean;
    }

    @Bean
    public HibernateTransactionManager transactionManager(LocalSessionFactoryBean sessionFactory) {
        HibernateTransactionManager tm = new HibernateTransactionManager();
        tm.setSessionFactory(sessionFactory.getObject());
        return tm;
    }

    private Properties buildHibernateProperties() {
        Properties props = new Properties();

        // JDBC
        props.put(DRIVER,   env.getProperty("mysql.driver"));
        props.put(URL,      env.getProperty("mysql.url"));
        props.put(USER,     env.getProperty("mysql.user"));
        props.put(PASS,     env.getProperty("mysql.password"));

        // Hibernate
        props.put(SHOW_SQL,       env.getProperty("hibernate.show_sql"));
        props.put(FORMAT_SQL,     env.getProperty("hibernate.format_sql"));
        props.put(HBM2DDL_AUTO,   env.getProperty("hibernate.hbm2ddl.auto"));
        props.put(DIALECT,        env.getProperty("hibernate.dialect"));
        props.put(DEFAULT_SCHEMA, env.getProperty("hibernate.default_schema"));

        // c3p0
        props.put(C3P0_MIN_SIZE,           env.getProperty("hibernate.c3p0.min_size"));
        props.put(C3P0_MAX_SIZE,           env.getProperty("hibernate.c3p0.max_size"));
        props.put(C3P0_ACQUIRE_INCREMENT,  env.getProperty("hibernate.c3p0.acquire_increment"));
        props.put(C3P0_TIMEOUT,            env.getProperty("hibernate.c3p0.timeout"));
        props.put(C3P0_MAX_STATEMENTS,     env.getProperty("hibernate.c3p0.max_statements"));
        props.put(C3P0_CONFIG_PREFIX + ".initialPoolSize",
                env.getProperty("hibernate.c3p0.initialPoolSize"));
        props.put(C3P0_CONFIG_PREFIX + ".idleConnectionTestPeriod",
                env.getProperty("hibernate.c3p0.idle_test_period"));
        props.put(C3P0_CONFIG_PREFIX + ".acquireRetryAttempts",
                env.getProperty("hibernate.c3p0.acquireRetryAttempts"));
        props.put(C3P0_CONFIG_PREFIX + ".acquireRetryDelay",
                env.getProperty("hibernate.c3p0.acquireRetryDelay"));

        return props;
    }
}
