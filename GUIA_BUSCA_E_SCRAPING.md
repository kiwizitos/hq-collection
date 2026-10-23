# Guia Completo: Busca e Web Scraping no SeboKabo

## 📚 Índice

1. [Visão Geral da Arquitetura](#visão-geral-da-arquitetura)
2. [Componentes Principais](#componentes-principais)
3. [Passo a Passo da Implementação](#passo-a-passo-da-implementação)
4. [Boas Práticas e Otimizações](#boas-práticas-e-otimizações)
5. [Tratamento de Erros](#tratamento-de-erros)
6. [Exemplo de Uso Completo](#exemplo-de-uso-completo)
7. [Melhorias para Implementações Futuras](#melhorias-para-implementações-futuras)

---

## 🎯 Visão Geral da Arquitetura

O sistema de busca e scraping do SeboKabo segue a **Clean Architecture** com separação clara de responsabilidades:

```
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer (Compose)                      │
│  SearchScreen.kt │ CoversListScreen.kt │ DetailsScreen.kt   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    ViewModel Layer                           │
│                  SearchViewModel.kt                          │
│  • Gerencia estados (UiState)                               │
│  • Coordena casos de uso                                     │
│  • Mantém scroll position                                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                   Domain Layer (Use Cases)                   │
│  • SearchComicsUseCase                                       │
│  • LoadNextPageUseCase                                       │
│  • GetSeriesCoversUseCase                                    │
│  • LoadNextCoversPageUseCase                                 │
│  • GetComicDetailsUseCase                                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                      Data Layer                              │
│  ┌──────────────────┬──────────────────┬─────────────────┐ │
│  │  DataSource      │     Parser       │    Service      │ │
│  │  (Repository)    │   (Jsoup HTML)   │   (Retrofit)    │ │
│  └──────────────────┴──────────────────┴─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Princípios Aplicados:

- **Single Responsibility**: Cada classe tem uma única responsabilidade
- **Dependency Inversion**: Dependência de abstrações (interfaces) não implementações
- **Separation of Concerns**: UI não conhece detalhes de scraping
- **Testability**: Componentes isolados e mockáveis

---

## 🔧 Componentes Principais

### 1. **GuiaQuadrinhosService** (Retrofit Interface)

**Responsabilidade**: Fazer requisições HTTP ao site

```kotlin
interface GuiaQuadrinhosService {
    // GET simples para buscar HTML
    @GET
    suspend fun getPageHtml(@Url url: String): String

    // POST para paginação ASP.NET (ViewState)
    @FormUrlEncoded
    @POST
    suspend fun postPageNavigation(
        @Url url: String,
        @Field("__VIEWSTATE") viewState: String,
        @Field("__VIEWSTATEGENERATOR") viewStateGenerator: String = "EEB05935",
        @Field("__VIEWSTATEENCRYPTED") viewStateEncrypted: String = "",
        @Field("__EVENTVALIDATION") eventValidation: String,
        @Field("__EVENTTARGET") eventTarget: String,
        @Field("__EVENTARGUMENT") eventArgument: String = ""
    ): String
}
```

**Características**:
- Usa `ScalarsConverterFactory` para retornar HTML como String
- Suporta GET e POST
- POST é necessário para navegação em sites ASP.NET

---

### 2. **GuiaQuadrinhosParser** (Jsoup Parser)

**Responsabilidade**: Extrair dados estruturados do HTML

```kotlin
object GuiaQuadrinhosParser {
    // Parse lista de séries + paginação
    fun parseSeriesTableWithPagination(html: String): SeriesSearchResult

    // Parse lista de capas + paginação
    fun parseCoverListWithPagination(html: String, seriesTitle: String): CoversSearchResult

    // Parse detalhes de um quadrinho específico
    fun parseDetails(html: String, url: String): ComicDetails?

    // Extrai informações de paginação ASP.NET
    fun extractPaginationInfo(html: String): PaginationInfo
}
```

**Técnicas de Parsing**:

#### a) Seleção de Elementos:
```kotlin
// Selecionar por ID
val title = doc.selectFirst("span#nome_titulo_lb")?.text()

// Selecionar por classe CSS
val covers = doc.select("div.Lista_album_imagem_colecao li")

// Selecionar por atributo
val links = doc.select("a[href*=/edicao/]")

// Selecionar com contains
val nextButton = doc.select("a.next_last:contains(>)").firstOrNull()
```

#### b) Extração de ViewState (ASP.NET):
```kotlin
val viewState = doc.selectFirst("input#__VIEWSTATE")?.attr("value")
val eventValidation = doc.selectFirst("input#__EVENTVALIDATION")?.attr("value")
```

#### c) Regex para Extração de Dados:
```kotlin
// Extrair número total de resultados: "Itens: 31 - 60 de 1971"
val match = Regex("de\\s+(\\d+)").find(pageInfoText)
val totalResults = match?.groupValues?.get(1)?.toIntOrNull() ?: 0

// Extrair eventTarget do JavaScript
val targetMatch = Regex("__doPostBack\\('([^']+)'").find(onclickAttr)
val eventTarget = targetMatch?.groupValues?.get(1)
```

#### d) Limpeza de URLs Relativas:
```kotlin
val relativeLink = linkElement.attr("href").replace(Regex("^\\.\\./"), "")
val fullUrl = "http://www.guiadosquadrinhos.com/$relativeLink"
```

---

### 3. **GuiaQuadrinhosDataSource** (Repository Implementation)

**Responsabilidade**: Coordenar requisições e parsing + rate limiting

```kotlin
@Singleton
class GuiaQuadrinhosDataSource @Inject constructor(
    private val service: GuiaQuadrinhosService
) : ComicDataSource {

    companion object {
        private const val DELAY_BETWEEN_REQUESTS_MS = 3000L
        private var lastRequestTime = 0L
    }

    // Rate limiting para respeitar o servidor
    private suspend fun applyRateLimiting() {
        val now = System.currentTimeMillis()
        val timeSinceLastRequest = now - lastRequestTime

        if (timeSinceLastRequest < DELAY_BETWEEN_REQUESTS_MS) {
            val delayNeeded = DELAY_BETWEEN_REQUESTS_MS - timeSinceLastRequest
            delay(delayNeeded)
        }

        lastRequestTime = System.currentTimeMillis()
    }

    override suspend fun searchSeries(query: String): Result<SeriesSearchResult> {
        return try {
            applyRateLimiting()
            
            val queryEncoded = query.replace(" ", "%20")
            val url = "http://www.guiadosquadrinhos.com/titulos/$queryEncoded"
            
            val html = service.getPageHtml(url)
            val searchResult = GuiaQuadrinhosParser.parseSeriesTableWithPagination(html)
            
            Result.success(searchResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Features Importantes**:

1. **Rate Limiting**: 3 segundos entre requisições
2. **Error Handling**: Try-catch com Result pattern
3. **URL Encoding**: Tratamento de espaços e caracteres especiais
4. **Logging**: Logs detalhados para debug

---

### 4. **SearchViewModel** (State Management)

**Responsabilidade**: Gerenciar estado da UI e coordenar use cases

```kotlin
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchComicsUseCase: SearchComicsUseCase,
    private val loadNextPageUseCase: LoadNextPageUseCase,
    // ... outros use cases
) : ViewModel() {

    // Estados observáveis
    private val _searchState = MutableStateFlow<UiState<PaginatedSearchResult>>(UiState.Idle)
    val searchState: StateFlow<UiState<PaginatedSearchResult>> = _searchState.asStateFlow()

    // Preservar posição de scroll durante paginação
    var searchScrollIndex: Int = 0
    var searchScrollOffset: Int = 0

    // Buscar séries
    fun searchSeries(query: String) {
        viewModelScope.launch {
            _searchState.value = UiState.Loading
            currentSearchQuery = query

            val result = searchComicsUseCase(SearchComicsParams(query))

            _searchState.value = result.fold(
                onSuccess = { searchResult ->
                    UiState.Success(PaginatedSearchResult(
                        series = searchResult.series,
                        paginationInfo = searchResult.paginationInfo
                    ))
                },
                onFailure = { error ->
                    UiState.Error(ErrorHandler.getErrorMessage(error), error)
                }
            )
        }
    }

    // Carregar próxima página (paginação infinita)
    fun loadNextSearchPage() {
        viewModelScope.launch {
            val currentState = _searchState.value
            if (currentState !is UiState.Success) return@launch

            val paginationInfo = currentState.data.paginationInfo
            if (!paginationInfo.hasNextPage) return@launch

            _searchState.value = UiState.LoadingMore(currentState.data)

            val result = loadNextPageUseCase(LoadNextPageParams(
                query = currentSearchQuery,
                viewState = paginationInfo.viewState!!,
                eventValidation = paginationInfo.eventValidation!!,
                eventTarget = paginationInfo.eventTarget!!
            ))

            _searchState.value = result.fold(
                onSuccess = { searchResult ->
                    UiState.Success(PaginatedSearchResult(
                        series = currentState.data.series + searchResult.series, // Merge
                        paginationInfo = searchResult.paginationInfo
                    ))
                },
                onFailure = { error ->
                    UiState.Error(ErrorHandler.getErrorMessage(error), error)
                }
            )
        }
    }
}
```

**Padrão UiState**:
```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class LoadingMore<T>(val currentData: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}
```

---

### 5. **Models de Dados**

#### PaginationInfo (Estado de Paginação)
```kotlin
data class PaginationInfo(
    val currentPage: Int = 1,
    val totalResults: Int = 0,
    val hasNextPage: Boolean = false,
    val viewState: String? = null,          // ASP.NET ViewState
    val eventValidation: String? = null,    // ASP.NET EventValidation
    val eventTarget: String? = null         // ASP.NET EventTarget
)
```

#### SerieResult (Resultado de Busca)
```kotlin
data class SerieResult(
    val title: String,
    val relativeLink: String,
    val publisher: String,
    val originalPublisher: String,
    val year: String,
    val issueCount: String
)
```

#### ComicDetails (Detalhes Completos)
```kotlin
data class ComicDetails(
    val title: String,
    val publishedIn: String?,
    val month: String?,
    val year: String?,
    val publisher: String?,
    val format: String?,
    val coverPrice: String?,
    val synopsis: String?,
    val coverUrl: String?,
    val writer: String?,
    val artist: String?,
    val colorist: String?,
    val coverArtist: String?,
    val translator: String?
)
```

---

## 🚀 Passo a Passo da Implementação

### Etapa 1: Configurar Dependências

**build.gradle.kts (Module: app)**
```kotlin
dependencies {
    // Retrofit para HTTP
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Jsoup para parsing HTML
    implementation("org.jsoup:jsoup:1.16.1")
    
    // Hilt para injeção de dependência
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

---

### Etapa 2: Configurar Retrofit com OkHttp

**NetworkModule.kt**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    // User-Agent respeitoso
                    .addHeader("User-Agent", "SeboKabo/1.0 (contact@sebokabo.com)")
                    .addHeader("Accept", "text/html,application/xhtml+xml")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://www.guiadosquadrinhos.com/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create()) // ← Importante!
            .build()
    }

    @Provides
    @Singleton
    fun provideGuiaQuadrinhosService(retrofit: Retrofit): GuiaQuadrinhosService {
        return retrofit.create(GuiaQuadrinhosService::class.java)
    }
}
```

**⚠️ Atenção**: `ScalarsConverterFactory` é essencial para receber HTML como String.

---

### Etapa 3: Criar Interface de Dados (Repository)

**ComicDataSource.kt (Interface/Contract)**
```kotlin
interface ComicDataSource {
    suspend fun searchSeries(query: String): Result<SeriesSearchResult>
    
    suspend fun searchSeriesNextPage(
        query: String,
        viewState: String,
        eventValidation: String,
        eventTarget: String
    ): Result<SeriesSearchResult>
    
    suspend fun getSeriesCovers(
        seriesUrl: String,
        seriesTitle: String
    ): Result<CoversSearchResult>
    
    suspend fun getSeriesCoversNextPage(
        seriesUrl: String,
        seriesTitle: String,
        viewState: String,
        eventValidation: String,
        eventTarget: String
    ): Result<CoversSearchResult>
    
    suspend fun getComicDetails(url: String): Result<ComicDetails>
}
```

**Vantagens da Interface**:
- Permite trocar implementação (mock para testes, outro site, etc.)
- Facilita testes unitários
- Segue o princípio de inversão de dependência

---

### Etapa 4: Implementar Parser com Jsoup

**Exemplo Real: Parser de Tabela de Séries**

```kotlin
fun parseSeriesTable(html: String): List<SerieResult> {
    val results = mutableListOf<SerieResult>()
    
    try {
        val doc: Document = Jsoup.parse(html)
        
        // Selecionar linhas da tabela (excluindo header)
        val linhas = doc.select("table#tlbProfile tr, table.datatable tr")
            .filter { it.select("td").isNotEmpty() }
        
        for (linha in linhas) {
            try {
                val cells = linha.select("td")
                if (cells.size < 5) continue
                
                // Extrair dados de cada coluna
                val linkElement = cells[0].selectFirst("a") ?: continue
                val title = linkElement.text().trim()
                val relativeLink = linkElement.attr("href")
                    .replace(Regex("^\\.\\./"), "") // Remove "../" do início
                
                val publisher = cells[1].text().trim()
                val originalPublisher = cells[2].text().trim()
                val year = cells[3].text().trim()
                val issueCount = cells[4].text().trim()
                
                results.add(SerieResult(
                    title = title,
                    relativeLink = relativeLink,
                    publisher = publisher,
                    originalPublisher = originalPublisher,
                    year = year,
                    issueCount = issueCount
                ))
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao parsear série", e)
                // Continue parseando outras linhas
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Erro ao parsear lista de séries", e)
    }
    
    Log.d(TAG, "Parseadas ${results.size} séries")
    return results
}
```

**Dicas de Parsing**:

1. **Sempre validar elementos antes de acessar**:
   ```kotlin
   val element = doc.selectFirst("span#title") ?: return null
   ```

2. **Use try-catch individual para elementos opcionais**:
   ```kotlin
   val synopsis = try {
       doc.selectFirst("div.synopsis")?.text()
   } catch (e: Exception) {
       null
   }
   ```

3. **Normalize dados extraídos**:
   ```kotlin
   val year = yearText.trim().replace(Regex("[^0-9]"), "")
   ```

4. **Use seletores CSS robustos**:
   ```kotlin
   // Ruim (muito específico, pode quebrar)
   doc.select("div.container > div:nth-child(2) > span")
   
   // Bom (mais genérico e resistente)
   doc.select("span.title, h1.title").firstOrNull()
   ```

---

### Etapa 5: Implementar Paginação ASP.NET

**Como funciona paginação ASP.NET**:

1. **Primeira página**: GET simples
2. **Próximas páginas**: POST com ViewState

**Exemplo de Extração de ViewState**:

```kotlin
fun extractPaginationInfo(html: String): PaginationInfo {
    val doc: Document = Jsoup.parse(html)
    
    // 1. Extrair ViewState (hidden input)
    val viewState = doc.selectFirst("input#__VIEWSTATE")?.attr("value")
    val eventValidation = doc.selectFirst("input#__EVENTVALIDATION")?.attr("value")
    
    // 2. Verificar se tem próxima página
    val nextButton = doc.select("a.next_last:contains(>)").firstOrNull()
    val hasNextPage = nextButton != null
    
    // 3. Extrair EventTarget do botão "próxima"
    var eventTarget: String? = null
    if (hasNextPage && nextButton != null) {
        val onclickAttr = nextButton.attr("href")
        // href="javascript:__doPostBack('ctl00$MainContent$dataPager$ctl02$ctl00','')"
        val targetMatch = Regex("__doPostBack\\('([^']+)'").find(onclickAttr)
        eventTarget = targetMatch?.groupValues?.get(1)
    }
    
    // 4. Extrair página atual
    val currentPageElement = doc.selectFirst("span.current_page")
    val currentPage = currentPageElement?.text()?.toIntOrNull() ?: 1
    
    // 5. Extrair total de resultados
    // Formato: "Itens: 31 - 60 de 1971"
    val pageInfoText = doc.select("span[id*=dataPageDisplayNumberOfPages]").text()
    val totalMatch = Regex("de\\s+(\\d+)").find(pageInfoText)
    val totalResults = totalMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
    
    return PaginationInfo(
        currentPage = currentPage,
        totalResults = totalResults,
        hasNextPage = hasNextPage,
        viewState = viewState,
        eventValidation = eventValidation,
        eventTarget = eventTarget
    )
}
```

**Como usar ViewState para próxima página**:

```kotlin
suspend fun searchSeriesNextPage(
    query: String,
    viewState: String,
    eventValidation: String,
    eventTarget: String
): Result<SeriesSearchResult> {
    val url = "http://www.guiadosquadrinhos.com/titulos/$query"
    
    // POST com campos ASP.NET
    val html = service.postPageNavigation(
        url = url,
        viewState = viewState,
        eventValidation = eventValidation,
        eventTarget = eventTarget
    )
    
    return Result.success(GuiaQuadrinhosParser.parseSeriesTableWithPagination(html))
}
```

---

### Etapa 6: Implementar Use Cases

**Use Case Pattern**:
```kotlin
// Base abstrata
abstract class UseCase<in Params, out Result> {
    abstract suspend fun execute(params: Params): Result
    
    suspend operator fun invoke(params: Params): Result {
        return execute(params)
    }
}

// Implementação concreta
class SearchComicsUseCase(
    private val dataSource: ComicDataSource
) : UseCase<SearchComicsParams, Result<SeriesSearchResult>>() {

    override suspend fun execute(params: SearchComicsParams): Result<SeriesSearchResult> {
        // Validação de entrada
        if (params.query.isBlank()) {
            return Result.failure(IllegalArgumentException("Query cannot be empty"))
        }
        
        if (params.query.length < 2) {
            return Result.failure(IllegalArgumentException("Query must have at least 2 characters"))
        }
        
        // Delegação para data source
        return dataSource.searchSeries(params.query)
    }
}

// Parâmetros tipados
data class SearchComicsParams(val query: String)
```

**Vantagens**:
- Validação centralizada
- Lógica de negócio isolada
- Testável independentemente
- Reutilizável

---

### Etapa 7: Criar ViewModel com UiState

**UiState Pattern** (Estado da UI):

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class LoadingMore<T>(val currentData: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
    
    fun getDataOrNull(): T? = if (this is Success) data else null
    fun isLoading(): Boolean = this is Loading || this is LoadingMore
}
```

**ViewModel Completo**:

```kotlin
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchComicsUseCase: SearchComicsUseCase,
    private val loadNextPageUseCase: LoadNextPageUseCase
) : ViewModel() {

    private val _searchState = MutableStateFlow<UiState<PaginatedSearchResult>>(UiState.Idle)
    val searchState: StateFlow<UiState<PaginatedSearchResult>> = _searchState.asStateFlow()

    private var currentSearchQuery: String = ""
    var searchScrollIndex: Int = 0
    var searchScrollOffset: Int = 0

    fun searchSeries(query: String) {
        viewModelScope.launch {
            _searchState.value = UiState.Loading
            currentSearchQuery = query

            val result = searchComicsUseCase(SearchComicsParams(query))

            _searchState.value = result.fold(
                onSuccess = { searchResult ->
                    if (searchResult.series.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(PaginatedSearchResult(
                            series = searchResult.series,
                            paginationInfo = searchResult.paginationInfo
                        ))
                    }
                },
                onFailure = { error ->
                    UiState.Error(ErrorHandler.getErrorMessage(error), error)
                }
            )
        }
    }

    fun loadNextSearchPage() {
        viewModelScope.launch {
            val currentState = _searchState.value
            if (currentState !is UiState.Success) return@launch

            val paginationInfo = currentState.data.paginationInfo
            
            // Validações
            if (!paginationInfo.hasNextPage) return@launch
            if (paginationInfo.viewState.isNullOrBlank()) return@launch
            if (paginationInfo.eventValidation.isNullOrBlank()) return@launch
            if (paginationInfo.eventTarget.isNullOrBlank()) return@launch

            // Mostrar loading sem perder dados atuais
            _searchState.value = UiState.LoadingMore(currentState.data)

            val result = loadNextPageUseCase(LoadNextPageParams(
                query = currentSearchQuery,
                viewState = paginationInfo.viewState,
                eventValidation = paginationInfo.eventValidation,
                eventTarget = paginationInfo.eventTarget
            ))

            _searchState.value = result.fold(
                onSuccess = { newPage ->
                    UiState.Success(PaginatedSearchResult(
                        series = currentState.data.series + newPage.series, // Merge!
                        paginationInfo = newPage.paginationInfo // Atualiza estado paginação
                    ))
                },
                onFailure = { error ->
                    UiState.Error(ErrorHandler.getErrorMessage(error), error)
                }
            )
        }
    }
}
```

---

### Etapa 8: Implementar UI com Jetpack Compose

**SearchScreen.kt**:

```kotlin
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = hiltViewModel(),
    onSeriesClick: (SerieResult) -> Unit
) {
    var queryText by remember { mutableStateOf("") }
    val searchState by searchViewModel.searchState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra de busca
        SearchBar(
            query = queryText,
            onQueryChange = { queryText = it },
            onSearch = { 
                if (queryText.isNotBlank()) {
                    searchViewModel.searchSeries(queryText)
                }
            }
        )

        // Estados da UI
        when (val state = searchState) {
            is UiState.Idle -> {
                EmptyState(message = "Digite algo para buscar")
            }
            is UiState.Loading -> {
                LoadingState()
            }
            is UiState.Success -> {
                SearchResults(
                    series = state.data.series,
                    hasNextPage = state.data.paginationInfo.hasNextPage,
                    onLoadMore = { searchViewModel.loadNextSearchPage() },
                    onSeriesClick = onSeriesClick
                )
            }
            is UiState.LoadingMore -> {
                SearchResults(
                    series = state.currentData.series,
                    hasNextPage = state.currentData.paginationInfo.hasNextPage,
                    onLoadMore = { searchViewModel.loadNextSearchPage() },
                    onSeriesClick = onSeriesClick,
                    isLoadingMore = true
                )
            }
            is UiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { searchViewModel.searchSeries(queryText) }
                )
            }
            is UiState.Empty -> {
                EmptyState(message = "Nenhum resultado encontrado")
            }
        }
    }
}
```

**Lista com Paginação Infinita**:

```kotlin
@Composable
fun SearchResults(
    series: List<SerieResult>,
    hasNextPage: Boolean,
    onLoadMore: () -> Unit,
    onSeriesClick: (SerieResult) -> Unit,
    isLoadingMore: Boolean = false,
    scrollIndex: Int = 0,
    scrollOffset: Int = 0,
    onScrollPositionChange: (Int, Int) -> Unit = { _, _ -> }
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = scrollIndex,
        initialFirstVisibleItemScrollOffset = scrollOffset
    )

    // Salvar posição de scroll
    LaunchedEffect(listState) {
        snapshotFlow { 
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset 
        }.collect { (index, offset) ->
            onScrollPositionChange(index, offset)
        }
    }

    LazyColumn(state = listState) {
        // Items da lista
        items(
            items = series,
            key = { it.relativeLink }
        ) { serie ->
            SeriesItemCard(
                serie = serie,
                onClick = { onSeriesClick(serie) }
            )
        }

        // Loading indicator no final
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Trigger para carregar mais
        if (hasNextPage && !isLoadingMore) {
            item {
                // Detecta quando chega no final
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
```

---

## 🎯 Boas Práticas e Otimizações

### 1. **Rate Limiting Respeitoso**

**Por que é importante**:
- Evita sobrecarga no servidor
- Previne bloqueio de IP
- Demonstra responsabilidade ética

**Implementação**:
```kotlin
class GuiaQuadrinhosDataSource {
    companion object {
        private const val DELAY_BETWEEN_REQUESTS_MS = 3000L // 3 segundos
        private var lastRequestTime = 0L
    }

    private suspend fun applyRateLimiting() {
        val now = System.currentTimeMillis()
        val timeSinceLastRequest = now - lastRequestTime

        if (timeSinceLastRequest < DELAY_BETWEEN_REQUESTS_MS) {
            val delayNeeded = DELAY_BETWEEN_REQUESTS_MS - timeSinceLastRequest
            Log.d(TAG, "Aguardando ${delayNeeded}ms antes da próxima requisição...")
            delay(delayNeeded)
        }

        lastRequestTime = System.currentTimeMillis()
    }

    override suspend fun searchSeries(query: String): Result<SeriesSearchResult> {
        applyRateLimiting() // ← Sempre chamar antes de requisição
        // ... resto do código
    }
}
```

**Melhorias Futuras**:
- Implementar exponential backoff em caso de erro 429 (Too Many Requests)
- Cache local para evitar requisições duplicadas
- Queue de requisições com prioridade

---

### 2. **User-Agent Respeitoso**

**Sempre identifique seu app**:
```kotlin
.addInterceptor { chain ->
    val request = chain.request().newBuilder()
        .header("User-Agent", "SeboKabo/1.0 (contact@sebokabo.com)")
        .header("Accept", "text/html,application/xhtml+xml")
        .build()
    chain.proceed(request)
}
```

**Formato recomendado**: `AppName/Version (Contact)`

---

### 3. **Caching Inteligente**

**Cache em Memória (ViewModel)**:
```kotlin
class SearchViewModel {
    private val searchCache = mutableMapOf<String, PaginatedSearchResult>()
    
    fun searchSeries(query: String) {
        // Verificar cache primeiro
        searchCache[query]?.let {
            _searchState.value = UiState.Success(it)
            return
        }
        
        // Se não, buscar da API
        viewModelScope.launch {
            // ... busca normal
        }
    }
}
```

**Cache em Disco (Room Database)**:
```kotlin
@Entity(tableName = "search_cache")
data class SearchCacheEntity(
    @PrimaryKey val query: String,
    val results: String, // JSON serializado
    val timestamp: Long,
    val expiresIn: Long = 24 * 60 * 60 * 1000 // 24 horas
)

// Use Case com cache
class SearchComicsUseCase(
    private val dataSource: ComicDataSource,
    private val cacheDao: SearchCacheDao
) {
    suspend fun execute(params: SearchComicsParams): Result<SeriesSearchResult> {
        // Tentar cache primeiro
        val cached = cacheDao.getSearch(params.query)
        if (cached != null && !cached.isExpired()) {
            return Result.success(cached.toSearchResult())
        }
        
        // Buscar da API
        val result = dataSource.searchSeries(params.query)
        
        // Salvar em cache
        result.onSuccess { searchResult ->
            cacheDao.insert(SearchCacheEntity.from(params.query, searchResult))
        }
        
        return result
    }
}
```

---

### 4. **Error Handling Robusto**

**ErrorHandler centralizado**:
```kotlin
object ErrorHandler {
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> 
                "Sem conexão com a internet"
            
            is SocketTimeoutException -> 
                "Tempo de conexão esgotado. Tente novamente."
            
            is HttpException -> when (throwable.code()) {
                404 -> "Página não encontrada"
                429 -> "Muitas requisições. Aguarde um momento."
                500, 502, 503 -> "Servidor temporariamente indisponível"
                else -> "Erro HTTP ${throwable.code()}"
            }
            
            is IOException -> 
                "Erro de conexão. Verifique sua internet."
            
            is IllegalArgumentException -> 
                throwable.message ?: "Parâmetros inválidos"
            
            else -> 
                throwable.message ?: "Erro desconhecido"
        }
    }
}
```

**Retry com Exponential Backoff**:
```kotlin
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    
    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            if (attempt == maxRetries - 1) throw e
            
            Log.d("Retry", "Tentativa ${attempt + 1} falhou. Aguardando ${currentDelay}ms...")
            delay(currentDelay)
            
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }
    
    throw IllegalStateException("Retry failed")
}

// Uso
val result = retryWithBackoff {
    dataSource.searchSeries(query)
}
```

---

### 5. **Logging Detalhado**

**Diferentes níveis de log**:
```kotlin
object ScraperLogger {
    private const val TAG = "WebScraper"
    
    fun logRequest(url: String) {
        Log.d(TAG, "🌐 Requisição: $url")
    }
    
    fun logResponse(itemCount: Int, page: Int) {
        Log.d(TAG, "✅ Recebido: $itemCount itens (página $page)")
    }
    
    fun logError(operation: String, error: Throwable) {
        Log.e(TAG, "❌ Erro em $operation", error)
    }
    
    fun logParsing(elementType: String, count: Int) {
        Log.v(TAG, "📄 Parseado: $count $elementType")
    }
    
    fun logRateLimit(delayMs: Long) {
        Log.d(TAG, "⏳ Rate limit: aguardando ${delayMs}ms")
    }
}
```

---

### 6. **Preservar Estado Durante Paginação**

**Problema**: Ao carregar mais itens, a lista "pula" e perde scroll position

**Solução**: Salvar e restaurar posição

```kotlin
class SearchViewModel {
    var searchScrollIndex: Int = 0
    var searchScrollOffset: Int = 0
}

@Composable
fun SearchResults(
    scrollIndex: Int,
    scrollOffset: Int,
    onScrollPositionChange: (Int, Int) -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = scrollIndex,
        initialFirstVisibleItemScrollOffset = scrollOffset
    )
    
    // Observar mudanças de scroll
    LaunchedEffect(listState) {
        snapshotFlow { 
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset 
        }.collect { (index, offset) ->
            onScrollPositionChange(index, offset)
        }
    }
    
    LazyColumn(state = listState) {
        // ...
    }
}
```

---

### 7. **Cancelamento de Requisições**

**Problema**: Usuário digita rápido, dispara várias buscas

**Solução**: Debounce + cancelamento automático

```kotlin
class SearchViewModel {
    private var searchJob: Job? = null
    
    fun searchSeries(query: String) {
        // Cancelar busca anterior
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            // Debounce de 500ms
            delay(500)
            
            _searchState.value = UiState.Loading
            
            val result = searchComicsUseCase(SearchComicsParams(query))
            
            _searchState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Erro") }
            )
        }
    }
}
```

**Alternativa com Flow**:
```kotlin
@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    var queryText by remember { mutableStateOf("") }
    
    // Auto-search com debounce
    LaunchedEffect(queryText) {
        snapshotFlow { queryText }
            .debounce(500) // 500ms de debounce
            .filter { it.length >= 2 } // Mínimo 2 caracteres
            .distinctUntilChanged() // Evita buscas duplicadas
            .collect { query ->
                viewModel.searchSeries(query)
            }
    }
}
```

---

### 8. **Parsing Defensivo**

**Sempre use safe calls e valores padrão**:

```kotlin
// ❌ Ruim (pode crashar)
val title = doc.selectFirst("span#title").text()

// ✅ Bom
val title = doc.selectFirst("span#title")?.text() ?: "Título desconhecido"

// ✅ Melhor (com fallback)
val title = doc.selectFirst("span#nome_titulo_lb")?.text()
    ?: doc.selectFirst("h1")?.text()
    ?: doc.selectFirst("title")?.text()
    ?: "Sem título"
```

**Validação de dados extraídos**:
```kotlin
fun parseYear(yearText: String?): String? {
    return yearText
        ?.trim()
        ?.replace(Regex("[^0-9]"), "") // Apenas números
        ?.takeIf { it.length == 4 } // Validar formato
        ?.takeIf { it.toInt() in 1900..2100 } // Validar range
}
```

**Try-catch granular**:
```kotlin
fun parseSeriesTable(html: String): List<SerieResult> {
    val results = mutableListOf<SerieResult>()
    
    try {
        val doc = Jsoup.parse(html)
        val rows = doc.select("table tr")
        
        for (row in rows) {
            try {
                // Parse individual de cada linha
                val serie = parseSerieRow(row)
                results.add(serie)
            } catch (e: Exception) {
                // Log mas continua processando outras linhas
                Log.w(TAG, "Erro ao parsear linha: ${e.message}")
                continue
            }
        }
    } catch (e: Exception) {
        // Erro fatal no parsing
        Log.e(TAG, "Erro ao parsear tabela", e)
        throw e
    }
    
    return results
}
```

---

### 9. **Otimização de Memória**

**Evite carregar imagens muito grandes**:
```kotlin
// Com Coil
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(coverUrl)
        .size(400, 600) // Limitar tamanho
        .crossfade(true)
        .build(),
    contentDescription = null
)
```

**Paginação para evitar listas gigantes**:
```kotlin
// Limitar itens carregados por vez
const val PAGE_SIZE = 30

