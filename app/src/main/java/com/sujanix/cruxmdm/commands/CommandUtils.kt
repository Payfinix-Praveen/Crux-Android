package com.sujanix.cruxmdm.commands

import com.google.android.managementapi.commands.model.Command
import java.lang.StringBuilder

/** Contains command related utility methods. */
object CommandUtils {
    /**
     * Returns the properly formatted string representation of the given [Command] in a way that can
     * be logged and/or surfaced in UI.
     */
    fun parseCommandForPrettyPrint(command: Command): String {
        val stringBuilder =
            StringBuilder()
                .append("Id: ${command.commandId}\n")
                .append("Create time: ${command.createTime}\n")
                .append("Complete time: ${command.completeTime}\n")
                .append("State: ${command.state}\n")

        when (command.status.kind) {
            Command.StatusCase.Kind.CLEAR_APPS_DATA_STATUS ->
                for ((key, value) in command.status.clearAppsDataStatus().statusMap) {
                    stringBuilder.append("\t").append(key).append(": ").append(value.clearStatus).append("\n")
                }

            else -> {}
        }
        return stringBuilder.toString()
    }
}
