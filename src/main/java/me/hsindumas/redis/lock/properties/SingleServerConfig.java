package me.hsindumas.redis.lock.properties;

import lombok.Data;

/**
 * 单节点配置
 *
 * @author Zho.Xin
 * @since 2020/5/18
 */
@Data
public class SingleServerConfig {

  private String address;
  private Integer database = 0;
}