fun parseSeriesTable(html: String): List<SerieResult> {
    val results = mutableListOf<SerieResult>()
    val rows = doc.select("table tr")
    
    // Processar apenas primeira página
    for (row in rows.take(PAGE_SIZE)) {
        // ...
    }
    
    return results
}
```

---

### 10. **Testes Automatizados**

**Teste de Parser (Unit Test)**:
```kotlin
class GuiaQuadrinhosParserTest {
    
    @Test
    fun `parseSeriesTable should extract series correctly`() {
        // Arrange
        val html = loadTestHtml("series_table.html")
        
        // Act
        val results = GuiaQuadrinhosParser.parseSeriesTable(html)
        
        // Assert
        assertThat(results).hasSize(30)
        assertThat(results.first().title).isEqualTo("Batman")
        assertThat(results.first().publisher).isEqualTo("Panini Comics")
    }
    
    @Test
    fun `extractPaginationInfo should extract ViewState correctly`() {
        // Arrange
        val html = loadTestHtml("page_with_pagination.html")
        
        // Act
        val paginationInfo = GuiaQuadrinhosParser.extractPaginationInfo(html)
        
        // Assert
        assertThat(paginationInfo.hasNextPage).isTrue()
        assertThat(paginationInfo.viewState).isNotNull()
        assertThat(paginationInfo.eventTarget).contains("ctl00")
    }
}
```

**Mock de API para testes**:
```kotlin
class FakeComicDataSource : ComicDataSource {
    private val fakeResults = listOf(
        SerieResult(
            title = "Test Serie 1",
            relativeLink = "/serie/test-1",
            publisher = "Test Publisher",
            originalPublisher = "Original Publisher",
            year = "2020",
            issueCount = "12"
        )
    )
    
