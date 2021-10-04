package co.aikar.commands;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class JDACommandCompletions extends CommandCompletions<CommandCompletionContext<?>> {
    private boolean initialized;

    public JDACommandCompletions(CommandManager manager) {
        super(manager);
        this.initialized = true;
    }

    @Override
    public CommandCompletionHandler registerCompletion(String id, CommandCompletionHandler<CommandCompletionContext<?>> handler) {
        // TODO: Discord slash-commands
        if (initialized) {
            throw new UnsupportedOperationException("JDA Doesn't support Command Completions");
        }
        return null;
    }

    @Override
    public CommandCompletionHandler registerAsyncCompletion(String id, AsyncCommandCompletionHandler<CommandCompletionContext<?>> handler) {
        // TODO: Discord slash-commands
        if (initialized) {
            throw new UnsupportedOperationException("JDA Doesn't support Command Completions");
        }
        return null;
    }

    @NotNull
    @Override
    protected List<String> of(RegisteredCommand command, CommandIssuer sender, String[] args, boolean isAsync) {
        return Collections.emptyList();
    }

    @Override
    protected List<String> getCompletionValues(RegisteredCommand command, CommandIssuer sender, String completion, String[] args, boolean isAsync) {
        return Collections.emptyList();
    }
}
