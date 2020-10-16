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
   * 锁的模式:默认情况下,当参数有多个使用 MULTIPLE 否则使用公平锁
   *
   * @return the lock model
   */
  LockModel lockModel() default LockModel.AUTO;

  /** @return [] string [ ] */
  String[] keys();

  /**
   * key的静态常量:当key的spel的值是LIST,数组时使用+号连接将会被spel认为这个变量是个字符串,只能产生一把锁,达不到我们的目的,<br>
   * 而我们如果又需要一个常量的话.这个参数将会在拼接在每个元素的后面
   *
   * @return String string
   */
  String keyConstant() default "";

  /**
   * 锁超时时间,默认使用配置文件全局设置
   *
   * @return long long
   */
  long lockTime() default 0;

  /**
   * 等待加锁超时时间,默认使用配置文件全局设置 -1 则表示一直等待
   *
   * @return long long
   */
  long waitTime() default 0;
}
