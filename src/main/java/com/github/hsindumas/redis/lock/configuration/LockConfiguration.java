package com.github.hsindumas.redis.lock.configuration;

import com.github.hsindumas.redis.lock.aop.LockAop;
import com.github.hsindumas.redis.lock.properties.LockProperties;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Zho.Xin
 * @since 2020/5/18
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnBean(RedissonClient.class)
@EnableConfigurationProperties(LockProperties.class)
public class LockConfiguration {

    private final LockProperties lockProperties;
    private final RedissonClient redissonClient;

    /**
     * Lock aop lock aop.
     *
     * @return the lock aop
     */
    @Bean
    @ConditionalOnMissingBean(LockAop.class)
    public LockAop lockAop() {
        return new LockAop(lockProperties, redissonClient);
    }
}
