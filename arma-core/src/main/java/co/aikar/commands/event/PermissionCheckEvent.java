package co.aikar.commands.event;

import co.aikar.commands.JDACommandEvent;
import lombok.Data;

@Data
public class PermissionCheckEvent {

    private final JDACommandEvent event;
    private final String permission;
    private boolean allowed;

}
