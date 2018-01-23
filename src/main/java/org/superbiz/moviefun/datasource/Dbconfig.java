package org.superbiz.moviefun.datasource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class Dbconfig {

    @Bean
    public HikariDataSource albumsDataSource(
            @Value("${moviefun.datasources.albums.url}") String url,
            @Value("${moviefun.datasources.albums.username}") String username,
            @Value("${moviefun.datasources.albums.password}") String password
    ) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        HikariConfig config = new HikariConfig();
        config.setDataSource(dataSource);
        return new HikariDataSource(config);
    }

    @Bean
    public DataSource moviesDataSource(
            @Value("${moviefun.datasources.movies.url}") String url,
            @Value("${moviefun.datasources.movies.username}") String username,
            @Value("${moviefun.datasources.movies.password}") String password
    ) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        HikariConfig config = new HikariConfig();
        config.setDataSource(dataSource);
        return new HikariDataSource(config);
    }

    @Bean
    public HibernateJpaVendorAdapter getJpa() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean
    @Qualifier("localMoviesEntityManager")
    public LocalContainerEntityManagerFactoryBean localMoviesEntityManager
            (DataSource moviesDataSource, HibernateJpaVendorAdapter moviesJpa) {
        LocalContainerEntityManagerFactoryBean moviesEm = new LocalContainerEntityManagerFactoryBean();
        moviesEm.setDataSource(moviesDataSource);
        moviesEm.setJpaVendorAdapter(moviesJpa);
        moviesEm.setPackagesToScan("org.superbiz.moviefun.movies");
        moviesEm.setPersistenceUnitName("movies");
        return moviesEm;
    }

    @Bean
    @Qualifier("localAlbumsEntityManager")
    public LocalContainerEntityManagerFactoryBean localAlbumsEntityManager
            (DataSource albumsDataSource, HibernateJpaVendorAdapter albumsJpa) {
        LocalContainerEntityManagerFactoryBean albumsEm = new LocalContainerEntityManagerFactoryBean();
        albumsEm.setDataSource(albumsDataSource);
        albumsEm.setJpaVendorAdapter(albumsJpa);
        albumsEm.setPackagesToScan("org.superbiz.moviefun.albums");
        albumsEm.setPersistenceUnitName("albums");
        return albumsEm;
    }

    @Bean
    public PlatformTransactionManager moviesPtm
            (@Qualifier("localMoviesEntityManager") LocalContainerEntityManagerFactoryBean localMoviesEntityManager) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(localMoviesEntityManager.getObject());
        return transactionManager;
    }

    @Bean
    public PlatformTransactionManager albumsPtm
            (@Qualifier("localAlbumsEntityManager") LocalContainerEntityManagerFactoryBean localAlbumsEntityManager) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(localAlbumsEntityManager.getObject());
        return transactionManager;
    }

}
