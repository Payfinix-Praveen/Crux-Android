package com.sujanix.cruxmdm.commands

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.managementapi.commands.model.Command

object InMemoryCommandRepository {
    private val commandLiveData: MutableLiveData<Command> = MutableLiveData()
    fun onCommandStatusChanged(command: Command?) {
        command?.let { commandLiveData.postValue(it) }
    }

    fun getCommandLiveData(): LiveData<Command> {
        return commandLiveData
    }
}