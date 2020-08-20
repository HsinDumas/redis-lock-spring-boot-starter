package me.hsindumas.redis.lock.configuration;

import lombok.RequiredArgsConstructor;
import me.hsindumas.redis.lock.aop.LockAop;
import me.hsindumas.redis.lock.properties.LockProperties;
import me.hsindumas.redis.lock.properties.MultipleServerConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.config.ReplicatedServersConfig;
import org.redisson.config.SentinelServersConfig;
import org.redisson.connection.balancer.LoadBalancer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author Zho.Xin
 * @since 2020/5/18
 */
@Configuration
@EnableConfigurationProperties(LockProperties.class)
@ConditionalOnClass(LockProperties.class)
@RequiredArgsConstructor
public class RedissonConfiguration {

  private final LockProperties lockProperties;

  @Bean
  @ConditionalOnMissingBean(LockAop.class)
  public LockAop lockAop() {
    return new LockAop(lockProperties);
  }

  @Bean
  @ConditionalOnMissingBean(RedissonClient.class)
  public RedissonClient redissonClient() {
    Config config = new Config(new Config());
    MultipleServerConfig multipleServerConfig = lockProperties.getMultipleServerConfig();

    switch (lockProperties.getModel()) {
      case SINGLE:
        org.redisson.config.SingleServerConfig singleServerConfig = config.useSingleServer();
        me.hsindumas.redis.lock.properties.SingleServerConfig param =
            lockProperties.getSingleServerConfig();
        singleServerConfig.setAddress(prefixAddress(param.getAddress()));
        singleServerConfig.setConnectionMinimumIdleSize(param.getConnectionMinimumIdleSize());
        singleServerConfig.setConnectionPoolSize(param.getConnectionPoolSize());
        singleServerConfig.setDatabase(param.getDatabase());
        singleServerConfig.setDnsMonitoringInterval(param.getDnsMonitoringInterval());
        singleServerConfig.setSubscriptionConnectionMinimumIdleSize(
            param.getSubscriptionConnectionMinimumIdleSize());
        singleServerConfig.setSubscriptionConnectionPoolSize(
            param.getSubscriptionConnectionPoolSize());
        singleServerConfig.setPassword(lockProperties.getPassword());
        singleServerConfig.setSslKeystore(lockProperties.getSslKeystore());
        singleServerConfig.setSslKeystorePassword(lockProperties.getSslKeystorePassword());
        singleServerConfig.setSslTruststore(lockProperties.getSslTruststore());
        singleServerConfig.setSslTruststorePassword(lockProperties.getSslTruststorePassword());
        break;
      case CLUSTER:
        ClusterServersConfig clusterServersConfig = config.useClusterServers();
        clusterServersConfig.setScanInterval(multipleServerConfig.getScanInterval());
        clusterServersConfig.setSlaveConnectionMinimumIdleSize(
            multipleServerConfig.getSlaveConnectionMinimumIdleSize());
        clusterServersConfig.setSlaveConnectionPoolSize(
            multipleServerConfig.getSlaveConnectionPoolSize());
        clusterServersConfig.setFailedSlaveReconnectionInterval(
            multipleServerConfig.getFailedSlaveReconnectionInterval());
        clusterServersConfig.setFailedSlaveCheckInterval(
            multipleServerConfig.getFailedSlaveCheckInterval());
        clusterServersConfig.setMasterConnectionMinimumIdleSize(
            multipleServerConfig.getMasterConnectionMinimumIdleSize());
        clusterServersConfig.setMasterConnectionPoolSize(
            multipleServerConfig.getMasterConnectionPoolSize());
        clusterServersConfig.setReadMode(multipleServerConfig.getReadMode());
        clusterServersConfig.setSubscriptionMode(multipleServerConfig.getSubscriptionMode());
        clusterServersConfig.setSubscriptionConnectionMinimumIdleSize(
            multipleServerConfig.getSubscriptionConnectionMinimumIdleSize());
        clusterServersConfig.setSubscriptionConnectionPoolSize(
            multipleServerConfig.getSubscriptionConnectionPoolSize());
        clusterServersConfig.setDnsMonitoringInterval(
            multipleServerConfig.getDnsMonitoringInterval());
        try {
          clusterServersConfig.setLoadBalancer(
              (LoadBalancer) Class.forName(multipleServerConfig.getLoadBalancer()).newInstance());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        for (String nodeAddress : multipleServerConfig.getNodeAddresses()) {
          clusterServersConfig.addNodeAddress(prefixAddress(nodeAddress));
        }
        clusterServersConfig.setPassword(lockProperties.getPassword());
        clusterServersConfig.setSslKeystore(lockProperties.getSslKeystore());
        clusterServersConfig.setSslKeystorePassword(lockProperties.getSslKeystorePassword());
        clusterServersConfig.setSslTruststore(lockProperties.getSslTruststore());
        clusterServersConfig.setSslTruststorePassword(lockProperties.getSslTruststorePassword());
        break;
      case SENTINEL:
        SentinelServersConfig sentinelServersConfig = config.useSentinelServers();
        sentinelServersConfig.setDatabase(multipleServerConfig.getDatabase());
        sentinelServersConfig.setMasterName(multipleServerConfig.getMasterName());
        sentinelServersConfig.setScanInterval(multipleServerConfig.getScanInterval());
        sentinelServersConfig.setSlaveConnectionMinimumIdleSize(
            multipleServerConfig.getSlaveConnectionMinimumIdleSize());
        sentinelServersConfig.setSlaveConnectionPoolSize(
            multipleServerConfig.getSlaveConnectionPoolSize());
        sentinelServersConfig.setFailedSlaveReconnectionInterval(
            multipleServerConfig.getFailedSlaveReconnectionInterval());
        sentinelServersConfig.setFailedSlaveCheckInterval(
            multipleServerConfig.getFailedSlaveCheckInterval());
        sentinelServersConfig.setMasterConnectionMinimumIdleSize(
            multipleServerConfig.getMasterConnectionMinimumIdleSize());
        sentinelServersConfig.setMasterConnectionPoolSize(
            multipleServerConfig.getMasterConnectionPoolSize());
        sentinelServersConfig.setReadMode(multipleServerConfig.getReadMode());
        sentinelServersConfig.setSubscriptionMode(multipleServerConfig.getSubscriptionMode());
        sentinelServersConfig.setSubscriptionConnectionMinimumIdleSize(
            multipleServerConfig.getSubscriptionConnectionMinimumIdleSize());
        sentinelServersConfig.setSubscriptionConnectionPoolSize(
            multipleServerConfig.getSubscriptionConnectionPoolSize());
        sentinelServersConfig.setDnsMonitoringInterval(
            multipleServerConfig.getDnsMonitoringInterval());
        try {
          sentinelServersConfig.setLoadBalancer(
              (LoadBalancer) Class.forName(multipleServerConfig.getLoadBalancer()).newInstance());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        for (String nodeAddress : multipleServerConfig.getNodeAddresses()) {
          sentinelServersConfig.addSentinelAddress(prefixAddress(nodeAddress));
        }
        sentinelServersConfig.setPassword(lockProperties.getPassword());
        sentinelServersConfig.setSslKeystore(lockProperties.getSslKeystore());
        sentinelServersConfig.setSslKeystorePassword(lockProperties.getSslKeystorePassword());
        sentinelServersConfig.setSslTruststore(lockProperties.getSslTruststore());
        sentinelServersConfig.setSslTruststorePassword(lockProperties.getSslTruststorePassword());
        break;
      case REPLICATED:
        ReplicatedServersConfig replicatedServersConfig = config.useReplicatedServers();
        replicatedServersConfig.setDatabase(multipleServerConfig.getDatabase());
        replicatedServersConfig.setScanInterval(multipleServerConfig.getScanInterval());
        replicatedServersConfig.setSlaveConnectionMinimumIdleSize(
            multipleServerConfig.getSlaveConnectionMinimumIdleSize());
        replicatedServersConfig.setSlaveConnectionPoolSize(
            multipleServerConfig.getSlaveConnectionPoolSize());
        replicatedServersConfig.setFailedSlaveReconnectionInterval(
            multipleServerConfig.getFailedSlaveReconnectionInterval());
        replicatedServersConfig.setFailedSlaveCheckInterval(
            multipleServerConfig.getFailedSlaveCheckInterval());
        replicatedServersConfig.setMasterConnectionMinimumIdleSize(
            multipleServerConfig.getMasterConnectionMinimumIdleSize());
        replicatedServersConfig.setMasterConnectionPoolSize(
            multipleServerConfig.getMasterConnectionPoolSize());
        replicatedServersConfig.setReadMode(multipleServerConfig.getReadMode());
        replicatedServersConfig.setSubscriptionMode(multipleServerConfig.getSubscriptionMode());
        replicatedServersConfig.setSubscriptionConnectionMinimumIdleSize(
            multipleServerConfig.getSubscriptionConnectionMinimumIdleSize());
        replicatedServersConfig.setSubscriptionConnectionPoolSize(
            multipleServerConfig.getSubscriptionConnectionPoolSize());
        replicatedServersConfig.setDnsMonitoringInterval(
            multipleServerConfig.getDnsMonitoringInterval());
        try {
          replicatedServersConfig.setLoadBalancer(
              (LoadBalancer) Class.forName(multipleServerConfig.getLoadBalancer()).newInstance());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        for (String nodeAddress : multipleServerConfig.getNodeAddresses()) {
          replicatedServersConfig.addNodeAddress(prefixAddress(nodeAddress));
        }
        replicatedServersConfig.setPassword(lockProperties.getPassword());
        replicatedServersConfig.setSslKeystore(lockProperties.getSslKeystore());
        replicatedServersConfig.setSslKeystorePassword(lockProperties.getSslKeystorePassword());
        replicatedServersConfig.setSslTruststore(lockProperties.getSslTruststore());
        replicatedServersConfig.setSslTruststorePassword(lockProperties.getSslTruststorePassword());
        break;
      case MASTER_SLAVE:
        MasterSlaveServersConfig masterSlaveServersConfig = config.useMasterSlaveServers();
        masterSlaveServersConfig.setDatabase(multipleServerConfig.getDatabase());
        masterSlaveServersConfig.setSlaveConnectionMinimumIdleSize(
            multipleServerConfig.getSlaveConnectionMinimumIdleSize());
        masterSlaveServersConfig.setSlaveConnectionPoolSize(
            multipleServerConfig.getSlaveConnectionPoolSize());
        masterSlaveServersConfig.setFailedSlaveReconnectionInterval(
            multipleServerConfig.getFailedSlaveReconnectionInterval());
        masterSlaveServersConfig.setFailedSlaveCheckInterval(
            multipleServerConfig.getFailedSlaveCheckInterval());
        masterSlaveServersConfig.setMasterConnectionMinimumIdleSize(
            multipleServerConfig.getMasterConnectionMinimumIdleSize());
        masterSlaveServersConfig.setMasterConnectionPoolSize(
            multipleServerConfig.getMasterConnectionPoolSize());
        masterSlaveServersConfig.setReadMode(multipleServerConfig.getReadMode());
        masterSlaveServersConfig.setSubscriptionMode(multipleServerConfig.getSubscriptionMode());
        masterSlaveServersConfig.setSubscriptionConnectionMinimumIdleSize(
            multipleServerConfig.getSubscriptionConnectionMinimumIdleSize());
        masterSlaveServersConfig.setSubscriptionConnectionPoolSize(
            multipleServerConfig.getSubscriptionConnectionPoolSize());
        masterSlaveServersConfig.setDnsMonitoringInterval(
            multipleServerConfig.getDnsMonitoringInterval());
        try {
          masterSlaveServersConfig.setLoadBalancer(
              (LoadBalancer) Class.forName(multipleServerConfig.getLoadBalancer()).newInstance());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        int index = 0;
        for (String nodeAddress : multipleServerConfig.getNodeAddresses()) {
          if (index++ == 0) {
            masterSlaveServersConfig.setMasterAddress(prefixAddress(nodeAddress));
          } else {
            masterSlaveServersConfig.addSlaveAddress(prefixAddress(nodeAddress));
          }
        }
        masterSlaveServersConfig.setPassword(lockProperties.getPassword());
        masterSlaveServersConfig.setSslKeystore(lockProperties.getSslKeystore());
        masterSlaveServersConfig.setSslKeystorePassword(lockProperties.getSslKeystorePassword());
        masterSlaveServersConfig.setSslTruststore(lockProperties.getSslTruststore());
        masterSlaveServersConfig.setSslTruststorePassword(
            lockProperties.getSslTruststorePassword());
        break;
      default:
    }
    return Redisson.create(config);
  }

  private String prefixAddress(String address) {
    if (!StringUtils.isEmpty(address) && !address.startsWith("redis")) {
      return "redis://" + address;
    }
    return address;
  }
}
