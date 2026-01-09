package com.lovingapp.loving.infra.datasource;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(DataSource originalDataSource) {
        return new RlsAwareDataSource(originalDataSource);
    }
}
