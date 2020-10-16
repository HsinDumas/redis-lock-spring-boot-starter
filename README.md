
## 简介

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.hsindumas/redis-lock-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.lianjiatech/retrofit-spring-boot-starter)
[![GitHub release](https://img.shields.io/github/v/release/HsinDumas/redis-lock-spring-boot-starter.svg)](https://github.com/HsinDumas/redis-lock-spring-boot-starter/releases)
[![License](https://img.shields.io/badge/JDK-1.8+-4EB1BA.svg)](https://docs.oracle.com/javase/8/docs/index.html)
[![License](https://img.shields.io/badge/SpringBoot-2.x+-green.svg)](https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/reference/htmlsingle/)
[![Author](https://img.shields.io/badge/Author-ZhongXin-red.svg?style=flat-square)](https://juejin.im/user/3562073404738584/posts)

本插件是以AOP的形式加上redisson的封装实现。

可以修改锁超时时间和等待加锁超时时间

**`redis-lock-spring-boot-starter`将`redisson`与`spring-boot`框架快速整合，目的是为了实现`最简单`的基于redis的分布式锁**。

🚀项目持续优化迭代，欢迎大家提ISSUE和PR！麻烦大家能给一颗star✨，您的star是我们持续更新的动力！

<!--more-->

## 功能特性

- [x] [支持SPEL语法](#支持SPEL语法)

## 快速使用

### pom依赖

```xml
<!-- https://mvnrepository.com/artifact/com.github.hsindumas/redis-lock-spring-boot-starter -->
<dependency>
    <groupId>com.github.hsindumas</groupId>
    <artifactId>redis-lock-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

### gradle依赖

```groovy
// https://mvnrepository.com/artifact/com.github.hsindumas/redis-lock-spring-boot-starter
compile group: 'com.github.hsindumas', name: 'redis-lock-spring-boot-starter', version: '1.1.0'
```

### 配置redis

不需要额外配置redis，直接按照`spring.redis`的配置，避免创建没必要的redis连接

### 使用
在需要枷锁的方法上添加@Lock注释，设置好key，作为redis的key


`yml`配置方式：
可以修改全局的锁超时时间和等待时间
```yaml
lock:
  lockTime: 30000L
  waitTime: 10000L
```


## 反馈建议

如有任何问题，欢迎提issue。