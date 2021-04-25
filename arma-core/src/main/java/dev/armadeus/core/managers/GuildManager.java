//package dev.armadeus.core.managers;
//
//import dev.armadeus.core.ArmaCore;
//import dev.armadeus.core.configuration.ArmadeusGuildConfig;
//import net.dv8tion.jda.api.entities.Guild;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class GuildManager {
//
//    private static final Object CONNECTION_LOCK = new Object();
//    private static final Logger logger = LogManager.getLogger();
//
//    private final ArmaCore dbot;
//    private final Map<Long, ArmadeusGuildConfig> configs;
//
//    public GuildManager(ArmaCore dbot) {
//        this.dbot = dbot;
//        configs = new HashMap<>();
//    }
//
//    public ArmadeusGuildConfig getConfigurationFor(long guildID) {
//        return configs.computeIfAbsent(guildID, __ -> new ArmadeusGuildConfig(this, guildID).load());
//    }
//
//    public ArmadeusGuildConfig getConfigurationFor(Guild guild) {
//        return getConfigurationFor(guild.getIdLong());
//    }
//
//    public ArmaCore getDbot() {
//        return dbot;
//    }
//
//}
