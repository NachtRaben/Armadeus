package dev.armadeus.discord.util.eval;

import co.aikar.commands.CommandIssuer;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Tuple3;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class Eval {

    private final String script;
    private final Map<String, Object> passedVariables;

    public Eval(CommandIssuer user, String script) {
        this.script = script;
        passedVariables = new HashMap<>(Map.of("user", user));
    }

    public Tuple3<Object, String, Throwable> run() {
        Binding binding = new Binding(passedVariables);
        StringWriter writer = new StringWriter();
        binding.setProperty("out", writer);
        GroovyShell shell = new GroovyShell(binding);
        Tuple3<Object, String, Throwable> result;
        try {
            Object eval = shell.evaluate(script);
            result = new Tuple3<>(eval, writer.toString(), null);
        } catch (Exception e) {
            result = new Tuple3<>(null, writer.toString(), e);
        }
        return result;
    }
}