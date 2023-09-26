package com.rmblack.todoapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.rmblack.todoapp.R
import com.rmblack.todoapp.activities.StarterActivity
import com.rmblack.todoapp.adapters.SharedTasksAdapter
import com.rmblack.todoapp.databinding.FragmentTasksBinding
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.viewmodels.SharedTasksViewModel
import kotlinx.coroutines.launch
import java.util.UUID

class SharedTasksFragment : TasksFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        binding.tasksRv.layoutManager = LinearLayoutManager(context)

        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        viewModel = ViewModelProvider(this, SharedFragmentViewModelFactory(sharedPreferencesManager))
            .get(SharedTasksViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpConnectionManagementSection()
        setUpRecyclerview()
        setUpSwipeToDelete()
        setUpClickListeners()
    }

    private fun setUpConnectionManagementSection() {
        if (viewModel.getUser() == null) {
            binding.manageConnectionBtn.visibility = View.GONE
        } else {
            binding.manageConnectionBtn.visibility = View.VISIBLE
        }

        val firstFragment: Fragment = ConnectUserFragment()
        val secondFragment: Fragment = ConnectionStatusFragment()
        val fm = childFragmentManager

        fm.beginTransaction().add(R.id.manage_user_connection_container, secondFragment, "2").hide(secondFragment).commit()
        fm.beginTransaction().add(R.id.manage_user_connection_container, firstFragment, "1").commit()

        if (viewModel.getUser() != null) {
            if (viewModel.getConnectedPhone() == "") {
                binding.manageConnectionBtn.rotation = 180f
                binding.manageUserConnectionContainer.visibility = View.VISIBLE
                fm.beginTransaction().hide(secondFragment).show(firstFragment).commit()
            } else {
                binding.manageUserConnectionContainer.visibility = View.GONE
                fm.beginTransaction().hide(firstFragment).show(secondFragment).commit()
            }
        } else {
            binding.manageUserConnectionContainer.visibility = View.GONE
        }
    }

    private fun setUpClickListeners() {
        if (viewModel.getUser() == null) {
            binding.ivNoTask.setOnClickListener {
                goToStarterActivity()
            }
            binding.tvNoTask.setOnClickListener {
                goToStarterActivity()
            }
        }

        binding.manageConnectionBtn.setOnClickListener {
            val slideInAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in)
            val slideOutAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out)
            if (viewModel.getUser() == null) {
                goToStarterActivity()
            } else {
                if (binding.manageUserConnectionContainer.visibility == View.GONE) {
                    binding.manageUserConnectionContainer.startAnimation(slideInAnimation)
                    binding.manageUserConnectionContainer.visibility = View.VISIBLE
                    binding.manageConnectionBtn.rotation = 180f
                } else {
                    binding.manageUserConnectionContainer.startAnimation(slideOutAnimation)
                    binding.manageUserConnectionContainer.visibility = View.GONE
                    binding.manageConnectionBtn.rotation = 0f
                }
            }
        }
    }

    private fun goToStarterActivity() {
        val intent = Intent(requireContext(), StarterActivity::class.java)
        startActivity(intent)
    }

    private fun setUpRecyclerview() {
        var editedTaskId: UUID? = null

        var isNewTask: Boolean? = null

        setFragmentResultListener(
            EditTaskBottomSheet.REQUEST_KEY_ID_SHARED
        ) { _, bundle ->
            editedTaskId = bundle.getSerializable(EditTaskBottomSheet.BUNDLE_KEY_ID_SHARED) as UUID?
        }

        setFragmentResultListener(
            EditTaskBottomSheet.REQUEST_KEY_IS_NEW_SHARED
        ) { _, bundle ->
            isNewTask = bundle.getBoolean(EditTaskBottomSheet.BUNDLE_KEY_IS_NEW_SHARED)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    if (viewModel.detailsVisibility.size != viewModel.tasks.value.size) {
                        setUpForTaskMoving()
                    } else if (editedTaskId != null) {
                        setUpForNewOrEditTask(tasks, editedTaskId, isNewTask)
                    }
                    editedTaskId = null
                    isNewTask = null
                    while (viewModel.detailsVisibility.size > viewModel.tasks.value.size) {
                        setUpIfLabeledTaskMoved()
                    }

                    val layoutManager = binding.tasksRv.layoutManager as LinearLayoutManager
                    val firstVisibleItem = layoutManager.getChildAt(0)
                    val offset = firstVisibleItem?.top ?: 0
                    val pos = layoutManager.findFirstVisibleItemPosition()
                    val marginTop = firstVisibleItem?.marginTop ?: 0

                    binding.tasksRv.adapter = createSharedTasksAdapter()

                    layoutManager.scrollToPositionWithOffset(pos, offset - marginTop)

                    if (tasks.size != 1) setUpNoTaskIconAndText(tasks.isNotEmpty())
                }
            }
        }
    }

    override fun setUpNoTaskIconAndText(hide: Boolean) {
        if (viewModel.getUser() != null) {
            if (viewModel.getConnectedPhone() == "") {
                binding.ivNoTask.setImageResource(R.drawable.ic_community)
                binding.tvNoTask.text = "ابتدا باید به لیست اشتراکی \n فرد مورد نظرتان متصل شوید "
            } else {
                binding.tvNoTask.text = "تسکی برای انجام نداری!"
                binding.ivNoTask.setImageResource(R.drawable.ic_no_task)
            }
            super.setUpNoTaskIconAndText(hide)
        } else {
            binding.ivNoTask.setImageResource(R.drawable.ic_open_door)
            binding.tvNoTask.text = "برای استفاده از بخش اشتراکی ،\n باید ابتدا وارد حساب کاربری شوید."
            super.setUpNoTaskIconAndText(hide)

        }
    }

    private fun createSharedTasksAdapter() =
        SharedTasksAdapter(viewModel.getUserToken(), viewModel, this, requireActivity())

    class SharedFragmentViewModelFactory(private val sharedPreferencesManager: SharedPreferencesManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(SharedTasksViewModel::class.java)) {
                SharedTasksViewModel(sharedPreferencesManager) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}