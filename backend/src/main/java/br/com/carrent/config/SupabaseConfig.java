package br.com.carrent.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupabaseConfig {

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String usuario,
            @Value("${spring.datasource.password}") String senha,
            @Value("${app.db.pool-max-size:5}") int tamanhoMaximoPool,
            @Value("${app.db.pool-min-idle:1}") int minimoOciosas) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("carrent-supabase-pool");
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setJdbcUrl(garantirSsl(url));
        dataSource.setUsername(usuario);
        dataSource.setPassword(senha);
        dataSource.setMaximumPoolSize(tamanhoMaximoPool);
        dataSource.setMinimumIdle(Math.min(minimoOciosas, tamanhoMaximoPool));
        dataSource.setConnectionTimeout(30_000);
        dataSource.setIdleTimeout(300_000);
        dataSource.setMaxLifetime(1_500_000);
        dataSource.addDataSourceProperty("prepareThreshold", "0");
        dataSource.addDataSourceProperty("ApplicationName", "carrent-backend");
        return dataSource;
    }

    private static String garantirSsl(String url) {
        if (url.contains("supabase") && !url.contains("sslmode=")) {
            return url + (url.contains("?") ? "&" : "?") + "sslmode=require";
        }
        return url;
    }
}
