package global.amal.app
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // If using coroutines
import global.amal.app.IntentRequest

class TabActivityViewModel : ViewModel() {
    // Store the requests here
    val currentIntentRequests = mutableListOf<IntentRequest>()

    fun addRequest(request: IntentRequest) {
        currentIntentRequests.add(request)
    }

    fun findAndRemoveRequest(requestCode: Int): IntentRequest? {
        val index = currentIntentRequests.indexOfFirst { it.requestCode == requestCode }
        return if (index != -1) {
            currentIntentRequests.removeAt(index)
        } else {
            null
        }
    }

    // Optional: Clear requests if needed, e.g., in onCleared()
    override fun onCleared() {
        super.onCleared()
        currentIntentRequests.clear()
    }
}
