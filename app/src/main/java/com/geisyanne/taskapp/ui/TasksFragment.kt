package com.geisyanne.taskapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geisyanne.taskapp.R
import com.geisyanne.taskapp.data.model.Status
import com.geisyanne.taskapp.data.model.Task
import com.geisyanne.taskapp.databinding.FragmentTasksBinding
import com.geisyanne.taskapp.ui.adapter.TaskAdapter
import com.geisyanne.taskapp.util.NetworkUtils
import com.geisyanne.taskapp.util.StateView
import com.geisyanne.taskapp.util.showBottomSheet

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    private val viewModel: TaskViewModel by activityViewModels()

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
        viewModel.getTasks()
    }

    private fun initListeners() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_tasksFragment_to_formTaskFragment)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.taskList.observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.OnLoading -> {
                    binding.progressTodo.isVisible = true
                }

                is StateView.OnSuccess -> {
                    val taskList = stateView.data?.filter { it.status == Status.TODO }

                    binding.progressTodo.isVisible = false
                    listEmpty(taskList ?: emptyList())

                    taskAdapter.submitList(taskList)
                }

                is StateView.OnError -> {
                    Toast.makeText(requireContext(), stateView.msg, Toast.LENGTH_SHORT).show()
                    binding.progressTodo.isVisible = false
                }
            }
        }

        viewModel.taskInsert.observe(viewLifecycleOwner) { stateView ->

            when (stateView) {
                is StateView.OnLoading -> {
                    binding.progressTodo.isVisible = true
                }

                is StateView.OnSuccess -> {
                    binding.progressTodo.isVisible = false

                    if (stateView.data?.status == Status.TODO) {

                        // Armazena lista atual
                        val oldList = taskAdapter.currentList

                        // Gera nova lista com tarefa atualizada
                        val newList = oldList.toMutableList().apply {
                            add(0, stateView.data)
                        }

                        // Envia lista atualizada para adapter
                        taskAdapter.submitList(newList)

                        // Ajusta a posição da rv
                        setPositionRv()
                    }

                }

                is StateView.OnError -> {
                    Toast.makeText(requireContext(), stateView.msg, Toast.LENGTH_SHORT).show()
                    binding.progressTodo.isVisible = false
                }
            }

        }

        viewModel.taskUpdate.observe(viewLifecycleOwner) { stateView ->

            when (stateView) {
                is StateView.OnLoading -> {
                    binding.progressTodo.isVisible = true
                }

                is StateView.OnSuccess -> {
                    binding.progressTodo.isVisible = false

                    // Armazena lista atual
                    val oldList = taskAdapter.currentList

                    // Gera nova lista com tarefa atualizada
                    val newList = oldList.toMutableList().apply {

                        if (!oldList.contains(stateView.data) && stateView.data?.status == Status.TODO) {
                            add(0, stateView.data)
                            setPositionRv()
                        }

                        if (stateView.data?.status == Status.TODO) {
                            find { it.id == stateView.data.id }?.description =
                                stateView.data.description
                        } else {
                            remove(stateView.data)
                        }
                    }

                    // Armazena posição da tarefa a ser atualizada na lista
                    val position = newList.indexOfFirst { it.id == stateView.data?.id }

                    // Envia lista atualizada para adapter
                    taskAdapter.submitList(newList)

                    // Atualiza a tarefa pela posição do adapter
                    taskAdapter.notifyItemChanged(position)

                    listEmpty(newList)
                }

                is StateView.OnError -> {
                    Toast.makeText(requireContext(), stateView.msg, Toast.LENGTH_SHORT).show()
                    binding.progressTodo.isVisible = false
                }
            }

        }

        viewModel.taskDelete.observe(viewLifecycleOwner) { stateView ->

            when (stateView) {
                is StateView.OnLoading -> {
                    binding.progressTodo.isVisible = true
                }

                is StateView.OnSuccess -> {
                    binding.progressTodo.isVisible = false

                    Toast.makeText(requireContext(), R.string.task_delete, Toast.LENGTH_SHORT)
                        .show()

                    val oldList = taskAdapter.currentList
                    val newList = oldList.toMutableList().apply {
                        remove(stateView.data)
                    }

                    taskAdapter.submitList(newList)

                    listEmpty(newList)
                }

                is StateView.OnError -> {
                    Toast.makeText(requireContext(), stateView.msg, Toast.LENGTH_SHORT).show()
                    binding.progressTodo.isVisible = false
                }
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
                    onClick = { viewModel.deleteTask(task) }
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

    // Ajusta a posição da rolagem ao incluir novo item
    private fun setPositionRv() {
        taskAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.rvTask.scrollToPosition(0)
            }
        })
    }

    private fun listEmpty(taskList: List<Task>) {
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