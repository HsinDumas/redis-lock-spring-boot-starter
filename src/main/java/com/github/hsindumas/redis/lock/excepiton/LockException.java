package com.github.hsindumas.redis.lock.excepiton;
/**
 * 分布式锁异常
 *
 * @author Zho.Xin
 * @since 2020/5/18
 */
public class LockException extends RuntimeException {

  private static final long serialVersionUID = -2918683545881374966L;

  /**
   * Instantiates a new Lock exception.
   *
   * @param message the message
   */
  public LockException(String message) {
    super(message);
  }
}
