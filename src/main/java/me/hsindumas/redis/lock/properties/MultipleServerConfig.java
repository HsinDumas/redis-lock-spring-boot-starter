package me.hsindumas.redis.lock.properties;

import lombok.Data;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;

import java.util.ArrayList;
import java.util.List;
/**
 * 多节点配置
 *
 * @author Zho.Xin
 * @since 2020 /5/18
 */
@Data
public class MultipleServerConfig {

  private String loadBalancer = "org.redisson.connection.balancer.RoundRobinLoadBalancer";
  private Integer slaveConnectionMinimumIdleSize = 32;
  private Integer slaveConnectionPoolSize = 64;
  private Integer failedSlaveReconnectionInterval = 3000;
  private Integer failedSlaveCheckInterval = 180000;
  private Integer masterConnectionMinimumIdleSize = 32;
  private Integer masterConnectionPoolSize = 64;
  private ReadMode readMode = ReadMode.SLAVE;
  private SubscriptionMode subscriptionMode = SubscriptionMode.SLAVE;
  private Integer subscriptionConnectionMinimumIdleSize = 1;
  private Integer subscriptionConnectionPoolSize = 50;
  private Long dnsMonitoringInterval = 5000L;

  private List<String> nodeAddresses = new ArrayList<>();

  /** 集群,哨兵,云托管 */
  private Integer scanInterval = 1000;

  /** 哨兵模式,云托管,主从 */
  private Integer database = 0;

  /** 哨兵模式 */
  private String masterName;
}
