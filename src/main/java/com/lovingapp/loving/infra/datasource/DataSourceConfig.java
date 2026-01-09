package com.lovingapp.loving.infra.datasource;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;

// @Configuration
public class DataSourceConfig {

    // @Bean
    public DataSource originalDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    // @Bean
    // @Primary
    public DataSource dataSource(DataSource originalDataSource) {
        return new RlsAwareDataSource(originalDataSource);
    }
}
