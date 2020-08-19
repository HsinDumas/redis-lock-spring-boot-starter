package me.hsindumas.redis.lock.annotation;

import me.hsindumas.redis.lock.enums.LockModel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * The interface Lock.
 *
 * @author Zho.Xin
 * @since 2020/5/18
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lock {

  /**
   * 锁的模式:如果不设置,自动模式,当参数只有一个.使用 REENTRANT 参数多个 MULTIPLE
   *
   * @return the lock model
   */
  LockModel lockModel() default LockModel.AUTO;

  /**
   * 如果keys有多个,如果不设置,则使用 联锁
   *
   * @return [] string [ ]
   */
  String[] keys() default {};

  /**
   * key的静态常量:当key的spel的值是LIST,数组时使用+号连接将会被spel认为这个变量是个字符串,只能产生一把锁,达不到我们的目的,<br>
   * 而我们如果又需要一个常量的话.这个参数将会在拼接在每个元素的后面
   *
   * @return String string
   */
  String keyConstant() default "";

  /**
   * 锁超时时间,默认30000毫秒(可在配置文件全局设置)
   *
   * @return long long
   */
  long lockWatchdogTimeout() default 0;

  /**
   * 等待加锁超时时间,默认10000毫秒 -1 则表示一直等待(可在配置文件全局设置)
   *
   * @return long long
   */
  long attemptTimeout() default 0;
}