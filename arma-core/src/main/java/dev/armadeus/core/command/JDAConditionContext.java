package dev.armadeus.core.command;

import co.aikar.commands.ConditionContext;

public class JDAConditionContext extends ConditionContext<JDACommandEvent> {
    JDAConditionContext(JDACommandEvent issuer, String config) {
        super(issuer, config);
    }
}
