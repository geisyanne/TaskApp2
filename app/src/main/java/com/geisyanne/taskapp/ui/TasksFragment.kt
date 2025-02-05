package com.geisyanne.taskapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.geisyanne.taskapp.R
import com.geisyanne.taskapp.data.db.AppDatabase
import com.geisyanne.taskapp.data.db.repository.TaskRepository
import com.geisyanne.taskapp.data.model.Task
import com.geisyanne.taskapp.databinding.FragmentTasksBinding
import com.geisyanne.taskapp.ui.adapter.TaskAdapter
import com.geisyanne.taskapp.util.NetworkUtils
import com.geisyanne.taskapp.util.showBottomSheet

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    private val taskListViewModel: TaskListViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {

                    val dataBase = AppDatabase.getDatabase(requireContext())
                    val repository = TaskRepository(dataBase.taskDao())

                    @Suppress("UNCHECKED_CAST")
                    return TaskListViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val taskViewModel: TaskViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {

                    val dataBase = AppDatabase.getDatabase(requireContext())
                    val repository = TaskRepository(dataBase.taskDao())

                    @Suppress("UNCHECKED_CAST")
                    return TaskViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        NetworkUtils.showBottomSheetNoInternet(this)

        initListeners()
        initRecyclerViewTask()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        taskListViewModel.getAllTasks()
    }

    private fun initListeners() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_tasksFragment_to_formTaskFragment)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        taskListViewModel.taskList.observe(viewLifecycleOwner) { taskList ->
            listEmpty(taskList)
            taskAdapter.submitList(taskList)
        }

        taskViewModel.taskStateData.observe(viewLifecycleOwner) { stateTask ->
            if (stateTask == StateTask.Delete) {
                taskListViewModel.getAllTasks()
            }
        }
    }

    private fun initRecyclerViewTask() {
        taskAdapter = TaskAdapter { task, option ->
            optionSelected(task, option)
        }

        with(binding.rvTask) {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = taskAdapter
        }
    }

    private fun optionSelected(task: Task, option: Int) {
        when (option) {
            TaskAdapter.SELECT_REMOVE -> {
                showBottomSheet(
                    titleDialog = R.string.title_delete_task,
                    titleButton = R.string.confirm,
                    message = getString(R.string.msg_delete_task),
                    onClick = { taskViewModel.deleteTask(task.id) }
                )
            }

            TaskAdapter.SELECT_EDIT -> {
                val action = TasksFragmentDirections
                    .actionTasksFragmentToFormTaskFragment(task)
                findNavController().navigate(action)
            }

            TaskAdapter.SELECT_DETAILS -> {
                Toast.makeText(
                    requireContext(),
                    "Detalhes: ${task.description}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }


    private fun listEmpty(taskList: List<Task>) {
        binding.progressTodo.isVisible = false

        binding.txtInfoTodo.text = if (taskList.isEmpty()) {
            getString(R.string.task_empty)
        } else {
            ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}