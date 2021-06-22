package co.aikar.commands;

import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import co.aikar.commands.annotation.Author;
import co.aikar.commands.annotation.CrossGuild;
import co.aikar.commands.annotation.SelfUser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.List;
import java.util.stream.Collectors;

// TODO: Message Keys !!!
public class JDACommandContexts extends CommandContexts<JDACommandExecutionContext> {
    private final JDACommandManager manager;
    private final ShardManager shardManager;

    public JDACommandContexts(JDACommandManager manager) {
        super(manager);
        this.manager = manager;
        this.shardManager = this.manager.getShardManager();
        this.registerIssuerOnlyContext(JDACommandEvent.class, CommandExecutionContext::getIssuer);
        this.registerIssuerOnlyContext(DiscordCommandIssuer.class, c -> (DiscordCommandIssuer) c.getIssuer());
        this.registerIssuerOnlyContext(MessageReceivedEvent.class, c -> c.getIssuer().getEvent());
        this.registerIssuerOnlyContext(SlashCommandEvent.class, c -> c.getIssuer().getSlash());
        this.registerIssuerOnlyContext(Message.class, c -> c.getIssuer().getMessage());
        this.registerIssuerOnlyContext(ChannelType.class, c -> c.getIssuer().getChannel().getType());
        this.registerIssuerOnlyContext(JDA.class, c -> c.getIssuer().getJda());
        this.registerIssuerOnlyContext(ShardManager.class, c -> shardManager);
        this.registerIssuerOnlyContext(Guild.class, c -> {
            MessageChannel channel = c.getIssuer().getChannel();
            if (channel.getType() == ChannelType.PRIVATE && !c.isOptional()) {
                throw new InvalidCommandArgument("This command can only be executed in a Guild.", false);
            } else {
                return c.getIssuer().getGuild();
            }
        });
        this.registerIssuerAwareContext(MessageChannel.class, c -> {
            if (c.hasAnnotation(Author.class)) {
                return c.getIssuer().getChannel();
            }
            boolean isCrossGuild = c.hasAnnotation(CrossGuild.class);
            String argument = c.popFirstArg(); // we pop because we are only issuer aware if we are annotated
            MessageChannel channel = null;
            if (argument.startsWith("<#")) {
                String id = argument.substring(2, argument.length() - 1);
                channel = isCrossGuild ? shardManager.getTextChannelById(id) : c.getIssuer().getGuild().getTextChannelById(id);
            } else {
                List<TextChannel> channelList = isCrossGuild ? shardManager.getShards().stream().flatMap(jda -> jda.getTextChannelsByName(argument, true).stream()).collect(Collectors.toList()) :
                        c.getIssuer().getEvent().getGuild().getTextChannelsByName(argument, true);
                if (channelList.size() > 1) {
                    throw new InvalidCommandArgument("Too many channels were found with the given name. Try with the `#channelname` syntax.", false);
                } else if (channelList.size() == 1) {
                    channel = channelList.get(0);
                }
            }
            if (channel == null) {
                throw new InvalidCommandArgument("Couldn't find a channel with that name or ID.");
            }
            return channel;
        });
        this.registerIssuerAwareContext(User.class, c -> {
            if (c.hasAnnotation(SelfUser.class)) {
                return c.getIssuer().getJda().getSelfUser();
            }
            String arg = c.getFirstArg();
            if (c.isOptional() && (arg == null || arg.isEmpty())) {
                return null;
            }
            arg = c.popFirstArg(); // we pop because we are only issuer aware if we are annotated
            User user = null;
            if (arg.startsWith("<@!")) { // for some reason a ! is added when @'ing and clicking their name.
                user = shardManager.getUserById(arg.substring(3, arg.length() - 1));
            } else if (arg.startsWith("<@")) { // users can /also/ be mentioned like this...
                user = shardManager.getUserById(arg.substring(2, arg.length() - 1));
            } else {
                String finalArg = arg;
                List<User> users = shardManager.getShards().stream().flatMap(jda -> jda.getUsersByName(finalArg, true).stream()).collect(Collectors.toList());
                if (users.size() > 1) {
                    throw new InvalidCommandArgument("Too many users were found with the given name. Try with the `@username#0000` syntax.", false);
                }
                if (!users.isEmpty()) {
                    user = users.get(0);
                }
            }
            if (user == null) {
                throw new InvalidCommandArgument("Could not find a user with that name or ID.");
            }
            return user;
        });
        this.registerContext(Role.class, c -> {
            boolean isCrossGuild = c.hasAnnotation(CrossGuild.class);
            String arg = c.popFirstArg();
            Role role = null;
            if (arg.startsWith("<@&")) {
                String id = arg.substring(3, arg.length() - 1);
                role = isCrossGuild ? shardManager.getRoleById(id) : c.getIssuer().getGuild().getRoleById(id);
            } else {
                List<Role> roles = isCrossGuild ? shardManager.getRolesByName(arg, true)
                        : c.getIssuer().getGuild().getRolesByName(arg, true);
                if (roles.size() > 1) {
                    throw new InvalidCommandArgument("Too many roles were found with the given name. Try with the `@role` syntax.", false);
                }
                if (!roles.isEmpty()) {
                    role = roles.get(0);
                }
            }
            if (role == null) {
                throw new InvalidCommandArgument("Could not find a role with that name or ID.");
            }
            return role;
        });
    }
}
