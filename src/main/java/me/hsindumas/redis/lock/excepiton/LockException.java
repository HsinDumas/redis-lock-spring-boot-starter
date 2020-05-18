package me.hsindumas.redis.lock.excepiton;
/**
 * @apiNote 分布式锁异常
 * @author Zho.Xin
 * @date 2020/5/18
 */
public class LockException extends RuntimeException {

  private static final long serialVersionUID = -2918683545881374966L;

  public LockException(String message) {
    super(message);
  }
}
