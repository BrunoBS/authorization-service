package br.com.portalmanager.platform.authorization.foundation.configuration;

import br.com.portalmanager.platform.authorization.core.authorization.usecase.port.AuthorizationRequirementFallback;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Configuration
@ConditionalOnProperty(prefix = "platform.authorization.fallback", name = "enabled", havingValue = "true")
public class AuthorizationViewConfiguration {

    @Bean
    DataSource authorizationViewDataSource(AuthorizationViewProperties properties) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(properties.jdbcUrl());
        dataSource.setUsername(properties.username());
        dataSource.setPassword(properties.password());
        return dataSource;
    }

    @Bean
    JdbcTemplate authorizationViewJdbcTemplate(DataSource authorizationViewDataSource) {
        return new JdbcTemplate(authorizationViewDataSource);
    }

    @Bean
    AuthorizationRequirementFallback authorizationRequirementFallback(
            JdbcTemplate authorizationViewJdbcTemplate, AuthorizationViewProperties properties) {
        return new JdbcAuthorizationRequirementFallback(authorizationViewJdbcTemplate, properties.query());
    }

    @Bean
    AuthorizationViewProperties authorizationViewProperties(
            org.springframework.core.env.Environment environment) {
        return new AuthorizationViewProperties(
                environment.getRequiredProperty("platform.authorization.fallback.jdbc-url"),
                environment.getProperty("platform.authorization.fallback.username", ""),
                environment.getProperty("platform.authorization.fallback.password", ""),
                environment.getRequiredProperty("platform.authorization.fallback.query"));
    }

    public record AuthorizationViewProperties(String jdbcUrl, String username, String password, String query)
            implements InitializingBean {
        @Override
        public void afterPropertiesSet() {
            if (!StringUtils.hasText(jdbcUrl) || !StringUtils.hasText(query)) {
                throw new IllegalStateException("Authorization view fallback requires a JDBC URL and query");
            }
            long placeholders = query.chars().filter(character -> character == '?').count();
            if (placeholders != 1 || !query.stripLeading().regionMatches(true, 0, "SELECT", 0, 6)) {
                throw new IllegalStateException(
                        "Authorization view fallback query must be a SELECT with one positional parameter");
            }
        }
    }

    private static final class JdbcAuthorizationRequirementFallback implements AuthorizationRequirementFallback {
        private final JdbcTemplate jdbc;
        private final String query;

        private JdbcAuthorizationRequirementFallback(JdbcTemplate jdbc, String query) {
            this.jdbc = jdbc;
            this.query = query;
        }

        @Override
        public Optional<String> findRequiredGroup(String cacheKey) {
            List<String> groups = jdbc.query(query, (resultSet, rowNumber) -> resultSet.getString(1), cacheKey);
            if (groups.size() > 1) {
                throw new IllegalStateException("Authorization view returned more than one required group");
            }
            return groups.stream().filter(StringUtils::hasText).findFirst();
        }
    }
}
