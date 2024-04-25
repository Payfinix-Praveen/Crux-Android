package com.sujanix.cruxmdm.notification

import android.util.Log
import com.google.android.managementapi.commands.CommandListener
import com.google.android.managementapi.commands.model.Command
import com.google.android.managementapi.notification.NotificationReceiverService
import com.sujanix.cruxmdm.commands.CommandUtils
import com.sujanix.cruxmdm.commands.InMemoryCommandRepository

class NotificationReceiverService : NotificationReceiverService() {

    override fun getCommandListener(): CommandListener {
        return object : CommandListener {
            override fun onCommandStatusChanged(command: Command) {
                Log.i("FATAL", "onCommandStatusChanged: ${CommandUtils.parseCommandForPrettyPrint(command)}")
                InMemoryCommandRepository.onCommandStatusChanged(command)
            }
        }
    }
}