# Android — Estudo Tech Stack

Projeto Android de estudo focado em **Clean Architecture + MVVM**, usado como referência prática de como estruturar um app moderno com Jetpack Compose, Coroutines, injeção de dependência (Hilt), persistência local (Room + DataStore) e comunicação com API REST (Retrofit).

O app exibe uma lista de usuários consumida de uma API pública, com cache local e atualização sob demanda (pull/refresh via botão).

## Sumário

- [Arquitetura](#arquitetura)
- [Stack técnica](#stack-técnica)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Fluxo de dados](#fluxo-de-dados)
- [Como rodar](#como-rodar)
- [Testes](#testes)
- [Observações sobre o Firebase](#observações-sobre-o-firebase)
- [Material de estudo (`examples/`)](#material-de-estudo-examples)

## Arquitetura

O projeto segue **Clean Architecture** com separação em três camadas, aplicando **MVVM** na camada de apresentação:

```
domain/          → regras de negócio, independentes de framework
├── model/       → entidades de domínio (User)
├── repository/  → contratos (interfaces) do repositório
└── usecase/     → casos de uso (GetUsersUseCase)

data/            → implementação de acesso a dados
├── local/       → Room (UserDao, AppDatabase, UserEntity) + DataStore/EncryptedSharedPreferences (StorageManager)
├── remote/      → Retrofit (ApiService, UserDto)
└── repository/  → implementação concreta do UserRepository (mescla local + remoto)

presentation/    → UI e estado (MVVM)
├── UserViewModel.kt   → expõe UserUiState via StateFlow
├── UserUiState.kt     → estado da tela + eventos (UserEvent)
└── UserListScreen.kt  → UI declarativa em Jetpack Compose

di/              → módulos de injeção de dependência (Hilt)
```

A regra de dependência é respeitada: `presentation` e `data` dependem de `domain`, mas `domain` não conhece Android, Room, Retrofit ou Compose — apenas Kotlin puro e coroutines.

### Padrão do repositório (offline-first simples)

`UserRepositoryImpl` implementa uma estratégia **single source of truth**:

1. `getUsers()` sempre lê do Room (`UserDao`), expondo um `Flow<List<User>>` reativo.
2. `refreshUsers()` busca dados remotos via `ApiService` e grava no Room (`REPLACE` por id).
3. A UI observa o Flow do banco — qualquer atualização no cache reflete automaticamente na tela, sem acoplar a UI à rede.

## Stack técnica

| Categoria | Tecnologia |
|---|---|
| Linguagem | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.09.00) + Material 3 |
| Navegação | Navigation Compose |
| Arquitetura | Clean Architecture + MVVM |
| Assincronismo | Kotlin Coroutines + Flow (StateFlow na ViewModel); RxJava/RxKotlin também incluídos como dependência de estudo |
| Injeção de dependência | Hilt (Dagger) |
| Rede | Retrofit 2 + OkHttp (logging interceptor) + Gson |
| Persistência local | Room (cache de usuários) |
| Preferências | DataStore Preferences + EncryptedSharedPreferences (`androidx.security.crypto`) |
| Observabilidade | Firebase Analytics / Crashlytics (dependências presentes, inicialização desativada — ver [seção abaixo](#observações-sobre-o-firebase)) |
| Testes unitários | JUnit4 + Coroutines Test |
| Testes instrumentados | AndroidJUnit4 + Espresso + Room in-memory |

Gerenciamento de dependências via **Gradle Version Catalog** (`gradle/libs.versions.toml`).

- `compileSdk` / `targetSdk`: 34
- `minSdk`: 28
- Gradle: 8.13 · AGP: 8.13.2

## Estrutura do projeto

```
app/src/main/java/com/example/android/
├── ExampleApplication.kt        # @HiltAndroidApp
├── MainActivity.kt              # NavHost + Compose entry point
├── di/AppModule.kt              # Providers: OkHttp, Retrofit, Room, Repository, UseCase
├── domain/
│   ├── model/User.kt
│   ├── repository/UserRepository.kt
│   └── usecase/GetUsersUseCase.kt
├── data/
│   ├── local/ (AppDatabase, UserDao, UserEntity, StorageManager)
│   ├── remote/ (ApiService, UserDto)
│   └── repository/UserRepositoryImpl.kt
├── presentation/
│   ├── UserViewModel.kt
│   ├── UserUiState.kt
│   └── UserListScreen.kt
└── ui/theme/ (Color, Theme, Type)

app/src/test/          # testes unitários (JVM, sem dispositivo/emulador)
app/src/androidTest/   # testes instrumentados (Room em memória)
examples/               # material de estudo comparando abordagens (não compilado no build)
```

## Fluxo de dados

```
UserListScreen (Compose)
      │  observa
      ▼
UserViewModel (StateFlow<UserUiState>)
      │  chama
      ▼
GetUsersUseCase
      │  delega
      ▼
UserRepository (interface) ──► UserRepositoryImpl
                                   ├── UserDao (Room)      → fonte de verdade / cache
                                   └── ApiService (Retrofit) → https://jsonplaceholder.typicode.com/users
```

Ao abrir a tela, o `UserViewModel` já dispara `refresh()` automaticamente e passa a observar o Flow do repositório; o botão flutuante "Refresh" permite forçar uma nova busca remota.

## Como rodar

### Pré-requisitos

- Android Studio (Ladybug ou mais recente recomendado)
- JDK 11+
- SDK Android 34 instalado

### Passos

```bash
git clone <url-do-repositorio>
cd Android
./gradlew assembleDebug
```

Ou abra o projeto diretamente no Android Studio e rode a configuração padrão `app` em um emulador/dispositivo com API 28+.

> O app consome a API pública [jsonplaceholder.typicode.com](https://jsonplaceholder.typicode.com/users) — é necessária conexão com a internet (permissão `INTERNET` já declarada no manifest).

## Testes

**Testes unitários** (rodam na JVM, sem emulador):

```bash
./gradlew test
```

Cobrem:
- Mapeamento `UserEntity` ↔ `User` ↔ `UserDto`
- `UserRepositoryImpl` (cache, refresh, e comportamento quando a API falha)
- `GetUsersUseCaseImpl` (delegação ao repositório)
- `UserViewModel` (estado inicial, refresh, tratamento de erro de rede e de stream)

**Testes instrumentados** (requerem emulador/dispositivo):

```bash
./gradlew connectedAndroidTest
```

Cobrem o `UserDao` com um banco Room em memória (inserção e substituição por id).

## Observações sobre o Firebase

O `google-services.json` versionado contém chaves de exemplo. Para evitar crash na inicialização automática do Firebase com credenciais inválidas, o `FirebaseInitProvider` é explicitamente removido no `AndroidManifest.xml`:

```xml
<provider
    android:name="com.google.firebase.provider.FirebaseInitProvider"
    android:authorities="${applicationId}.firebaseinitprovider"
    android:exported="false"
    android:initOrder="100"
    tools:node="remove" />
```

As dependências do Firebase (Analytics/Crashlytics) permanecem no `build.gradle.kts` apenas como referência de configuração — substitua o `google-services.json` por um projeto Firebase real antes de reativar a inicialização.

## Material de estudo (`examples/`)

A pasta `examples/` não faz parte do build do app (arquivos com esboços/pseudocódigo em Kotlin) e serve como material comparativo de estudo:

- `android_mvvm_com_clean_arch.kt` — estrutura de pastas e trechos de código no padrão MVVM **com** Clean Architecture (o mesmo modelo aplicado neste projeto).
- `android_mvvm_sem_clean_arch.kt` — o mesmo caso de uso implementado **sem** separação em camadas, para efeito de comparação.
- `ExampleTests.kt` — exemplos de estrutura de testes unitários para os dois cenários acima.

## Licença

Projeto de estudo pessoal, sem licença específica definida.
