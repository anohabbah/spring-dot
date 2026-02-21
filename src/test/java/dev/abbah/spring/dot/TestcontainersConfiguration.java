package dev.abbah.spring.dot;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

  static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:18"));

  @Bean
  @RestartScope
  @ServiceConnection
  PostgreSQLContainer postgresContainer() {
    return postgres;
  }

}
