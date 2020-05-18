package me.hsindumas.redis.lock.util;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author Zho.Xin
 * @date 2019/11/20
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {
  private final RedissonClient redissonClient;

  public void set(String key, String value, long timeout) {
    redissonClient.getBucket(key).set(value, timeout, TimeUnit.MILLISECONDS);
  }

  public void set(String key, String value) {
    redissonClient.getBucket(key).set(value);
  }

  public String get(String key) {
    Object value = redissonClient.getBucket(key).get();
    if (value == null) {
      return null;
    }
    return value.toString();
  }

  public String getAndDelete(String key) {
    Object value = redissonClient.getBucket(key).getAndDelete();
    if (value == null) {
      return null;
    }
    return value.toString();
  }

  public void delete(String key) {
    redissonClient.getBucket(key).delete();
  }
}
