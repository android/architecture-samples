package com.example.android.architecture.blueprints.todoapp.domain

import com.example.android.architecture.blueprints.todoapp.data.Result.Error
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.ACTIVE_TASKS
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType.COMPLETED_TASKS
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [GetTasksUseCase].
 */
@ExperimentalCoroutinesApi
class GetTasksUseCaseTest {

    private val tasksRepository = FakeRepository()

    private val useCase = GetTasksUseCase(tasksRepository)

    @Test
    fun loadTasks_noFilter_empty() = runBlockingTest {
        // Given an empty repository

        // When calling the use case
        val result = useCase()

        // Verify the result is a success and empty
        assertTrue(result is Success)
        assertTrue((result as Success).data.isEmpty())
    }

    @Test
    fun loadTasks_error() = runBlockingTest {
        // Make the repository return errors
        tasksRepository.setReturnError(true)

        // Load tasks
        val result = useCase()

        // Verify the result is an error
        assertTrue(result is Error)
    }

    @Test
    fun loadTasks_noFilter() = runBlockingTest {
        // Given a repository with 1 active and 2 completed tasks:
        tasksRepository.addTasks(
            Task("title", "desc", false),
            Task("title", "desc", true),
            Task("title", "desc", true)
        )

        // Load tasks
        val result = useCase()

        // Verify the result is filtered correctly
        assertTrue(result is Success)
        assertEquals((result as Success).data.size, 3)
    }

    @Test
    fun loadTasks_completedFilter() = runBlockingTest{
        // Given a repository with 1 active and 2 completed tasks:
        tasksRepository.addTasks(
            Task("title", "desc", false),
            Task("title", "desc", true),
            Task("title", "desc", true)
        )

        // Load tasks
        val result = useCase(currentFiltering = COMPLETED_TASKS)

        // Verify the result is filtered correctly
        assertTrue(result is Success)
        assertEquals((result as Success).data.size, 2)
    }

    @Test
    fun loadTasks_activeFilter() = runBlockingTest{
        // Given a repository with 1 active and 2 completed tasks:
        tasksRepository.addTasks(
            Task("title", "desc", false),
            Task("title", "desc", true),
            Task("title", "desc", true)
        )

        // Load tasks
        val result = useCase(currentFiltering = ACTIVE_TASKS)

        // Verify the result is filtered correctly
        assertTrue(result is Success)
        assertEquals((result as Success).data.size, 1)
    }
}