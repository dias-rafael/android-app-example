1. Model: User.kt
data class User(val id: Int, val name: String)


2. Repository: UserRepository.kt
import kotlinx.coroutines.delay
import io.reactivex.rxjava3.core.Observable

class UserRepository {
    // Usando Coroutines
    suspend fun getUsersCoroutine(): List<User> {
        delay(1000) // Simula chamada de rede
        return listOf(User(1, "Alice"), User(2, "Bob"))
    }

    // Usando RxJava
    fun getUsersRx(): Observable<List<User>> {
        return Observable.just(listOf(User(1, "Alice"), User(2, "Bob")))
            .delay(1, java.util.concurrent.TimeUnit.SECONDS)
    }
}


3. ViewModel: UserViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import io.reactivex.rxjava3.disposables.CompositeDisposable

class UserViewModel : ViewModel() {
    private val repository = UserRepository()
    val users = mutableStateListOf<User>()
    private val disposables = CompositeDisposable()

    // Usando Coroutines
    fun fetchUsersCoroutine() {
        viewModelScope.launch {
            val result = repository.getUsersCoroutine()
            users.clear()
            users.addAll(result)
        }
    }

    // Usando RxJava
    fun fetchUsersRx() {
        val disposable = repository.getUsersRx()
            .subscribe { result ->
                users.clear()
                users.addAll(result)
            }
        disposables.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}


4. UI (Jetpack Compose): UserScreen.kt
import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun UserScreen(viewModel: UserViewModel) {
    Column {
        Row {
            Button(onClick = { viewModel.fetchUsersCoroutine() }) {
                Text("Buscar (Coroutines)")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.fetchUsersRx() }) {
                Text("Buscar (RxJava)")
            }
        }
        LazyColumn {
            items(viewModel.users) { user ->
                Text(text = "${user.id}: ${user.name}")
            }
        }
    }
}


5. MainActivity.kt
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    private val viewModel = UserViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UserScreen(viewModel)
        }
    }
}