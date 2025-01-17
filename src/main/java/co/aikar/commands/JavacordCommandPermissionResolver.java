/*
 * Copyright (c) 2021 Kevin Zuman (Greenadine)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package co.aikar.commands;

import org.javacord.api.entity.permission.PermissionType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JavacordCommandPermissionResolver implements CommandPermissionResolver {
    private final Map<String, Long> discordPermissionValues;

    public JavacordCommandPermissionResolver() {
        discordPermissionValues = new HashMap<>();
        for (PermissionType permission : PermissionType.values()) {
            discordPermissionValues.put(permission.name().toLowerCase(Locale.ENGLISH).replaceAll("_", "-"), permission.getValue());
        }
    }

    @Override
    public boolean hasPermission(JavacordCommandManager manager, JavacordCommandEvent event, String permission) {
        // Explicitly return true if the user is the bot's owner. They are always allowed.
        if (manager.getBotOwnerId() == event.getIssuer().getMessageAuthor().getId()) {
            return true;
        }

        // Return false on webhook messages, as they cannot have permissions defined.
        // If the User instance for the message author is not present, the author is a webhook.
        if (!event.getIssuer().getMessageAuthor().asUser().isPresent()) {
            return false;
        }

        Long permissionValue = discordPermissionValues.get(permission);
        if (permissionValue == null) {
            return false;
        }

        if (!event.getChannel().asServerTextChannel().isPresent()) return false;

        PermissionType permissionType = getPermission(permissionValue);

        if (permissionType == null) return false;

        return event.getChannel().asServerTextChannel().get().getServer().hasAnyPermission(event.getUser(), permissionType, PermissionType.ADMINISTRATOR);
    }

    private PermissionType getPermission(long value) {
        for (PermissionType type : PermissionType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }

        return null;
    }
}
