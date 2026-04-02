package com.stemsep.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Properties;

@Configuration
public class HibernateConfig {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setDriverClass(env.getProperty("db.driver"));
        ds.setJdbcUrl(env.getProperty("db.url"));
        ds.setUser(env.getProperty("db.username"));
        ds.setPassword(env.getProperty("db.password"));

        // c3p0 connection pool settings
        ds.setMinPoolSize(Integer.parseInt(env.getProperty("c3p0.minPoolSize", "5")));
        ds.setMaxPoolSize(Integer.parseInt(env.getProperty("c3p0.maxPoolSize", "20")));
        ds.setAcquireIncrement(Integer.parseInt(env.getProperty("c3p0.acquireIncrement", "1")));
        ds.setMaxIdleTime(Integer.parseInt(env.getProperty("c3p0.maxIdleTime", "30000")));

        return ds;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sf = new LocalSessionFactoryBean();
        sf.setDataSource(dataSource);
        sf.setPackagesToScan("com.stemsep.model");
        sf.setHibernateProperties(hibernateProperties());
        return sf;
    }

    private Properties hibernateProperties() {
        Properties props = new Properties();
        props.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect"));
        props.setProperty("hibernate.show_sql", env.getProperty("hibernate.show_sql", "true"));
        props.setProperty("hibernate.format_sql", "true");
        props.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto", "update"));
        return props;
    }

    @Bean
    public HibernateTransactionManager transactionManager(org.hibernate.SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }
}