    override suspend fun searchSeries(query: String): Result<SeriesSearchResult> {
        delay(500) // Simular latência
        return Result.success(
            SeriesSearchResult(
                series = fakeResults,
                paginationInfo = PaginationInfo()
            )
        )
    }
}
```

---

## ⚠️ Tratamento de Erros

### Principais Erros e Soluções

| Erro | Causa | Solução |
|------|-------|---------|
| `UnknownHostException` | Sem internet | Mostrar mensagem pedindo para verificar conexão |
| `SocketTimeoutException` | Timeout | Aumentar timeout ou retry |
| `HttpException 404` | URL inválida | Validar URLs antes de fazer request |
| `HttpException 429` | Rate limit excedido | Implementar backoff exponencial |
| `HttpException 500-503` | Servidor fora | Implementar retry com delay |
| `NullPointerException` em parsing | HTML mudou | Usar safe calls (?.) e valores padrão |
| `IllegalStateException` | ViewState inválido | Validar campos antes de usar para paginação |

### Implementação de Retry

```kotlin
suspend fun <T> executeWithRetry(
    maxAttempts: Int = 3,
    initialDelay: Long = 1000,
    block: suspend () -> T
): T {
    var currentAttempt = 0
    var currentDelay = initialDelay
    
    while (true) {
        try {
            return block()
        } catch (e: Exception) {
            currentAttempt++
            
            if (currentAttempt >= maxAttempts) {
                throw e
            }
            
            if (e is HttpException && e.code() == 404) {
                // Não retry em 404
                throw e
            }
            
            Log.d(TAG, "Tentativa $currentAttempt falhou. Retry em ${currentDelay}ms")
            delay(currentDelay)
            
            currentDelay *= 2 // Exponential backoff
        }
    }
}
```

---

## 📝 Exemplo de Uso Completo

### Fluxo Completo: Buscar → Ver Capas → Adicionar

```kotlin
// 1. Usuário busca por "Batman"
searchViewModel.searchSeries("Batman")

