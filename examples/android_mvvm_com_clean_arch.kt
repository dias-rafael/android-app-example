android_mvvm_com_clean_arch/
├── domain/
│   ├── model/
│   │   └── User.kt
│   ├── repository/
│   │   └── UserRepository.kt
│   └── usecase/
│       ├── GetUsersUseCase.kt
│       └── GetUsersUseCaseImpl.kt
├── data/
│   ├── datasource/
│   │   ├── UserRemoteDataSource.kt
│   │   └── UserRemoteDataSourceImpl.kt
│   └── repository/
│       └── UserRepositoryImpl.kt
├── di/
│   └── AppModule.kt
├── presentation/
│   ├── viewmodel/
│   │   └── UserViewModel.kt
│   ├── ui/
│   │   └── UserScreen.kt
│   └── MainActivity.kt




// 1. Domain Layer
// Model: User.kt
data class User(val id: Int, val name: String)

// Repository Contract
interface UserRepository {
    suspend fun getUsersCoroutine(): List<User>
    fun getUsersRx(): io.reactivex.rxjava3.core.Observable<List<User>>
}

// UseCase Contract
interface GetUsersUseCase {
    suspend fun getUsersCoroutine(): List<User>
    fun getUsersRx(): io.reactivex.rxjava3.core.Observable<List<User>>
}

// UseCase Implementation
import javax.inject.Inject

class GetUsersUseCaseImpl @Inject constructor(
    private val repository: UserRepository
) : GetUsersUseCase {
    override suspend fun getUsersCoroutine() = repository.getUsersCoroutine()
    override fun getUsersRx() = repository.getUsersRx()
}

// Dependency Injection Container

// Hilt DI Module
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideUserRemoteDataSource(): UserRemoteDataSource = UserRemoteDataSourceImpl()

    @Provides
    @Singleton
    fun provideUserRepository(remoteDataSource: UserRemoteDataSource): UserRepository = UserRepositoryImpl(remoteDataSource)

    @Provides
    @Singleton
    fun provideGetUsersUseCase(userRepository: UserRepository): GetUsersUseCase = GetUsersUseCaseImpl(userRepository)
}


// 2. Data Layer
// DataSource Contracts
interface UserRemoteDataSource {
    suspend fun fetchUsers(): List<User>
    fun fetchUsersRx(): io.reactivex.rxjava3.core.Observable<List<User>>
}

// DataSource Implementation
import kotlinx.coroutines.delay
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class UserRemoteDataSourceImpl @Inject constructor() : UserRemoteDataSource {
    override suspend fun fetchUsers(): List<User> {
        delay(1000)
        return listOf(User(1, "Alice"), User(2, "Bob"))
    }
    override fun fetchUsersRx(): Observable<List<User>> {
        return Observable.just(listOf(User(1, "Alice"), User(2, "Bob")))
            .delay(1, java.util.concurrent.TimeUnit.SECONDS)
    }
}

// Repository Implementation adaptada para usar DataSource
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource
) : UserRepository {
    override suspend fun getUsersCoroutine(): List<User> {
        return remoteDataSource.fetchUsers()
    }
    override fun getUsersRx(): Observable<List<User>> {
        return remoteDataSource.fetchUsersRx()
    }
}


// 3. Presentation Layer
// ViewModel: UserViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.launch
import io.reactivex.rxjava3.disposables.CompositeDisposable

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {
    val users = mutableStateListOf<User>()
    private val disposables = CompositeDisposable()

    // Usando Coroutines
    fun fetchUsersCoroutine() {
        viewModelScope.launch {
            val result = getUsersUseCase.getUsersCoroutine()
            users.clear()
            users.addAll(result)
        }
    }

    // Usando RxJava
    fun fetchUsersRx() {
        val disposable = getUsersUseCase.getUsersRx()
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


// 4. UI (Jetpack Compose): UserScreen.kt
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


// 5. MainActivity.kt
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // O ViewModel será injetado automaticamente pelo Hilt
    private val viewModel: UserViewModel by lazy {
        androidx.lifecycle.viewmodel.compose.viewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UserScreen(viewModel)
        }
    }
}