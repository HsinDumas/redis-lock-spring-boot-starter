package me.hsindumas.redis.lock.properties;

import lombok.Data;

/**
 * @apiNote 单节点配置
 * @author Zho.Xin
 * @date 2020/5/18
 */
@Data
public class SingleServerConfig {

  private String address;
  private Integer database = 0;
  private Integer connectionPoolSize = 64;
  private Long dnsMonitoringInterval = 5000L;
  private Integer connectionMinimumIdleSize = 32;
  private Integer subscriptionConnectionPoolSize = 50;
  private Integer subscriptionConnectionMinimumIdleSize = 1;
}
