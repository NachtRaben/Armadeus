package co.aikar.commands;

import co.aikar.locales.MessageKeyProvider;

import java.awt.*;
import java.util.List;

public class JDAHelpFormatter extends CommandHelpFormatter {

    private CommandManager manager;

    public JDAHelpFormatter(CommandManager manager) {
        super(manager);
        this.manager = manager;
    }

    @Override
    public void showAllResults(CommandHelp commandHelp, List<HelpEntry> entries) {
        CommandIssuer issuer = commandHelp.getIssuer();
        StringBuilder sb = new StringBuilder();
        sb.append(internalPrintHelpHeader(commandHelp, issuer));
        for (HelpEntry e : entries) {
            sb.append("\n").append(internalPrintHelpCommand(commandHelp, issuer, e));
        }
        sb.append("\n").append(internalPrintHelpFooter(commandHelp, issuer));
        issuer.sendMessage(sb.toString());
    }

    @Override
    public void showSearchResults(CommandHelp commandHelp, List<HelpEntry> entries) {
        CommandIssuer issuer = commandHelp.getIssuer();
        StringBuilder sb = new StringBuilder();
        sb.append(internalPrintSearchHeader(commandHelp, issuer));
        for (HelpEntry e : entries) {
            sb.append("\n").append(internalPrintSearchEntry(commandHelp, issuer, e));
        }
        sb.append("\n").append(internalPrintSearchFooter(commandHelp, issuer));
        issuer.sendMessage(sb.toString());
    }

    @Override
    public void showDetailedHelp(CommandHelp commandHelp, HelpEntry entry) {
        CommandIssuer issuer = commandHelp.getIssuer();

        // normal help line
        StringBuilder sb = new StringBuilder();
        sb.append(internalPrintDetailedHelpCommand(commandHelp, issuer, entry));

        // additionally detailed help for params
        for (CommandParameter param : entry.getParameters()) {
            String description = param.getDescription();
            if (description != null && !description.isEmpty()) {
                sb.append("\n").append(internalPrintDetailedParameter(commandHelp, issuer, entry, param));
            }
        }
        issuer.sendMessage(sb.toString());
    }

    private String internalPrintHelpHeader(CommandHelp help, CommandIssuer issuer) {
        return this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_HEADER, getHeaderFooterFormatReplacements(help));
    }

    private String internalPrintHelpCommand(CommandHelp help, CommandIssuer issuer, HelpEntry entry) {
        return this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_FORMAT, getEntryFormatReplacements(help, entry));
    }

    private String internalPrintHelpFooter(CommandHelp help, CommandIssuer issuer) {
        if(help.isOnlyPage())
            return "";
        return this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_PAGE_INFORMATION, getHeaderFooterFormatReplacements(help));
    }

    private String internalPrintSearchHeader(CommandHelp help, CommandIssuer issuer) {
        return this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_SEARCH_HEADER, getHeaderFooterFormatReplacements(help));
    }

    private String internalPrintSearchEntry(CommandHelp help, CommandIssuer issuer, HelpEntry page) {
        return this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_FORMAT, getEntryFormatReplacements(help, page));
    }

    public String  internalPrintSearchFooter(CommandHelp help, CommandIssuer issuer) {
        if(help.isOnlyPage())
            return "";
        return this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_PAGE_INFORMATION, getHeaderFooterFormatReplacements(help));
    }

    public String internalPrintDetailedHelpHeader(CommandHelp help, CommandIssuer issuer, HelpEntry entry) {
        return this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_DETAILED_HEADER,
                "{command}", entry.getCommand(),
                "{commandprefix}", help.getCommandPrefix()
        );
    }

    public String internalPrintDetailedHelpCommand(CommandHelp help, CommandIssuer issuer, HelpEntry entry) {
        return this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_DETAILED_COMMAND_FORMAT, getEntryFormatReplacements(help, entry));
    }

    public String internalPrintDetailedParameter(CommandHelp help, CommandIssuer issuer, HelpEntry entry, CommandParameter param) {
        return this.manager.formatMessage(issuer, MessageType.HELP, MessageKeys.HELP_DETAILED_PARAMETER_FORMAT, getParameterFormatReplacements(help, param, entry));
    }

}