// 2. Resultados aparecem
// UI observa: searchState -> UiState.Success

// 3. Usuário clica em uma série
onSeriesClick(serieResult)

// 4. Navegar para lista de capas
navController.navigate("covers/${serieResult.relativeLink}/${serieResult.title}")

// 5. Carregar capas da série
searchViewModel.getSeriesCovers(
    seriesUrl = serieResult.relativeLink,
    seriesTitle = serieResult.title
)

// 6. Usuário rola até o final → carregar mais
// LazyColumn detecta e chama:
searchViewModel.loadNextCoversPage()

// 7. Usuário clica em uma capa
onCoverClick(coverItem)

// 8. Navegar para detalhes
navController.navigate("details/${URLEncoder.encode(coverItem.relativeLink)}")

// 9. Carregar detalhes completos
comicViewModel.loadComicDetails(coverItem.relativeLink)

// 10. Adicionar ao catálogo
comicViewModel.addComicFromDetails(
    details = comicDetails,
    collectionType = CollectionType.SHOP
)
```

---

## 🚀 Melhorias para Implementações Futuras

### 1. **Background Sync com WorkManager**

Sincronizar dados em background:

```kotlin
class SyncComicsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val dataSource = // inject
            val result = dataSource.searchSeries("novidades")
            
            // Salvar em cache local
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

