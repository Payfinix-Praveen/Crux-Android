package com.sujanix.cruxmdm.commands

import android.content.Context
import android.util.Log
import com.google.android.managementapi.commands.LocalCommandClientFactory.create
import com.google.android.managementapi.commands.model.Command
import com.google.android.managementapi.commands.model.IssueCommandRequest
import com.google.android.managementapi.commands.model.IssueCommandRequest.ClearAppsData
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import java.lang.StringBuilder

/** Contains command related utility methods. */
object CommandUtils {

    private const val TAG = "CommandUtils"
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

    fun issueClearAppDataCommand(context: Context, packageNames: List<String?>?) {
        val issueCommandRequest = createClearAppRequest(packageNames)

        Futures.addCallback<Command>(
            create(context)
                .issueCommand(issueCommandRequest),
            object : FutureCallback<Command?> {
                override fun onSuccess(result: Command?) {
                    // Process the returned command result here
                    Log.d(TAG, "onSuccess: ${result?.commandId}")
                }

                override fun onFailure(t: Throwable) {
                    Log.d(TAG, "onFailure: ${t.message}")
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun createClearAppRequest(packageNames: List<String?>?): IssueCommandRequest {
        return IssueCommandRequest.builder()
            .setClearAppsData(
                ClearAppsData.builder()
                    .setPackageNames(packageNames!!)
                    .build()
            ).build()
    }

//    private fun issueRebootCommand(): IssueCommandRequest {
//
//        return IssueCommandRequest.builder()
//            .set(
//                RebootDevice.builder()
//                    .build()
//            ).build()
//    }
}
