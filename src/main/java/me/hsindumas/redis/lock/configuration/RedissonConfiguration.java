package me.hsindumas.redis.lock.configuration;

import lombok.RequiredArgsConstructor;
import me.hsindumas.redis.lock.aop.LockAop;
import me.hsindumas.redis.lock.properties.LockProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Zho.Xin
 * @since 2020/5/18
 */
@Configuration
@EnableConfigurationProperties(LockProperties.class)
@ConditionalOnClass(LockProperties.class)
@RequiredArgsConstructor
public class RedissonConfiguration {

  private final LockProperties lockProperties;

  @Bean
  @ConditionalOnMissingBean(LockAop.class)
  public LockAop lockAop() {
    return new LockAop(lockProperties);
  }
}
