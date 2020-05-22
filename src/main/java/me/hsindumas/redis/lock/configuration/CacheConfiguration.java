package me.hsindumas.redis.lock.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.hsindumas.redis.lock.annotation.EnableCache;
import me.hsindumas.redis.lock.properties.RedissonProperties;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * cache 配置
 *
 * @author Zho.Xin
 * @since 2020/5/22
 */
@Slf4j
@Component
@EnableCaching
@EnableConfigurationProperties(value = RedissonProperties.class)
@RequiredArgsConstructor
public class CacheConfiguration implements ImportAware {
  private String[] value;

  /** 缓存时间 默认30分钟 */
  private long ttl;

  /** 最长空闲时间 默认30分钟 */
  private long maxIdleTime;

  private final RedissonClient redissonClient;

  @Bean
  CacheManager cacheManager() {
    Map<String, CacheConfig> config = new HashMap<>();
    for (String s : value) {
      log.info("初始化spring cache空间{}", s);
      config.put(s, new CacheConfig(ttl, maxIdleTime));
    }
    return new RedissonSpringCacheManager(redissonClient, config);
  }

  @Override
  public void setImportMetadata(AnnotationMetadata importMetadata) {
    Map<String, Object> enableAttrMap =
        importMetadata.getAnnotationAttributes(EnableCache.class.getName());
    AnnotationAttributes enableAttrs = AnnotationAttributes.fromMap(enableAttrMap);
    assert enableAttrs != null;
    value = enableAttrs.getStringArray("value");
    maxIdleTime = enableAttrs.getNumber("maxIdleTime");
    ttl = enableAttrs.getNumber("ttl");
  }
}
