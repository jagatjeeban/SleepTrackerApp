/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

     val nights = database.getAllNights()

    private var tonight = MutableLiveData<SleepNight?>()


    init {
        initializeTonight()
    }

    //coroutine builder method
    private fun initializeTonight() {
        viewModelScope.launch { tonight.value = getTonightFromDatabase() }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        var night = database.getTonight()

        if (night?.endTimeMilli != night?.startTimeMilli) {
            night = null
        }
        return night
    }

    fun onStartTracking() {
        viewModelScope.launch {
            val newNight = SleepNight()
            insert(newNight)
            tonight.value = getTonightFromDatabase()
        }
    }
    private suspend fun insert(night: SleepNight) {
        database.insert(night)
    }

    fun onStopTracking() {
        viewModelScope.launch {

            /**
             * The return@label syntax specifies the function from which this statement returns,
             * among several nested functions.
             */
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)

            _navigateToSleepQuality.value = oldNight
        }
    }

    private suspend fun update(night: SleepNight) {
        database.update(night)
    }

    fun onClear() {
        viewModelScope.launch {
            clear()
            tonight.value = null
            _showSnackbarEvent.value = true
        }
    }
    private suspend fun clear() {
        database.clear()
    }

    //backing property for navigation
    private val _navigateToSleepQuality = MutableLiveData<SleepNight?>()
    val navigateToSleepQuality : LiveData<SleepNight?>
    get() = _navigateToSleepQuality

    fun doneNavigating(){
        _navigateToSleepQuality.value = null
    }

    /**
     * Updating the buttons for visibility
     */
    val startButtonVisible = Transformations.map(tonight){
        it == null
    }
    val stopButtonVisible = Transformations.map(tonight){
        it != null
    }
    val clearButtonVisible = Transformations.map(nights){
        it?.isNotEmpty()
    }

    //snackbar event
    private val _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackbarEvent :LiveData<Boolean>
    get() = _showSnackbarEvent

    fun doneShowingSnackbar(){
        _showSnackbarEvent.value = false
    }

    private val _navigateToSleepDetail = MutableLiveData<Long?>()
    val navigateToSleepDetail: MutableLiveData<Long?>
        get() = _navigateToSleepDetail

    fun onSleepNightClicked(id:Long){
        _navigateToSleepDetail.value = id
    }
    fun onSleepDetailNavigated() {
        _navigateToSleepDetail.value = null
    }

}

