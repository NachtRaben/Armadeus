package com.nachtraben.armadeus.commands;

import com.nachtraben.core.util.TimeUtil;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.Map;

public class UptimeCommand extends Command {

    private static final RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();

    public UptimeCommand() {
        super("uptime", "", "Gets the uptime of the bot.");
        super.setAliases(Arrays.asList("up", "ontime", "online"));
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        sender.sendMessage("I have been online for: `" + TimeUtil.format(rb.getUptime()) + "`.");
    }

}
