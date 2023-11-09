package com.rmblack.todoapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.view.marginTop
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
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.viewmodels.SharedTasksViewModel
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.UUID

class SharedTasksFragment : TasksFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        viewModel = ViewModelProvider(
            this, SharedFragmentViewModelFactory(sharedPreferencesManager)
        )[SharedTasksViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val res = super.onCreateView(inflater, container, savedInstanceState)
        binding.tasksRv.adapter = createSharedTasksAdapter()
        return res
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreLastExpandedID(savedInstanceState)
        setUpRecyclerview()
        setUpClickListeners()
        setUpConnectionManagementSection()
    }

    private fun restoreLastExpandedID(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (savedInstanceState != null) {
                val id = savedInstanceState.getSerializable(LAST_EXPANDED_ID_KEY, UUID::class.java)
                viewModel.setPreviouslyExpandedID(id)
            }
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun setUpConnectionManagementSection() {
        viewLifecycleOwner.lifecycleScope.launch {
            val userToken = viewModel.getUser()?.token
            if (userToken == null) {
                binding.manageConnectionBtn.visibility = View.GONE
            } else {
                binding.manageConnectionBtn.visibility = View.VISIBLE
            }

            val fm = childFragmentManager

            fm.beginTransaction()
                .add(R.id.manage_user_connection_container, connectionStatusFragment, "2")
                .hide(connectionStatusFragment).commit()
            fm.beginTransaction()
                .add(R.id.manage_user_connection_container, connectUserFragment, "1").commit()

            if (userToken != null) {
                if (viewModel.getConnectedPhones() == null) {
                    fm.beginTransaction().hide(connectionStatusFragment).show(connectUserFragment)
                        .commit()
                } else {
                    fm.beginTransaction().hide(connectUserFragment).show(connectionStatusFragment)
                        .commit()
                }
            } else {
                binding.manageUserConnectionContainer.visibility = View.GONE
            }

            viewModel.tasks.take(2).collect { tasks ->
                if (tasks.size != 1) setUpConnectionManagerVisibility()
            }

            viewModel.syncConnectedPhonesWithServer()
            (connectionStatusFragment as RefreshCallback).onRefresh()
            (connectUserFragment as RefreshCallback).onRefresh()
        }
    }

    private fun setUpConnectionManagerVisibility() {
        if (viewModel.getConnectedPhones() == null && viewModel.getUserToken() != null) {
            binding.manageConnectionBtn.rotation = 180f
            binding.manageUserConnectionContainer.visibility = View.VISIBLE
        } else {
            binding.manageUserConnectionContainer.visibility = View.GONE
        }
    }

    private fun setUpClickListeners() {
        if (viewModel.getUser()?.token == null) {
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
            if (viewModel.getUser()?.token == null) {
                goToStarterActivity()
            } else {
                if (binding.manageUserConnectionContainer.visibility == View.GONE) {
                    binding.manageUserConnectionContainer.startAnimation(slideInAnimation)
                    binding.manageUserConnectionContainer.visibility = View.VISIBLE
                    binding.manageConnectionBtn.rotation = 180f
                } else {
                    hideKeyboard()
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
                    setUpForNewOrEditTask(tasks, editedTaskId, isNewTask)
                    editedTaskId = null
                    isNewTask = null

                    val layoutManager = binding.tasksRv.layoutManager as LinearLayoutManager
                    val firstVisibleItem = layoutManager.getChildAt(0)
                    val offset = firstVisibleItem?.top ?: 0
                    val pos = layoutManager.findFirstVisibleItemPosition()
                    val marginTop = firstVisibleItem?.marginTop ?: 0

                    val adapter = binding.tasksRv.adapter as SharedTasksAdapter
                    adapter.updateData(tasks)

                    layoutManager.scrollToPositionWithOffset(pos, offset - marginTop)

                    if (tasks.size != 1) setUpNoTaskIconAndText(tasks.isNotEmpty())
                }
            }
        }
    }

    override fun setUpNoTaskIconAndText(hide: Boolean) {
        if (viewModel.getUser()?.token != null) {
            binding.tvNoTask.text = "تسکی برای انجام نداری!"
            binding.ivNoTask.setImageResource(R.drawable.ic_no_task)
        } else {
            binding.ivNoTask.setImageResource(R.drawable.ic_open_door)
            binding.tvNoTask.text =
                "برای استفاده از بخش اشتراکی ،\n باید ابتدا وارد حساب کاربری شوید."
        }
        super.setUpNoTaskIconAndText(hide)
    }

    private fun createSharedTasksAdapter() = SharedTasksAdapter(
        viewLifecycleOwner.lifecycleScope, viewModel, this, requireActivity()
    )

    class SharedFragmentViewModelFactory(private val sharedPreferencesManager: SharedPreferencesManager) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            return if (modelClass.isAssignableFrom(SharedTasksViewModel::class.java)) {
                SharedTasksViewModel(sharedPreferencesManager) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}