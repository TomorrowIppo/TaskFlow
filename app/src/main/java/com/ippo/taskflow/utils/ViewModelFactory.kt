package com.ippo.taskflow.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ippo.taskflow.auth.AuthViewModel
import com.ippo.taskflow.group.GroupViewModel
import com.ippo.taskflow.task.TaskViewModel

class ViewModelFactory(
    private val authViewModel: AuthViewModel,
    private val taskViewModel: TaskViewModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 🚨 GroupViewModel 요청 시 두 가지 의존성(AuthVM, TaskVM)을 주입합니다.
        if (modelClass.isAssignableFrom(GroupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupViewModel(authViewModel, taskViewModel) as T
        }

        // 🚨 이 팩토리가 생성할 수 없는 다른 ViewModel 요청이 들어오면 오류를 반환합니다.
        // (이 오류는 TestActivity에서 GroupViewModel에만 팩토리를 적용했기 때문에 발생하지 않아야 하지만, 안전을 위해 남겨둡니다.)
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}. Not handled by custom factory.")
    }
}