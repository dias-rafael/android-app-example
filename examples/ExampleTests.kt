android_mvvm_com_clean_arch/
├── src/
│   ├── main/           # código de produção
│   └── test/           # testes unitários
│       ├── domain/
│       │   ├── usecase/
│       │   │   └── GetUsersUseCaseTest.kt
│       ├── data/
│       │   ├── repository/
│       │   │   └── UserRepositoryImplTest.kt
│       │   ├── datasource/
│       │   │   └── UserRemoteDataSourceImplTest.kt
│       ├── presentation/
│       │   ├── viewmodel/
│       │   │   └── UserViewModelTest.kt
│       │   ├── ui/
│       │   │   └── UserScreenTest.kt
│       └── integration/
│           └── IntegrationTest.kt



// Example unit tests for Clean Architecture layers

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import io.reactivex.rxjava3.observers.TestObserver

// Domain Layer: UseCase Test
class GetUsersUseCaseTest {
    private val fakeRepository = object : UserRepository {
        override suspend fun getUsersCoroutine() = listOf(User(1, "Test"))
        override fun getUsersRx() = io.reactivex.rxjava3.core.Observable.just(listOf(User(1, "Test")))
    }
    private val useCase = GetUsersUseCaseImpl(fakeRepository)

    @Test
    fun `getUsersCoroutine returns users`() = runBlocking {
        val users = useCase.getUsersCoroutine()
        assertEquals(1, users.size)
        assertEquals("Test", users[0].name)
    }

    @Test
    fun `getUsersRx returns users`() {
        val testObserver = TestObserver<List<User>>()
        useCase.getUsersRx().subscribe(testObserver)
        testObserver.assertValue { it.size == 1 && it[0].name == "Test" }
    }
}

// Data Layer: DataSource Test
class UserRemoteDataSourceImplTest {
    private val dataSource = UserRemoteDataSourceImpl()

    @Test
    fun `fetchUsers returns correct data`() = runBlocking {
        val users = dataSource.fetchUsers()
        assertEquals(2, users.size)
        assertEquals("Alice", users[0].name)
    }

    @Test
    fun `fetchUsersRx returns correct data`() {
        val testObserver = TestObserver<List<User>>()
        dataSource.fetchUsersRx().subscribe(testObserver)
        testObserver.assertValue { it.size == 2 && it[0].name == "Alice" }
    }
}

// Data Layer: Repository Test (usando DataSource)
class UserRepositoryImplTest {
    private val fakeDataSource = object : UserRemoteDataSource {
        override suspend fun fetchUsers() = listOf(User(10, "RepoTest"))
        override fun fetchUsersRx() = io.reactivex.rxjava3.core.Observable.just(listOf(User(10, "RepoTest")))
    }
    private val repository = UserRepositoryImpl(fakeDataSource)

    @Test
    fun `getUsersCoroutine returns correct data`() = runBlocking {
        val users = repository.getUsersCoroutine()
        assertEquals(1, users.size)
        assertEquals("RepoTest", users[0].name)
    }

    @Test
    fun `getUsersRx returns correct data`() {
        val testObserver = TestObserver<List<User>>()
        repository.getUsersRx().subscribe(testObserver)
        testObserver.assertValue { it.size == 1 && it[0].name == "RepoTest" }
    }
}

// Presentation Layer: ViewModel Test
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule

class UserViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val fakeUseCase = object : GetUsersUseCase {
        override suspend fun getUsersCoroutine() = listOf(User(2, "VMTest"))
        override fun getUsersRx() = io.reactivex.rxjava3.core.Observable.just(listOf(User(2, "VMTest")))
    }
    private val viewModel = UserViewModel(fakeUseCase)

    @Test
    fun `fetchUsersCoroutine updates users`() = runBlocking {
        viewModel.fetchUsersCoroutine()
        assertEquals(1, viewModel.users.size)
        assertEquals("VMTest", viewModel.users[0].name)
    }

    @Test
    fun `fetchUsersRx updates users`() {
        viewModel.fetchUsersRx()
        assertEquals(1, viewModel.users.size)
        assertEquals("VMTest", viewModel.users[0].name)
    }
}

// UI Layer: Compose Test
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule

class UserScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun userScreen_displaysUsers() {
        val fakeViewModel = object : UserViewModel(object : GetUsersUseCase {
            override suspend fun getUsersCoroutine() = listOf(User(3, "ComposeTest"))
            override fun getUsersRx() = io.reactivex.rxjava3.core.Observable.just(listOf(User(3, "ComposeTest")))
        }) {
            init {
                users.add(User(3, "ComposeTest"))
            }
        }
        composeRule.setContent {
            UserScreen(fakeViewModel)
        }
        composeRule.onNodeWithText("3: ComposeTest").assertExists()
    }
}

// Integration Test: Repository + DataSource + UseCase + ViewModel + UI
class IntegrationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun fullFlow_displaysUserFromDataSource() {
        val dataSource = UserRemoteDataSourceImpl()
        val repository = UserRepositoryImpl(dataSource)
        val useCase = GetUsersUseCaseImpl(repository)
        val viewModel = UserViewModel(useCase)

        // Simula busca de usuários
        runBlocking { viewModel.fetchUsersCoroutine() }

        composeRule.setContent {
            UserScreen(viewModel)
        }
        composeRule.onNodeWithText("1: Alice").assertExists()
        composeRule.onNodeWithText("2: Bob").assertExists()
    }
}
