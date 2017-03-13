package com.nachtraben.tohsaka.eval;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by NachtRaben on 3/3/2017.
 */
public enum EvalEngine {

    JAVASCRIPT() {
        private ScriptEngineManager manager = new ScriptEngineManager();

        @Override
        public Triple<Object, String, String> eval(final Map<String, Object> fieldMap, final Collection<String> classes, final Collection<String> packages, String script) {
            StringBuilder importString = new StringBuilder();
            for(String s : packages) {
                if(!StringUtils.startsWith(s, "java.")) continue;
                importString.append(s).append(", ");
            }
            if(importString.length() >= 2) importString.replace(importString.length() - 2, importString.length(), "");

            script = "(function() { with ( new JavaImporter(" + importString.toString() + ")) {" + script + "} }) ();";
            return this.eval(fieldMap, script, manager.getEngineByName("nashorn"));
        }
    },
    GROOVY() {
        @Override
        public Triple<Object, String, String> eval(final Map<String, Object> fieldMap, final Collection<String> classes, final Collection<String> packages, String script) {
            StringBuilder importString = new StringBuilder();
            for(String s : classes) {
                importString.append("import ").append(s).append(";\n");
            }
            for(String s : packages) {
                importString.append("import ").append(s.endsWith(".*") ? s : s + ".*").append(";\n");
            }

            script = importString.toString() + script;
            return this.eval(fieldMap, script, new GroovyScriptEngineImpl());
        }
    };

    public static List<String> DEFAULT_IMPORTS = Arrays.asList(
            "java.lang", "java.io", "java.util", "java.math", "java.util.concurrent", "net.dv8tion.jda.core", //Java Imports
            "net.dv8tion.jda.core", "net.dv8tion.jda.core.entities", "net.dv8tion.core.entities.impl"); //JDA Imports

    private static ScheduledExecutorService service = Executors.newScheduledThreadPool(1, factory -> new Thread(factory, "Eval-Thread"));

    public abstract Triple<Object, String, String> eval(Map<String, Object> fields, final Collection<String> classes, final Collection<String> packages, String script);

    protected Triple<Object, String, String> eval(final Map<String, Object> fields, final String script, final ScriptEngine engine) {

        // Set fields
        for(Map.Entry<String, Object> field : fields.entrySet()) {
            engine.put(field.getKey(), field.getValue());
        }

        // Set writers for intercepting output
        StringWriter stdout = new StringWriter();
        PrintWriter out = new PrintWriter(stdout);
        engine.getContext().setWriter(out);

        // Set writers for intercepting err
        StringWriter stderr = new StringWriter();
        PrintWriter err = new PrintWriter(stderr);
        engine.getContext().setErrorWriter(err);

        ScheduledFuture<Object> future = service.schedule(() -> engine.eval(script), 0, TimeUnit.MILLISECONDS);

        Object result = null;

        try {
            result = future.get(15, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            future.cancel(true);
            err.println(e.toString());
        } catch (ExecutionException e) {
            err.println(e.getCause());
        }
        return new ImmutableTriple<>(result, stdout.toString(), stderr.toString());
    }

}
