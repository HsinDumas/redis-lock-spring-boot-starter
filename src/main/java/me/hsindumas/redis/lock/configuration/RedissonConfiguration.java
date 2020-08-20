package me.hsindumas.redis.lock.configuration;

import lombok.RequiredArgsConstructor;
import me.hsindumas.redis.lock.aop.LockAop;
import me.hsindumas.redis.lock.excepiton.LockException;
import me.hsindumas.redis.lock.properties.LockProperties;
import me.hsindumas.redis.lock.properties.MultipleServerConfig;
import me.hsindumas.redis.lock.properties.SingleServerConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.config.ReplicatedServersConfig;
import org.redisson.config.SentinelServersConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Zho.Xin
 * @since 2020/5/18
 */
@Configuration
@EnableConfigurationProperties({LockProperties.class, RedisProperties.class})
@ConditionalOnClass(LockProperties.class)
@RequiredArgsConstructor
public class RedissonConfiguration {

  private final LockProperties lockProperties;
  private final RedisProperties redisProperties;

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
        SingleServerConfig param = lockProperties.getSingleServerConfig();
        singleServerConfig.setAddress(prefixAddress(param.getAddress()));
        singleServerConfig.setDatabase(param.getDatabase());
        singleServerConfig.setPassword(lockProperties.getPassword());
        singleServerConfig.setSslKeystore(lockProperties.getSslKeystore());
        singleServerConfig.setSslKeystorePassword(lockProperties.getSslKeystorePassword());
        singleServerConfig.setSslTruststore(lockProperties.getSslTruststore());
        singleServerConfig.setSslTruststorePassword(lockProperties.getSslTruststorePassword());
        break;
      case CLUSTER:
        ClusterServersConfig clusterServersConfig = config.useClusterServers();
        multipleServerConfig.getNodeAddresses().stream()
            .map(this::prefixAddress)
            .forEach(clusterServersConfig::addNodeAddress);
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
        multipleServerConfig.getNodeAddresses().stream()
            .map(this::prefixAddress)
            .forEach(sentinelServersConfig::addSentinelAddress);
        sentinelServersConfig.setPassword(lockProperties.getPassword());
        sentinelServersConfig.setSslKeystore(lockProperties.getSslKeystore());
        sentinelServersConfig.setSslKeystorePassword(lockProperties.getSslKeystorePassword());
        sentinelServersConfig.setSslTruststore(lockProperties.getSslTruststore());
        sentinelServersConfig.setSslTruststorePassword(lockProperties.getSslTruststorePassword());
        break;
      case REPLICATED:
        ReplicatedServersConfig replicatedServersConfig = config.useReplicatedServers();
        replicatedServersConfig.setDatabase(multipleServerConfig.getDatabase());
        multipleServerConfig.getNodeAddresses().stream()
            .map(this::prefixAddress)
            .forEach(replicatedServersConfig::addNodeAddress);
        replicatedServersConfig.setPassword(lockProperties.getPassword());
        replicatedServersConfig.setSslKeystore(lockProperties.getSslKeystore());
        replicatedServersConfig.setSslKeystorePassword(lockProperties.getSslKeystorePassword());
        replicatedServersConfig.setSslTruststore(lockProperties.getSslTruststore());
        replicatedServersConfig.setSslTruststorePassword(lockProperties.getSslTruststorePassword());
        break;
      case MASTER_SLAVE:
        MasterSlaveServersConfig masterSlaveServersConfig = config.useMasterSlaveServers();
        masterSlaveServersConfig.setDatabase(multipleServerConfig.getDatabase());
        masterSlaveServersConfig.setMasterAddress(
            prefixAddress(multipleServerConfig.getNodeAddresses().remove(0)));
        multipleServerConfig.getNodeAddresses().stream()
            .map(this::prefixAddress)
            .forEach(masterSlaveServersConfig::addSlaveAddress);
        masterSlaveServersConfig.setPassword(lockProperties.getPassword());
        masterSlaveServersConfig.setSslKeystore(lockProperties.getSslKeystore());
        masterSlaveServersConfig.setSslKeystorePassword(lockProperties.getSslKeystorePassword());
        masterSlaveServersConfig.setSslTruststore(lockProperties.getSslTruststore());
        masterSlaveServersConfig.setSslTruststorePassword(
            lockProperties.getSslTruststorePassword());
        break;
      default:
        throw new LockException(
            "lock model " + lockProperties.getModel().name() + " is not supported");
    }
    return Redisson.create(config);
  }

  private String prefixAddress(String address) {
    if (address != null && !address.startsWith("redis")) {
      return "redis://" + address;
    }
    return address;
  }
}