// Agendar sync diário
val syncRequest = PeriodicWorkRequestBuilder<SyncComicsWorker>(
    repeatInterval = 1,
    repeatIntervalTimeUnit = TimeUnit.DAYS
).build()

WorkManager.getInstance(context).enqueue(syncRequest)
```

---

### 2. **Parallel Scraping com async/await**

Carregar múltiplas páginas em paralelo:

```kotlin
suspend fun searchMultipleSources(query: String): List<SerieResult> = coroutineScope {
    val results = listOf(
        async { dataSource1.searchSeries(query) },
        async { dataSource2.searchSeries(query) },
        async { dataSource3.searchSeries(query) }
    ).awaitAll()
    
    results.flatMap { it.getOrNull()?.series ?: emptyList() }
}
```

---

### 3. **OCR para Extrair Texto de Imagens**

Usar ML Kit para detectar texto em capas:

```kotlin
val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

suspend fun extractTextFromCover(imageUrl: String): String? {
    val bitmap = loadBitmap(imageUrl)
    val image = InputImage.fromBitmap(bitmap, 0)
    
    return suspendCoroutine { continuation ->
        recognizer.process(image)
            .addOnSuccessListener { text ->
                continuation.resume(text.text)
            }
            .addOnFailureListener { e ->
                continuation.resume(null)
            }
    }
}
```

---

### 4. **GraphQL para APIs Modernas**

Se migrar para uma API própria:

```kotlin
// Schema GraphQL
type Query {
    searchSeries(
        query: String!
        page: Int = 1
        pageSize: Int = 30
    ): SeriesSearchResult!
}

