package com.nachtraben.core.util.eval;

import com.nachtraben.core.util.HasteBin;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class Eval {

    private String script;
    private Map<String, Object> vars;

    public Eval(String script, Map<String, Object> vars) {
        this.script = script;
        this.vars = vars;
    }

    public String run() {
        Binding binding = new Binding(vars);
        StringWriter writer = new StringWriter();
        binding.setProperty("out", writer);
        GroovyShell shell = new GroovyShell(binding);

        try {
            Object result = shell.evaluate(script);
            StringBuilder sb = new StringBuilder();
            if (result != null)
                sb.append("Result: ").append(result.toString());

            if (!writer.toString().isEmpty()) {
                if (sb.length() != 0)
                    sb.append("\n");
                sb.append("Output: \n").append(writer.toString());
            }

            if (sb.toString().length() < 2048) {
                return sb.toString();
            } else if (sb.toString().length() <= 40000) {
                return "Output: " + new HasteBin(sb.toString()).getHaste();
            } else {
                File f = new File("Eval-" + Date.from(Instant.now()) + ".txt");
                try (FileWriter fw = new FileWriter(f)) {
                    fw.write(sb.toString());
                    fw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "Output too long, written to " + f.getName() + ".";
            }

        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }
}
