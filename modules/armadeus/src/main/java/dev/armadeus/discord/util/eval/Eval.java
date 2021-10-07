package dev.armadeus.discord.util.eval;

import co.aikar.commands.CommandIssuer;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Tuple3;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class Eval {

    private final String script;
    private final Map<String, Object> passedVariables;

    public Eval(DiscordCommandIssuer user, String script) {
        this.script = script;
        passedVariables = new HashMap<>(Map.of("user", user));
    }

    public EvalResult<Object, String, Throwable> run() {
        Binding binding = new Binding(passedVariables);
        StringWriter writer = new StringWriter();
        binding.setProperty("out", new PrintWriter(writer));
        GroovyShell shell = new GroovyShell(binding);
        EvalResult<Object, String, Throwable> result;
        try {
            Object eval = shell.evaluate(script);
            result = new EvalResult<>(eval, writer.toString(), null);
        } catch (Exception e) {
            result = new EvalResult<>(null, writer.toString(), e);
        }
        return result;
    }
}