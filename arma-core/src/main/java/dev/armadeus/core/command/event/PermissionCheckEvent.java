package dev.armadeus.core.command.event;

import dev.armadeus.core.command.JDACommandEvent;
import lombok.Data;

@Data
public class PermissionCheckEvent {

    private final JDACommandEvent event;
    private final String permission;
    private boolean allowed;

}
