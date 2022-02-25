package projectj.sm.gameserver.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {"projectj.sm.gameserver.domain", "projectj.sm.gameserver.repository"})
@EnableJpaRepositories("projectj.sm.gameserver.repository")
public class JpaConfig {
}
