import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import dev.armadeus.core.config.ArmaConfigImpl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.Arrays.asList;

public class ConfigGenerators {

    private static TomlFormat format = TomlFormat.instance();

    public static void main(String[] args) {
        Config.setInsertionOrderPreserved(true);
        generateCoreConfig();
        generateGuildConfig();
    }

    private static void generateCoreConfig() {
        CommentedConfig config = format.createConfig();
        CommentedConfig arma = format.createConfig();
        config.setComment("arma-core", "The default configuration for Arma-Core services");
        config.set("arma-core", arma);

        arma.setComment("token", "The bot token used to connect to discord, available at\nYour token can be found at https://discord.com/developers/applications/");
        arma.set("token", "");
        arma.setComment("shards", "A set of shards this instance will service\nA empty list will assume all shards are hosted by this instance");
        arma.set("shards", Ints.asList());
        arma.setComment("shardsTotal", "The total number of shards hosted by all instances\nRequired to properly shard a bot\nMore information available at https://discord.com/developers/docs/topics/gateway#sharding");
        arma.set("shardsTotal", 1);
        arma.setComment("ownerIds", "A list of Snowflakes identified as the owner of this bot\nUsed to handle commands executable by owners");
        arma.set("ownerIds", Longs.asList(118255810613608451L));
        arma.setComment("developerIds", "A list of Snowflakes identified as developers of this bot\nUsed to handle commands executable by developers");
        arma.set("developerIds", Longs.asList(135923847147945985L));
        arma.setComment("defaultPrefixes", "A set of prefixes used by the bot when the GuildConfig has not set any");
        arma.set("defaultPrefixes", asList("!"));
        CommentedConfig database = format.createConfig();
        new ObjectConverter().toConfig(new ArmaConfigImpl.Database(), database);
        database.setComment("enabled", "Should we enable and use database connections to save bot/guild data");
        database.setComment("username", "Username used to connect to the database");
        database.setComment("password", "Password used to connect to the database");
        config.set("database", database);
        save(config, Path.of("arma-core/src/main/resources/configs/arma-core.toml"));
    }

    private static void generateGuildConfig() {
        CommentedConfig config = format.createConfig();
        config.setComment("commandCooldown", "Rate-limit in seconds for command executions\nSet to 0 to disable");
        config.set("commandCooldown", 0);
        config.setComment("purgeDelay", "Delay in seconds for bot responses to be deleted\nSet to -1 to disable\nSet to 0 to use the default (120 Seconds)");
        config.set("purgeDelay", 0);
        config.setComment("deleteCommands", "Should the bot delete user generated command message");
        config.set("deleteCommands", true);
        config.setComment("prefixes", "Guild specific prefixes the bot will respond to\nThese will override any global prefixes defined by the bot");
        config.set("prefixes", List.of());
        config.setComment("disabledCommands", "List of command permissions to disabled in the guild\nThese tokens can be regex to match multiple command permissions");
        config.set("disabledCommands", List.of("command.unknown", "command.unknown2.*"));
        save(config, Path.of("arma-core/src/main/resources/configs/guild-config.toml"));
        config.setComment("disabledCommandsByRole", "List of command permissions to disable per role\nThese tokens can be regex to match multiple command permissions");
        config.set(asList("disabledCommandsByRole", "123456789L"), List.of("command.unknown", "command.unknown2.*"));
        save(config, Path.of("arma-core/src/main/resources/configs/guild-config.toml"));
    }

    private static void save(CommentedConfig config, Path path) {
        try(BufferedWriter out = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
            format.createWriter().write(config, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
