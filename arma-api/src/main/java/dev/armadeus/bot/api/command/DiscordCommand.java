package dev.armadeus.bot.api.command;

import co.aikar.commands.ACFPatterns;
import co.aikar.commands.ArmaCommandManager;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.commands.CommandOperationContext;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Dependency;
import dev.armadeus.bot.api.ArmaCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

public abstract class DiscordCommand extends BaseCommand {

    private static final int NOTHING = 0;
    private static final int REPLACEMENTS = 1;
    private static final int LOWERCASE = 1 << 1;
    private static final int UPPERCASE = 1 << 2;
    private static final int NO_EMPTY = 1 << 3;
    private static final int DEFAULT_EMPTY = 1 << 4;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Dependency
    protected ArmaCore core;

    public List<String> getConditions() {
        return super.conditions == null ? Collections.emptyList() : List.of(super.conditions.split(ACFPatterns.PIPE.pattern()));
    }

    public List<String> getAliases() {
        final Class<? extends BaseCommand> self = this.getClass();
        return core.commandManager().getAnnotationValues(self, CommandAlias.class, REPLACEMENTS | LOWERCASE | NO_EMPTY);
    }

}
