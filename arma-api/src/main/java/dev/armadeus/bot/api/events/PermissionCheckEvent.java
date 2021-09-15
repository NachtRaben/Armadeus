package dev.armadeus.bot.api.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.User;

@RequiredArgsConstructor
@Data
public class PermissionCheckEvent {

    private final User user;
    private boolean allowed = true;

}
