package dev.armadeus.bot.api.config;

import java.util.List;
import java.util.Set;

public interface ArmaConfig {

    String getToken();
    List<Integer> getShards();
    Integer getShardsTotal();
    List<Long> getOwnerIds();
    List<Long> getDeveloperIds();
    List<String> getDefaultPrefixes();
    boolean isDatabaseEnabled();

}
