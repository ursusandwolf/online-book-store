package com.lisu.onlinestore.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean;

@Configuration
@ComponentScan(basePackages = "com.lisu.onlinestore")
public class AppConfig {
    @Bean
    public DataSource getDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/test");
        dataSource.setUsername("root");
        dataSource.setPassword("Root123$");
        return dataSource;
    }

    @Bean
    public LocalSessionFactoryBean getSessionFactory() {
        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
        factoryBean.setDataSource(getDataSource());

        Properties properties = new Properties();
        properties.put("show_sql", "true");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");

        factoryBean.setHibernateProperties(properties);
        factoryBean.setPackagesToScan("com.lisu.onlinestore");

        return factoryBean;
    }
}
