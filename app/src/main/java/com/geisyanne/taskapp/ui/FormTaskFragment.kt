package com.geisyanne.taskapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.geisyanne.taskapp.R
import com.geisyanne.taskapp.data.db.AppDatabase
import com.geisyanne.taskapp.data.db.repository.TaskRepository
import com.geisyanne.taskapp.data.model.Status
import com.geisyanne.taskapp.data.model.Task
import com.geisyanne.taskapp.databinding.FragmentFormTaskBinding
import com.geisyanne.taskapp.util.initToolbar
import com.geisyanne.taskapp.util.showBottomSheet

class FormTaskFragment : BaseFragment() {

    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: Task
    private var status: Status = Status.TODO
    private var newTask: Boolean = true

    private val args: FormTaskFragmentArgs by navArgs()

    private val viewModel: TaskViewModel by viewModels {
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
        _binding = FragmentFormTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbarFormTask)

        getArgs()
        initListeners()
    }

    private fun getArgs() {
        args.task.let {
            if (it != null) {
                this.task = it
                configTask()
            }
        }
    }

    private fun initListeners() {
        binding.btnSave.setOnClickListener {
            observeViewModel()
            validateData()
        }

        binding.radioGroup.setOnCheckedChangeListener { _, id ->
            status = when (id) {
                R.id.rb_todo -> Status.TODO
                R.id.rb_doing -> Status.DOING
                else -> Status.DONE
            }
        }
    }

    private fun configTask() {
        newTask = false
        status = task.status
        binding.txtTbFormTask.setText(R.string.task_edit)
        binding.edtDescription.setText(task.description)
        setStatus()
    }

    private fun setStatus() {
        binding.radioGroup.check(
            when (task.status) {
                Status.TODO -> R.id.rb_todo
                Status.DOING -> R.id.rb_doing
                else -> R.id.rb_done
            }
        )
    }

    private fun validateData() {
        val description = binding.edtDescription.text.toString().trim()

        if (description.isNotEmpty()) {

            hideKeyboard()

            binding.progressFormTask.isVisible = true

            if (newTask) task = Task()
            task.description = description
            task.status = status

            if (newTask) {
                viewModel.insertOrUpdateTask(description = description, status = status)
            } else {
                viewModel.insertOrUpdateTask(id = task.id, description = description, status = status)
            }

        } else {
            showBottomSheet(message = getString(R.string.enter_description))
        }
    }

    private fun observeViewModel() {

        viewModel.taskStateData.observe(viewLifecycleOwner) { stateTask ->
            if (stateTask == StateTask.Inserted || stateTask == StateTask.Updated) {
                findNavController().popBackStack()
            }
        }

        viewModel.taskStateMessage.observe(viewLifecycleOwner) { message ->
            binding.progressFormTask.isVisible = false

            Toast.makeText(
                requireContext(),
                getString(message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}