package me.hsindumas.redis.lock.annotation;

import me.hsindumas.redis.lock.configuration.CacheConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
/**
 * @author Zho.Xin
 * @since 2020/5/22
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.TYPE})
@Documented
@Import(CacheConfiguration.class)
@Configuration
public @interface EnableCache {

  /**
   * 缓存的名称 @Cacheable,@CachePut,@CacheEvict的value必须包含在这里面
   *
   * @return String
   */
  String[] value();

  /**
   * 缓存时间 默认30分钟
   *
   * @return long
   */
  long ttl() default 1000 * 60 * 30L;

  /**
   * 最长空闲时间 默认30分钟
   *
   * @return long
   */
  long maxIdleTime() default 1000 * 60 * 30L;
}