type SeriesSearchResult {
    series: [Serie!]!
    pagination: PaginationInfo!
}

// Cliente Apollo
val client = ApolloClient.Builder()
    .serverUrl("https://api.sebokabo.com/graphql")
    .build()

val response = client.query(SearchSeriesQuery(query = "Batman")).execute()
```

---

### 5. **WebView com JavaScript Injection**

Para sites com conteúdo dinâmico (React/Vue):

```kotlin
webView.evaluateJavascript("""
    (function() {
        const results = [];
        document.querySelectorAll('.serie-item').forEach(item => {
            results.push({
                title: item.querySelector('.title').innerText,
                url: item.querySelector('a').href
            });
        });
        return JSON.stringify(results);
    })();
""") { json ->
    val results = Json.decodeFromString<List<SerieResult>>(json)
}
```

---

### 6. **Proxy Rotation para Evitar Bloqueios**

```kotlin
class ProxyRotatingInterceptor(
    private val proxies: List<Proxy>
) : Interceptor {
    private var currentIndex = 0
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val proxy = proxies[currentIndex % proxies.size]
        currentIndex++
        
        val client = OkHttpClient.Builder()
            .proxy(proxy)
            .build()
        
        return chain.proceed(chain.request())
    }
}
```

---

### 7. **Machine Learning para Detecção de Mudanças**

Treinar modelo para detectar quando HTML muda:

```kotlin
class HtmlStructureDetector {
    fun detectChanges(oldHtml: String, newHtml: String): Boolean {
        val oldStructure = extractStructure(oldHtml)
        val newStructure = extractStructure(newHtml)
        
        return oldStructure != newStructure
    }
    
