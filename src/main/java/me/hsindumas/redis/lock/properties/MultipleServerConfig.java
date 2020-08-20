package me.hsindumas.redis.lock.properties;

import lombok.Data;

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

  private List<String> nodeAddresses = new ArrayList<>();

  /** 哨兵模式,云托管,主从 */
  private Integer database = 0;

  /** 哨兵模式 */
  private String masterName;
}
