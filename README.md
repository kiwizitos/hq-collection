# Collection

Aplicativo Android para catalogar e acompanhar sua coleção de quadrinhos, com dados raspados do [Guia dos Quadrinhos](https://www.guiadosquadrinhos.com).

---

## Design System — Siege

O módulo `siege/` define todos os componentes de UI, tokens e tema. **Nenhum componente Material3 deve ser usado diretamente nas telas da app** — sempre prefira os equivalentes do Siege.

### Tokens

| Token | Arquivo | Uso |
|---|---|---|
| `SiegeColors` | `tokens/SiegeColors.kt` | Cores de destaque (`AccentPink`, `AccentCyan`, `Error`) |
| `SiegeShapes` | `tokens/SiegeShapes.kt` | Formas (`Small`, `Medium`, `Full`) para cards, chips, campos |
| `SiegeSpacing` | `tokens/SiegeSpacing.kt` | Espaçamentos (`XXSmall` → `XXLarge`) — nunca use valores `.dp` literais |
| `SiegeTypography` | `tokens/SiegeTypography.kt` | Tipografia base (consumida pelo `SiegeTextStyle`) |

### Tema

```kotlin
SiegeTheme(darkTheme = true) { /* conteúdo */ }
SiegeTheme.colors.textPrimary   // cores adaptadas ao tema claro/escuro
SiegeTheme.colors.surface
SiegeTheme.colors.surfaceVariant
SiegeTheme.colors.outline
// …
```

### Componentes

#### Fundação

| Componente | Arquivo | Uso |
|---|---|---|
| `SiegeText` | `foundation/SiegeText.kt` | Substitui `Text`. Use sempre `SiegeTextStyle.Headline/Body/Label` |
| `SiegeButton` | `foundation/SiegeButton.kt` | Substitui `Button`/`OutlinedButton`/`TextButton`. Estilos: `Primary`, `Outlined`, `Accent`, `Ghost` |
| `SiegeTextField` | `foundation/SiegeTextField.kt` | Substitui `OutlinedTextField`. Já aplica cores, forma e tipografia do Siege |
| `SiegeSearchBar` | `foundation/SiegeSearchBar.kt` | Campo de pesquisa com ícone e botão de limpar |

#### Cards

| Componente | Arquivo | Uso |
|---|---|---|
| `SiegeCard` | `card/SiegeCard.kt` | Container genérico. Estilos: `Filled`, `Elevated`, `Outlined`. Slots: `title`, `trailingContent`, `bottomContent` |
| `SiegeContentCell` | `card/SiegeContentCell.kt` | Célula de capa para séries/volumes em listas e grades |
| `StatCard` | `card/StatCard.kt` | Card compacto com valor numérico e barra de destaque |
| `MiniSeriesCard` | `card/MiniSeriesCard.kt` | Card reduzido para séries em listas horizontais |

#### Layout

| Componente | Arquivo | Uso |
|---|---|---|
| `SiegeList` | `layout/SiegeList.kt` | Lista/grade com cabeçalho. Estilos: `Horizontal`, `Grid(n)` |

### Onde cada componente é usado

| Tela | Componentes Siege |
|---|---|
| `AuthScreen` | `SiegeTextField`, `SiegeButton`, `SiegeText` |
| `HomeScreen` | `SiegeContentCell`, `SiegeList`, `StatCard`, `SiegeText` |
| `SearchScreen` | `SiegeSearchBar`, `SiegeCard` (Outlined), `SiegeButton`, `SiegeText`, `CategoryBadge` |
| `CoversScreen` | `SiegeCard` (Elevated), `SiegeButton`, `SiegeText` |
| `DetailsScreen` | `SiegeCard` (Filled), `SiegeButton`, `SiegeText`, `SeriesBelongsToCard` |
| `LibraryScreen` | `SiegeCard`, `SiegeButton`, `SiegeText`, `SiegeContentCell` |
| `ProfileScreen` | `SiegeCard` (Filled), `SiegeButton`, `SiegeText` |

### O que NÃO usar nas telas

Estes componentes Material3 **têm equivalente no Siege** — não os use diretamente:

- ❌ `Text` → ✅ `SiegeText`
- ❌ `Button` / `OutlinedButton` / `TextButton` → ✅ `SiegeButton`
- ❌ `OutlinedTextField` → ✅ `SiegeTextField`
- ❌ `Card` / `ElevatedCard` / `OutlinedCard` / `Surface` (como container) → ✅ `SiegeCard`

Componentes sem equivalente (`CircularProgressIndicator`, `HorizontalDivider`, `Scaffold`, `TopAppBar`, `Switch`, `Icon`) podem ser usados diretamente.

---



| Camada | Tecnologia |
|---|---|
| UI | Jetpack Compose + Design System próprio (`siege`) |
| Navegação | Navigation Compose |
| DI | Hilt (Dagger) |
| Backend | Supabase (Auth + PostgREST) |
| Scraping | Jsoup (HTML parsing do Guia dos Quadrinhos) |
| Serialização | kotlinx.serialization |

---

## Arquitetura

```
app/
├── data/
│   ├── model/          # UserItem, UserSeries, ComicDetails, ItemStatus, CoverItem…
│   ├── remote/         # GuiaQuadrinhosParser (Jsoup), GuiaQuadrinhosService (HTTP)
│   └── repository/     # GalleryRepository (interface) + SupabaseGalleryRepository
│                         GuiaQuadrinhosRepository (ComicDataSource)
├── domain/
│   └── usecase/        # GetGalleryUseCase, SaveItemUseCase, RemoveItemUseCase…
├── navigation/
│   ├── AppRoute.kt     # Definição de todas as rotas
│   └── AppNavHost.kt   # NavHost com lógica de redirect para edições únicas
└── presentation/
    ├── view/           # AuthScreen, HomeScreen, SearchScreen, CoversScreen,
    │                     DetailsScreen, LibraryScreen, ProfileScreen
    └── viewmodel/      # AuthViewModel, GalleryViewModel, SearchViewModel,
                          EditionViewModel
siege/                  # Design system (componentes, tokens, tema)
supabase/migrations/    # Histórico de migrações SQL
```

---

## Banco de dados (Supabase)

Duas tabelas principais. O schema completo está em [`supabase/migrations/008_unified_schema.sql`](supabase/migrations/008_unified_schema.sql).

### `user_series`
Séries salvas pelo usuário para acesso rápido. Sem status de posse/leitura — apenas referência e capa para exibição na Home e Biblioteca.

| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | uuid | PK |
| `user_id` | uuid | FK → `auth.users` |
| `series_url` | text | Caminho relativo no Guia (`capas/slug/codigo`) |
| `series_title` | text | Título da série |
| `cover_url` | text? | Thumbnail da capa do 1º volume |
| `publisher` | text? | Editora nacional |
| `issue_count` | text? | Número de edições |

### `user_editions`
Volumes individuais salvos com status de posse e/ou leitura.

| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | uuid | PK |
| `user_id` | uuid | FK → `auth.users` |
| `guia_url` | text | Caminho relativo da edição (`edicao/slug/codigo/id`) |
| `title` | text | Título do volume |
| `cover_url` | text? | URL da capa |
| `series_url` | text? | FK lógica → `user_series.series_url` |
| `series_title` | text? | Título da série (desnormalizado para display) |
| `ownership` | `ownership_enum`? | `TENHO` \| `QUERO` |
| `read_status` | `read_status_enum`? | `LIDO` \| `LENDO` |
| `is_standalone` | boolean | `true` quando a edição não tem série com galeria de capas |

> **Regra:** ao menos um de `ownership` ou `read_status` deve ser não-nulo (`check_edition_status`).

---

## Funcionalidades principais

### Busca e scraping
- Busca de séries com paginação via POST (ASP.NET ViewState)
- Scraping de galeria de capas por série
- Scraping de detalhes de edição individual
- Rate limiting entre requisições

### Disponibilidade da fonte externa

O Guia pode retornar `403` com `cf-mitigated: challenge`. Isso exige uma
verificação no navegador; Retrofit/Jsoup não a executam. HTTPS, renovação das
capas e mudança de User-Agent não garantem acesso. Abrir o site no navegador
não compartilha automaticamente a autorização com o app. Uma integração
estável depende de acesso permitido pelo responsável pelo Guia.

- O app distingue challenge, falha HTTP, conexão e HTML inesperado.
- Falhas de paginação mantêm os itens carregados e exigem tentativa manual,
  sem repetição automática infinita.
- O `__VIEWSTATEGENERATOR` é extraído da página, não fixado para todas as rotas.
- URLs de acesso são normalizadas para HTTPS; identidades salvas não são
  reescritas automaticamente.
- Renovação de capas de registros já salvos altera somente `cover_url`, sem
  criar itens nem modificar posse/leitura. Falhas preservam a capa anterior.
- Dados da biblioteca vêm do Supabase; catálogo e detalhes completos ainda
  dependem da fonte externa. Se detalhes externos falharem, uma edição já
  carregada na galeria exibe seus dados salvos e permite alterar os status
  sem substituir metadados. Isso não é um cache offline persistente: uma nova
  sessão ainda precisa carregar a galeria do Supabase.
  O cliente do Retrofit não configura o Coil.

Para diagnosticar no Android, diferencie as URLs do Supabase, HTML do Guia e
imagens do Coil. Os logs `GuiaHttp` incluem status, caminho, indicador de
challenge e Ray ID; `GuiaRepository` identifica a operação que falhou.
Não compartilhe tokens, cookies nem credenciais ao coletar logs.

### Edições únicas (standalone)
Quando uma página `/capas/` contém texto "Edição única" ou apenas um link `/edicao/`, o app detecta automaticamente a edição como standalone:
- A tela de capas é **substituída** diretamente pela tela de detalhes (sem passar pela lista)
- O campo `is_standalone = true` é salvo automaticamente ao adicionar à galeria
- O card "Pertence ao título" não é exibido para edições standalone
- Na biblioteca, edições standalone aparecem na aba **Avulsos**

### Biblioteca
- Aba **Séries**: grade 2 colunas com séries salvas
- Aba **Volumes**: lista plana com filtros (`Avulsos` · `Todos` · `Tenho` · `Quero` · `Lido` · `Lendo`)
  - **Avulsos** mostra apenas `is_standalone = true` (lista plana, sem agrupamento)
  - Demais filtros excluem automaticamente os avulsos

### Galeria (DetailsScreen)
- Chips POSSE (`Tenho` / `Quero`) e LEITURA (`Lido` / `Lendo`) independentes
- Salva via upsert — o mesmo botão serve para criar e atualizar
- Ao desmarcar todos os chips, o item é removido automaticamente

---

## Migrações SQL

| Arquivo | Descrição |
|---|---|
| `001_user_items.sql` | Schema inicial |
| `002_item_status.sql` | Enum de status |
| `003_two_tables.sql` | Separação séries / edições |
| `004_normalized_schema.sql` | Normalização |
| `005_final_schema.sql` | Schema estável com RLS e índices |
| `006_standalone_editions.sql` | Coluna `is_standalone` em `user_editions` |
| `007_standalone_series.sql` | *(obsoleto — coluna não utilizada pelo app)* |
| `008_unified_schema.sql` | **Schema unificado de referência** (banco zerado) |
| `009_https_guia_urls.sql` | Atualização opcional somente de capas HTTP para HTTPS, sem alterar identidades |

Antes de executar SQL, confirme o schema instalado, o histórico aplicado e
faça backup. Os scripts de criação do zero não formam uma sequência incremental
segura para um banco existente. A migração 009 não foi executada pelo app e
não resolve bloqueio de acesso. Caso uma versão anterior de 009 já tenha sido
aplicada, não a reaplique como nova migração: audite o resultado e prepare uma
migração incremental específica.

Auditoria somente leitura antes de qualquer normalização de identidade:

```sql
SELECT 'editions' AS source, guia_url AS identity, count(*)
FROM user_editions GROUP BY guia_url
UNION ALL
SELECT 'series', series_url, count(*)
FROM user_series GROUP BY series_url;
```

Compare caminhos relativos e URLs absolutas do mesmo item por usuário antes
de planejar deduplicação; nunca descarte status para resolver conflitos.

## Validação local

Use JDK 17 compatível com o build Android (o JDK 24 pode falhar ao criar as
tasks de testes). No PowerShell, se o JDK 17 estiver instalado nesse caminho:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\jbr-17.0.14"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --console=plain
```

Os testes usam HTML de exemplo e serviços simulados, sem consultar o Guia nem
o Supabase real. Uma validação no aparelho continua necessária para testar
rede, sessão real, imagens e navegação.
