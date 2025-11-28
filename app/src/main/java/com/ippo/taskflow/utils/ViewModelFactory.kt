package com.ippo.taskflow.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ippo.taskflow.auth.AuthViewModel
import com.ippo.taskflow.group.GroupViewModel
import com.ippo.taskflow.task.TaskViewModel

/**
 * Custom ViewModel Factory: 인자가 있는 ViewModel (GroupViewModel)의 인스턴스 생성을 책임집니다.
 */
class ViewModelFactory(
    private val authViewModel: AuthViewModel,
    private val taskViewModel: TaskViewModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // GroupViewModel 요청 시, AuthViewModel과 TaskViewModel을 주입하여 생성
        if (modelClass.isAssignableFrom(GroupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupViewModel(authViewModel, taskViewModel) as T
        }

        // TaskViewModel이나 AuthViewModel처럼 인자 없는 ViewModel은 표준 방식으로 생성
        // (참고: TestActivity에서는 TaskViewModel과 AuthViewModel은 표준 factory로 이미 생성됨)

        throw IllegalArgumentException("Unknown ViewModel class. Please register the ViewModel in the factory.")
    }
}