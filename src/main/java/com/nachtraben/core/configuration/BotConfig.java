package com.nachtraben.core.configuration;

import com.nachtraben.lemonslice.JsonProperties;
import com.nachtraben.lemonslice.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class BotConfig extends JsonProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotConfig.class);

    @Property(name = "botToken")
    private String botToken = "plzchange";

    @Property(name = "shardCount")
    private int shardCount = -1;

    @Property(name = "ownerIDs")
    private List<Long> ownerIDs = Collections.singletonList(1L);

    @Property(name = "developerIDs")
    private List<Long> developerIDs = Collections.singletonList(1L);

    @Property(name = "defaultPrefixes")
    private List<String> defaultPrefixes = Collections.singletonList("-");

    @Property(name = "errorLogChannelId")
    private Long errorLogChannelId = -1L;

    @Property(name = "useRedis")
    private boolean useRedis = false;

    @Property(name = "redisHost")
    private String redisHost = "localhost";

    @Property(name = "redisPort")
    private int redisPort = 6379;

    @Property(name = "redisPassword")
    private String redisPassword = "changeplz";

    @Property(name = "redisTimeout")
    private int redisTimeout = 10000;

    public String getBotToken() {
        return botToken;
    }

    public int getShardCount() {
        return shardCount;
    }

    public List<Long> getOwnerIDs() {
        return ownerIDs;
    }

    public List<Long> getDeveloperIDs() {
        return developerIDs;
    }

    public List<String> getDefaultPrefixes() {
        return defaultPrefixes;
    }

    public Long getErrorLogChannelId() {
        return errorLogChannelId;
    }

    public boolean isUseRedis() {
        return useRedis;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public int getRedisTimeout() {
        return redisTimeout;
    }
}
