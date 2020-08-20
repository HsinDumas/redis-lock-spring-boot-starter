package me.hsindumas.redis.lock.properties;

import lombok.Data;
import me.hsindumas.redis.lock.enums.Model;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.net.URL;

/**
 * @author Zho.Xin
 * @since 2020/5/18
 */
@Data
@ConfigurationProperties(prefix = "lock")
public class LockProperties {

  private Model model = Model.SINGLE;

  private String password;

  private URL sslTruststore;
  private String sslTruststorePassword;
  private URL sslKeystore;
  private String sslKeystorePassword;

  /** 锁超时时间 */
  private Long lockTime = 30000L;
  /** 等待加锁超时时间 -1一直等待 */
  private Long waitTime = 10000L;

  @NestedConfigurationProperty private SingleServerConfig singleServerConfig;
  @NestedConfigurationProperty private MultipleServerConfig multipleServerConfig;
}
