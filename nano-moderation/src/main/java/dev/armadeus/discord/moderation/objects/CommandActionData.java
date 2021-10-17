package dev.armadeus.discord.moderation.objects;

import dev.armadeus.bot.api.command.DiscordCommandIssuer;

import java.util.function.Consumer;

public class CommandActionData {
    private DiscordCommandIssuer issuer;
    private boolean canTargetSelf = false;
    private boolean keywordsAllowed = false;
    private boolean notify = false;
    private boolean consume = false;
    private String search;
    private String reason;
    private String action;
    private String error;

    public DiscordCommandIssuer getIssuer() {
        return issuer;
    }

    public CommandActionData setIssuer( DiscordCommandIssuer commandIssuer ) {
        issuer = commandIssuer;
        return this;
    }

    public boolean getNotify() {
        return notify;
    }

    public CommandActionData setNotify( boolean shouldNotify ) {
        notify = shouldNotify;
        return this;
    }

    public String getSearch() {
        return search;
    }

    public CommandActionData setSearch( String searchString ) {
        search = searchString;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public CommandActionData setReason( String reasonString ) {
        reason = reasonString;
        return this;
    }

    public boolean getKeywordsAllowed() {
        return keywordsAllowed;
    }

    public CommandActionData setAllowKeywords( boolean allowKeywords ) {
        keywordsAllowed = allowKeywords;
        return this;
    }

    public String getDescriptor() {
        return action;
    }

    public CommandActionData setDescriptor( String descriptor ) {
        action = descriptor;
        return this;
    }

    public String getErrorString() {
        return error;
    }

    public String getErrorStringOrElse( String fallback ) {
        if ( error != null ) return error;
        return fallback;
    }

    public CommandActionData setErrorMessage( String errorMessage ) {
        error = errorMessage;
        return this;
    }

    public boolean getConsume() {
        return consume;
    }

    public CommandActionData setConsume( boolean shouldConsume ) {
        consume = shouldConsume;
        return this;
    }

    public boolean getCanTargetSelf() { return canTargetSelf; }

    public CommandActionData setCanTargetSelf( boolean selfTarget ) {
        canTargetSelf = selfTarget;
        return this;
    }

    public void run( Consumer<CommandActionData> consumer ) {
        consumer.accept( this );
    }
}
