package com.geisyanne.taskapp.data.db.repository

import com.geisyanne.taskapp.data.db.dao.TaskDao
import com.geisyanne.taskapp.data.db.entity.TaskEntity
import com.geisyanne.taskapp.data.model.Task

class TaskRepository(private val taskDao: TaskDao) {

    suspend fun getAllTask(): List<Task> {
        return taskDao.getAllTask()
    }

    suspend fun insertTask(taskEntity: TaskEntity): Long {
        return taskDao.insertTask(taskEntity)
    }

    suspend fun deleteTask(id: Long) = taskDao.deleteTask(id)

    suspend fun updateTask(taskEntity: TaskEntity) {
        taskDao.updateTask(
            id = taskEntity.id,
            description = taskEntity.description,
            status = taskEntity.status
        )
    }

}