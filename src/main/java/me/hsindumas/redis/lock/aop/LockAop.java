package me.hsindumas.redis.lock.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.hsindumas.redis.lock.annotation.Lock;
import me.hsindumas.redis.lock.enums.LockModel;
import me.hsindumas.redis.lock.excepiton.LockException;
import me.hsindumas.redis.lock.properties.LockProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.RedissonMultiLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Zho.Xin
 * @since 2020/5/18
 */
@Slf4j
@Aspect
@Order(-10)
@RequiredArgsConstructor
public class LockAop {
  private final LockProperties lockProperties;
  @Resource private RedissonClient redissonClient;

  @Pointcut("@annotation(lock)")
  public void controllerAspect(Lock lock) {}

  /**
   * 通过spring Spel 获取参数
   *
   * @param key 定义的key值 以#开头 例如:#user
   * @param parameterNames 形参
   * @param values 形参值
   * @param keyConstant key的常亮
   * @return String
   */
  private List<String> getValueBySpel(
      String key, String[] parameterNames, Object[] values, String keyConstant) {
    List<String> keys = new ArrayList<>();
    if (!key.contains("#")) {
      String s = "lock:" + key + keyConstant;
      log.info("没有使用spel表达式value->{}", s);
      keys.add(s);
      return keys;
    }
    // spel解析器
    ExpressionParser parser = new SpelExpressionParser();
    // spel上下文
    EvaluationContext context = new StandardEvaluationContext();
    for (int i = 0; i < parameterNames.length; i++) {
      context.setVariable(parameterNames[i], values[i]);
    }
    Expression expression = parser.parseExpression(key);
    Object value = expression.getValue(context);
    if (value != null) {
      if (value instanceof List) {
        List<?> value1 = (List<?>) value;
        for (Object o : value1) {
          keys.add("lock:" + o.toString() + keyConstant);
        }
      } else if (value.getClass().isArray()) {
        Object[] obj = (Object[]) value;
        for (Object o : obj) {
          keys.add("lock:" + o.toString() + keyConstant);
        }
      } else {
        keys.add("lock:" + value.toString() + keyConstant);
      }
    }
    log.info("spel expression : key {},value {}", key, keys);
    return keys;
  }

  @Around(value = "controllerAspect(lock)", argNames = "proceedingJoinPoint,lock")
  public Object aroundAdvice(ProceedingJoinPoint proceedingJoinPoint, Lock lock) throws Throwable {
    String[] keys = lock.keys();
    if (keys.length == 0) {
      throw new LockException("keys is required");
    }
    String[] parameterNames =
        new LocalVariableTableParameterNameDiscoverer()
            .getParameterNames(((MethodSignature) proceedingJoinPoint.getSignature()).getMethod());
    Object[] args = proceedingJoinPoint.getArgs();

    long waitTime = lock.attemptTimeout();
    if (waitTime == 0) {
      waitTime = lockProperties.getAttemptTimeout();
    }
    long lockTime = lock.lockTimeout();
    if (lockTime == 0) {
      lockTime = lockProperties.getLockTime();
    }
    LockModel lockModel = lock.lockModel();
    if (lockModel == LockModel.AUTO) {
      if (keys.length > 1) {
        lockModel = LockModel.RED_LOCK;
      } else {
        lockModel = LockModel.FAIR;
      }
    }
    if (lockModel != LockModel.MULTIPLE && lockModel != LockModel.RED_LOCK && keys.length > 1) {
      throw new RuntimeException(
          "There are multiple parameters, the current lock mode is "
              + lockModel.name()
              + ",cannot be locked");
    }
    log.info("锁模式->{},等待锁定时间->{}秒.锁定最长时间->{}秒", lockModel.name(), waitTime / 1000, lockTime / 1000);
    boolean res = false;
    RLock rLock = null;
    // 一直等待加锁.
    switch (lockModel) {
      case FAIR:
        rLock =
            redissonClient.getFairLock(
                getValueBySpel(keys[0], parameterNames, args, lock.keyConstant()).get(0));
        break;
      case RED_LOCK:
        List<RLock> rLocks = new ArrayList<>();
        for (String key : keys) {
          List<String> valueBySpel = getValueBySpel(key, parameterNames, args, lock.keyConstant());
          rLocks = valueBySpel.stream().map(redissonClient::getLock).collect(Collectors.toList());
        }
        RLock[] locks = new RLock[rLocks.size()];
        int index = 0;
        for (RLock r : rLocks) {
          locks[index++] = r;
        }
        rLock = new RedissonRedLock(locks);
        break;
      case MULTIPLE:
        rLocks = new ArrayList<>();
        for (String key : keys) {
          List<String> valueBySpel = getValueBySpel(key, parameterNames, args, lock.keyConstant());
          rLocks = valueBySpel.stream().map(redissonClient::getLock).collect(Collectors.toList());
        }
        locks = new RLock[rLocks.size()];
        index = 0;
        for (RLock r : rLocks) {
          locks[index++] = r;
        }
        rLock = new RedissonMultiLock(locks);
        break;
      case REENTRANT:
        List<String> valueBySpel =
            getValueBySpel(keys[0], parameterNames, args, lock.keyConstant());
        // 如果spel表达式是数组或者LIST 则使用红锁
        if (valueBySpel.size() == 1) {
          rLock = redissonClient.getLock(valueBySpel.get(0));
        } else {
          locks = new RLock[valueBySpel.size()];
          index = 0;
          for (String s : valueBySpel) {
            locks[index++] = redissonClient.getLock(s);
          }
          rLock = new RedissonRedLock(locks);
        }
        break;
      case READ:
        RReadWriteLock readWriteLock =
            redissonClient.getReadWriteLock(
                getValueBySpel(keys[0], parameterNames, args, lock.keyConstant()).get(0));
        rLock = readWriteLock.readLock();
        break;
      case WRITE:
        readWriteLock =
            redissonClient.getReadWriteLock(
                getValueBySpel(keys[0], parameterNames, args, lock.keyConstant()).get(0));
        rLock = readWriteLock.writeLock();
        break;
      default:
    }

    // 执行aop
    if (rLock != null) {
      try {
        if (waitTime == -1) {
          res = true;
          // 一直等待加锁
          rLock.lock(lockTime, TimeUnit.MILLISECONDS);
        } else {
          res = rLock.tryLock(waitTime, lockTime, TimeUnit.MILLISECONDS);
        }
        if (res) {
          return proceedingJoinPoint.proceed();
        } else {
          throw new LockException("请稍后再试~");
        }
      } finally {
        if (res) {
          rLock.unlock();
        }
      }
    }
    throw new LockException("请稍后再试~");
  }
}