    private fun extractStructure(html: String): String {
        val doc = Jsoup.parse(html)
        return doc.select("*")
            .joinToString(",") { "${it.tagName()}#${it.id()}.${it.className()}" }
    }
}
```

---

### 8. **Streaming de Resultados**

Enviar resultados conforme são parseados:

```kotlin
fun searchSeriesStream(query: String): Flow<SerieResult> = flow {
    val html = service.getPageHtml(url)
    val doc = Jsoup.parse(html)
    
    doc.select("table tr").forEach { row ->
        val serie = parseSerieRow(row)
        emit(serie) // Emite um por vez
        delay(10) // Throttle
    }
}

// Consumir stream
searchSeriesStream("Batman").collect { serie ->
    // Adicionar à UI incrementalmente
    _results.value = _results.value + serie
}
```

---

### 9. **Differential Sync**

Sincronizar apenas mudanças:

```kotlin
data class SyncMetadata(
    val lastSync: Long,
    val etag: String?,
    val checksum: String
)

suspend fun syncWithDiff(metadata: SyncMetadata): SyncResult {
    val response = service.getPageHtml(
        url = url,
        headers = mapOf("If-None-Match" to metadata.etag)
    )
    
    if (response.code == 304) {
        // Não modificado
        return SyncResult.NotModified
    }
    
    // Calcular diff e aplicar
    val diff = calculateDiff(metadata.checksum, response.body)
    applyDiff(diff)
    
    return SyncResult.Updated(diff.size)
}
```

---

### 10. **Offline-First com Sincronização**

```kotlin
class OfflineFirstRepository(
    private val remoteSource: ComicDataSource,
    private val localCache: ComicDao
) : ComicRepository {
    
    override fun searchSeries(query: String): Flow<Resource<List<SerieResult>>> = flow {
        // 1. Emitir cache imediatamente
        val cached = localCache.searchSeries(query)
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached))
        } else {
            emit(Resource.Loading())
        }
        
        // 2. Buscar da rede
        try {
            val remote = remoteSource.searchSeries(query).getOrThrow()
            
            // 3. Atualizar cache
            localCache.insertSeries(remote.series)
            
            // 4. Emitir dados atualizados
            emit(Resource.Success(remote.series))
        } catch (e: Exception) {
            // Se tem cache, manter; senão, erro
            if (cached.isEmpty()) {
                emit(Resource.Error(e.message ?: "Erro"))
            }
        }
    }
}
```

---

## 📊 Métricas e Monitoramento

### Analytics de Scraping

```kotlin
object ScrapingAnalytics {
    fun logSearchPerformed(query: String, resultCount: Int, duration: Long) {
        Firebase.analytics.logEvent("search_performed") {
            param("search_query", query)
            param("result_count", resultCount.toLong())
            param("duration_ms", duration)
        }
    }
    
