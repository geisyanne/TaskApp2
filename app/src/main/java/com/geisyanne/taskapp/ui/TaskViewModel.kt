package com.geisyanne.taskapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geisyanne.taskapp.R
import com.geisyanne.taskapp.data.db.entity.toTaskEntity
import com.geisyanne.taskapp.data.db.repository.TaskRepository
import com.geisyanne.taskapp.data.model.Status
import com.geisyanne.taskapp.data.model.Task
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _taskStateData = MutableLiveData<StateTask>()
    val taskStateData: LiveData<StateTask> = _taskStateData

    private val _taskStateMessage = MutableLiveData<Int>()
    val taskStateMessage: LiveData<Int> = _taskStateMessage

    fun insertOrUpdateTask(id: Long = 0, description: String, status: Status) {
        if (id == 0L) {
            insertTask(Task(description = description, status = status))
        } else {
            updateTask(Task(id, description, status))
        }
    }

    fun getTasks() {

    }

    fun deleteTask(id: Long) = viewModelScope.launch {
        try {
            repository.deleteTask(id)

            _taskStateData.postValue(StateTask.Delete)
            _taskStateMessage.postValue(R.string.task_delete)

        } catch (ex: Exception) {
            _taskStateMessage.postValue(R.string.task_delete_error)
        }
    }

    private fun insertTask(task: Task) = viewModelScope.launch {
        try {
            val id = repository.insertTask(task.toTaskEntity())

            if (id > 0) {
                _taskStateData.postValue(StateTask.Inserted)
                _taskStateMessage.postValue(R.string.task_saved)
            }

        } catch (ex: Exception) {
            _taskStateMessage.postValue(R.string.task_saved_error)
        }
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        try {
            repository.updateTask(task.toTaskEntity())

            _taskStateData.postValue(StateTask.Updated)
            _taskStateMessage.postValue(R.string.task_updated)

        } catch (ex: Exception) {
            _taskStateMessage.postValue(R.string.task_updated_error)
        }
    }

}

sealed class StateTask {
    object Inserted : StateTask()
    object Updated : StateTask()
    object Delete : StateTask()
}