## 简介

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.hsindumas/redis-lock-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.lianjiatech/retrofit-spring-boot-starter)
[![GitHub release](https://img.shields.io/github/v/release/HsinDumas/redis-lock-spring-boot-starter.svg)](https://github.com/HsinDumas/redis-lock-spring-boot-starter/releases)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-2.x+-green.svg)](https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/reference/htmlsingle/)
[![Author](https://img.shields.io/badge/Author-ZhongXin-red.svg?style=flat-square)](https://juejin.im/user/3562073404738584/posts)

本插件是以AOP的形式加上redisson的封装实现。

**`redis-lock-spring-boot-starter`将`redisson`与`spring-boot`框架整合，目的是为了给`spring-boot`开发者提供`最简单`
的基于redis的分布式锁**。

🚀项目持续优化迭代，欢迎大家提ISSUE和PR！麻烦大家能给一颗star✨，您的star是我们持续更新的动力！

<!--more-->

## 功能特性

- [x] 支持SPEL语法
- [x] 可重入锁
- [x] 公平锁
- [x] 联锁
- [x] 红锁
- [x] 读锁
- [x] 写锁

## 快速使用

### pom依赖

```xml
<!-- https://mvnrepository.com/artifact/com.github.hsindumas/redis-lock-spring-boot-starter -->
<dependency>
  <groupId>com.github.hsindumas</groupId>
  <artifactId>redis-lock-spring-boot-starter</artifactId>
  <version>1.1.6</version>
</dependency>
```

### gradle依赖

```groovy
// https://mvnrepository.com/artifact/com.github.hsindumas/redis-lock-spring-boot-starter
compile group: 'com.github.hsindumas', name: 'redis-lock-spring-boot-starter', version: '1.1.6'
```

### 配置redis

按照redisson锁支持的方式，通过`spring.redis.redisson`或者`spring.redis`配置

### 使用

在需要枷锁的方法上添加@Lock注释，设置好key，作为redis的key

```java
@Lock(keys = "test")
@Lock(keys = "#param", keyConstant = ":test")
```

`yml`配置方式： 可以修改全局的锁超时时间（默认30000毫秒）和等待时间（默认10000毫秒）

```yaml
lock:
  lockTime:
  waitTime: 
```

## 反馈建议

如有任何问题，欢迎提issue。

## 鸣谢

- 感谢 JetBrains 提供的免费开源 License：
  <img src="https://images.gitee.com/uploads/images/2020/0406/220236_f5275c90_5531506.png" alt="图片引用自lets-mica" style="float:left;">