    fun logParsingError(url: String, errorType: String) {
        Firebase.analytics.logEvent("parsing_error") {
            param("url", url)
            param("error_type", errorType)
        }
    }
    
    fun logRateLimitHit(delayMs: Long) {
        Firebase.analytics.logEvent("rate_limit_applied") {
            param("delay_ms", delayMs)
        }
    }
}
```

---

## 🎓 Conceitos Importantes

### Clean Architecture
- **Entities**: Modelos de dados (SerieResult, ComicDetails)
- **Use Cases**: Lógica de negócio (SearchComicsUseCase)
- **Interface Adapters**: ViewModels, Presenters
- **Frameworks**: Retrofit, Jsoup, Compose

### SOLID Principles
- **S**ingle Responsibility: Cada classe uma função
- **O**pen/Closed: Extensível via interfaces
- **L**iskov Substitution: Interfaces substituíveis
- **I**nterface Segregation: Interfaces específicas
- **D**ependency Inversion: Depender de abstrações

### Reactive Programming
- **Flow**: Streams de dados
- **StateFlow**: Estado observável
- **SharedFlow**: Eventos compartilhados

---

## 📚 Recursos Adicionais

### Documentação
- [Jsoup Documentation](https://jsoup.org/cookbook/)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)

### Ferramentas
- [SelectorGadget](https://selectorgadget.com/) - Chrome extension para CSS selectors
- [Postman](https://www.postman.com/) - Testar requisições HTTP
- [Charles Proxy](https://www.charlesproxy.com/) - Interceptar tráfego HTTP
- [ScrapingBee](https://www.scrapingbee.com/) - API de scraping profissional

### Ética e Legalidade
- Sempre respeite `robots.txt`
- Implemente rate limiting
- Use User-Agent identificável
- Considere entrar em contato com o site para parceria
- Verifique termos de serviço

---

## ✅ Checklist de Implementação

- [ ] Configurar dependências (Retrofit, Jsoup)
- [ ] Criar modelos de dados
- [ ] Implementar Retrofit Service
- [ ] Criar Parser com Jsoup
- [ ] Implementar Repository/DataSource
- [ ] Adicionar Rate Limiting
- [ ] Criar Use Cases
- [ ] Implementar ViewModel com UiState
- [ ] Criar UI com Compose
- [ ] Implementar paginação
- [ ] Adicionar tratamento de erros
- [ ] Implementar cache
- [ ] Adicionar logging
- [ ] Escrever testes
- [ ] Otimizar performance
- [ ] Documentar código

---

## 🎉 Conclusão

Este guia apresenta uma implementação completa e profissional de web scraping com:

✅ **Arquitetura Clean** separando responsabilidades  
✅ **Rate Limiting** respeitoso  
✅ **Paginação** ASP.NET complexa  
✅ **Error Handling** robusto  
✅ **Caching** inteligente  
✅ **Testes** automatizados  
✅ **UI Reativa** com Compose  

Use este guia como referência para implementar scraping em seus projetos com qualidade, ética e eficiência! 🚀

---

**Autor**: Equipe SeboKabo  
**Data**: Abril 2026  
**Versão**: 1.0

