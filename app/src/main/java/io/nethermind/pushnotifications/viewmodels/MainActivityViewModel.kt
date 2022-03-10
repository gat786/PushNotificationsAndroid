package io.nethermind.pushnotifications.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {
    val isLoggedIn: MutableLiveData<Boolean> by lazy{ MutableLiveData<Boolean>(false) };
}