package com.github.hsindumas.redis.lock.aop;

import com.github.hsindumas.redis.lock.annotation.Lock;
import com.github.hsindumas.redis.lock.enums.LockModel;
import com.github.hsindumas.redis.lock.excepiton.LockException;
import com.github.hsindumas.redis.lock.properties.LockProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.RedissonMultiLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

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

    private final RedissonClient redissonClient;

    /**
     * Lock aspect.
     *
     * @param lock the lock
     */
    @Pointcut("@annotation(lock)")
    public void lockAspect(Lock lock) {}

    private List<String> getValueBySpel(String key, String[] parameterNames, Object[] values, String keyConstant) {
        List<String> keys = new ArrayList<>();
        if (!key.contains("#")) {
            keys.add(key + keyConstant);
        } else {
            ExpressionParser parser = new SpelExpressionParser();
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
                        keys.add(o.toString() + keyConstant);
                    }
                } else if (value.getClass().isArray()) {
                    Object[] obj = (Object[]) value;
                    for (Object o : obj) {
                        keys.add(o.toString() + keyConstant);
                    }
                } else {
                    keys.add(value + keyConstant);
                }
            }
        }
        return keys;
    }

    /**
     * Around advice object.
     *
     * @param proceedingJoinPoint the proceeding join point
     * @param lock the lock
     * @return the object
     * @throws Throwable the throwable
     */
    @Around(value = "lockAspect(lock)", argNames = "proceedingJoinPoint,lock")
    public Object aroundAdvice(ProceedingJoinPoint proceedingJoinPoint, Lock lock) throws Throwable {
        String[] keys = lock.keys();
        if (keys.length == 0) {
            throw new LockException("the lock keys are required");
        }
        String[] parameterNames = new LocalVariableTableParameterNameDiscoverer()
                .getParameterNames(((MethodSignature) proceedingJoinPoint.getSignature()).getMethod());
        Object[] args = proceedingJoinPoint.getArgs();

        long waitTime = lock.waitTime();
        if (waitTime == 0) {
            waitTime = lockProperties.getWaitTime();
        }
        long lockTime = lock.lockTime();
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
            throw new RuntimeException("the lock mode " + lockModel.name() + " should have a single parameter");
        }
        RLock rLock;
        List<String> lockKeys = null;
        switch (lockModel) {
            case FAIR:
                lockKeys = getValueBySpel(keys[0], parameterNames, args, lock.keyConstant());
                rLock = redissonClient.getFairLock(lockKeys.get(0));
                break;
            case RED_LOCK:
                List<RLock> rLocks = new ArrayList<>();
                for (String key : keys) {
                    lockKeys = getValueBySpel(key, parameterNames, args, lock.keyConstant());
                    rLocks.addAll(lockKeys.stream().map(redissonClient::getLock).collect(Collectors.toList()));
                }
                rLock = new RedissonRedLock(rLocks.toArray(new RLock[0]));
                break;
            case MULTIPLE:
                rLocks = new ArrayList<>();
                for (String key : keys) {
                    lockKeys = getValueBySpel(key, parameterNames, args, lock.keyConstant());
                    rLocks.addAll(lockKeys.stream().map(redissonClient::getLock).collect(Collectors.toList()));
                }
                rLock = new RedissonMultiLock(rLocks.toArray(new RLock[0]));
                break;
            case REENTRANT:
                lockKeys = getValueBySpel(keys[0], parameterNames, args, lock.keyConstant());
                if (lockKeys.size() == 1) {
                    rLock = redissonClient.getLock(lockKeys.get(0));
                    break;
                }
                rLock = new RedissonRedLock(
                        lockKeys.stream().map(redissonClient::getLock).toArray(RLock[]::new));
                break;
            case READ:
                lockKeys = getValueBySpel(keys[0], parameterNames, args, lock.keyConstant());
                rLock = redissonClient.getReadWriteLock(lockKeys.get(0)).readLock();
                break;
            case WRITE:
                lockKeys = getValueBySpel(keys[0], parameterNames, args, lock.keyConstant());
                rLock = redissonClient.getReadWriteLock(lockKeys.get(0)).writeLock();
                break;
            default:
                throw new LockException("lock model " + lockModel.name() + " is not supported");
        }

        if (rLock == null) {
            throw new LockException("can not init the lock");
        }
        log.info(
                "prepare to acquire the {} lock with {}. WaitTime [{}]. LockTime [{}]",
                lockModel.name(),
                lockKeys,
                waitTime,
                lockTime);

        boolean res = false;
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
            }
            throw new LockException("can not acquire the lock");
        } finally {
            if (res) {
                rLock.unlock();
            }
        }
    }
}
