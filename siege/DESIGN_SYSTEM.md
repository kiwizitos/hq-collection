# 🎨 Siege Design System

Design System premium para o app **Minha Coleção de Séries**.

Baseado em **Jetpack Compose** + **Material 3** com foco em dark mode e interface vibrante.

---

## 📐 Estrutura

```
siege/
├── tokens/              # Design tokens (cores, tipografia, spacing, shapes)
│   ├── SiegeColors.kt
│   ├── SiegeTypography.kt
│   ├── SiegeSpacing.kt
│   └── SiegeShapes.kt
│
├── theme/               # Configuração do tema Material 3
│   ├── SiegeTheme.kt
│   └── SiegeColorScheme.kt
│
├── components/          # Componentes reutilizáveis
│   ├── button/
│   │   └── AppButtons.kt        # FilledAppButton, OutlinedAppButton, AccentButton
│   └── card/
│       ├── StatCard.kt          # Card de estatísticas
│       ├── SynopsisPanel.kt     # Painel de sinopse
│       ├── ContentCard.kt       # Card principal de conteúdo
│       └── MiniSeriesCard.kt    # Versão compacta
│
└── foundation/          # Utilitários e extensões
    └── Modifiers.kt
```

---

## 🎨 Paleta de Cores

### Accent Colors (Destaque)
- **AccentPink** (#FF6B9D) - Botões primários, destaques, borders
- **AccentCyan** (#00E5FF) - Botões secundários de ação

### Backgrounds & Surfaces (Dark Mode Premium)
- **BackgroundDark** (#121212) - Fundo principal
- **SurfaceDark** (#1E1E1E) - Cards e painéis
- **SurfaceVariantDark** (#252525) - Cards de destaque
- **SurfaceElevatedDark** (#2A2A2A) - Cards premium

### Text Colors (Hierarquia)
- **TextPrimary** (#F5F5F5) - Headlines e títulos
- **TextSecondary** (#E0E0E0) - Body text e descrições
- **TextTertiary** (#AAAAAA) - Labels, captions e status
- **TextDisabled** (#888888) - Texto desabilitado

### Badges Especiais
- **BadgeRare** (#FFD700) - Badge "RARO"
- **BadgePossessed** (#00E676) - Badge "POSSUÍDA"
- **BadgeRead** (#00B8D4) - Badge "LIDA"

---

## ✍️ Tipografia

### Hierarquia Clara

#### Headlines (Títulos Principais)
- **headlineLarge** - 32sp, Bold
- **headlineMedium** - 24sp, Bold
- **headlineSmall** - 20sp, Bold

**Uso:** Nomes de séries, títulos de edições, títulos de páginas.

#### Titles (Subtítulos)
- **titleLarge** - 22sp, SemiBold
- **titleMedium** - 16sp, Medium
- **titleSmall** - 14sp, Medium

**Uso:** Seções, categorias, subtítulos.

#### Body (Conteúdo)
- **bodyLarge** - 16sp, Regular
- **bodyMedium** - 14sp, Regular
- **bodySmall** - 12sp, Regular

**Uso:** Sinopses, descrições, conteúdo principal.

#### Labels (Metadados)
- **labelLarge** - 14sp, Medium
- **labelMedium** - 12sp, Medium
- **labelSmall** - 10sp, Medium

**Uso:** Status, badges, informações extras (geralmente UPPERCASE).

---

## 📏 Spacing

Sistema baseado em múltiplos de 4dp:

```kotlin
SiegeSpacing.None      // 0dp
SiegeSpacing.XXSmall   // 2dp
SiegeSpacing.XSmall    // 4dp
SiegeSpacing.Small     // 8dp
SiegeSpacing.Medium    // 12dp
SiegeSpacing.Regular   // 16dp
SiegeSpacing.Large     // 24dp
SiegeSpacing.XLarge    // 32dp
SiegeSpacing.XXLarge   // 48dp
SiegeSpacing.Huge      // 64dp
```

---

## 🔲 Shapes

```kotlin
SiegeShapes.None        // 0dp
SiegeShapes.ExtraSmall  // 4dp
SiegeShapes.Small       // 8dp
SiegeShapes.Medium      // 12dp  (Botões padrão)
SiegeShapes.Large       // 16dp  (Cards de conteúdo)
SiegeShapes.ExtraLarge  // 28dp
SiegeShapes.Full        // 50%
```

---

## 🧩 Componentes

### 1. StatCard

Card compacto para exibir estatísticas numéricas.

```kotlin
StatCard(
    label = "SÉRIES SALVAS",
    value = "18",
    accentColor = SiegeColors.AccentPink
)
```

**Características:**
- Borda vertical colorida à esquerda
- Número grande em destaque (Headline)
- Label descritivo em UPPERCASE

---

### 2. SynopsisPanel

Painel para exibir sinopse/descrição.

```kotlin
SynopsisPanel(
    synopsis = "Um feiticeiro tentando capturar a Morte...",
    displayNumber = "99"  // Opcional
)
```

**Características:**
- Título "Sinopse" em AccentPink
- Texto em body style
- Número decorativo opcional

---

### 3. ContentCard

Card premium para séries e edições.

```kotlin
ContentCard(
    coverImage = painterResource(R.drawable.watchmen),
    title = "Watchmen",
    subtitle = "DC Comics / Alan Moore",
    progress = 0.92f,
    progressText = "11/12",
    badges = listOf(
        BadgeData("RARO", SiegeColors.BadgeRare),
        BadgeData("POSSUÍDA", SiegeColors.BadgePossessed)
    ),
    onClick = { /* ação */ }
)
```

**Características:**
- Imagem de capa grande (aspect ratio ~2:3)
- Título em Headline
- Subtítulo em Body
- Badges de status
- Barra de progresso
- Elevação premium (8dp)

---

### 4. MiniSeriesCard

Versão compacta do ContentCard.

```kotlin
MiniSeriesCard(
    coverImage = painterResource(R.drawable.one_piece),
    title = "One Piece",
    subtitle = "Vol. 101",
    badge = BadgeData("NOVA", SiegeColors.AccentCyan),
    onClick = { /* ação */ }
)
```

**Características:**
- Menor (160dp width)
- Foco na imagem
- Informações mínimas
- Badge único opcional

---

### 5. Sistema de Botões

#### FilledAppButton (Ação Primária)

```kotlin
FilledAppButton(
    text = "Importar de 'Lido'",
    onClick = { /* ação */ },
    containerColor = SiegeColors.AccentCyan,
    icon = Icons.Default.Add  // Opcional
)
```

#### OutlinedAppButton (Ação Secundária)

```kotlin
OutlinedAppButton(
    text = "Quero completar",
    onClick = { /* ação */ },
    borderColor = SiegeColors.AccentPink,
    icon = Icons.Default.Check  // Opcional
)
```

#### AccentButton (Ação Crítica)

```kotlin
AccentButton(
    text = "Atualizar Status da Edição",
    onClick = { /* ação */ },
    icon = Icons.Default.Edit  // Opcional
)
```

---

## 🚀 Como Usar

### 1. Envolver o app com SiegeTheme

```kotlin
@Composable
fun MyApp() {
    SiegeTheme(darkTheme = true) {
        // Seu conteúdo aqui
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen()
        }
    }
}
```

### 2. Acessar tokens diretamente

```kotlin
@Composable
fun CustomComponent() {
    Box(
        modifier = Modifier
            .background(SiegeColors.SurfaceDark)
            .padding(SiegeSpacing.Regular)
    ) {
        Text(
            text = "Exemplo",
            style = MaterialTheme.typography.headlineMedium,
            color = SiegeColors.TextPrimary
        )
    }
}
```

---

## 📋 Checklist de Implementação

- [x] Tokens de cores (Dark Mode Premium)
- [x] Tipografia com hierarquia clara
- [x] Sistema de spacing
- [x] Sistema de shapes
- [x] Tema SiegeTheme com Material 3
- [x] StatCard
- [x] SynopsisPanel
- [x] ContentCard (Premium)
- [x] MiniSeriesCard
- [x] Sistema de botões (Filled, Outlined, Accent)
- [x] Extensões de Modifier para borders customizadas

---

## 🎯 Próximos Passos

1. **Adicionar fontes customizadas** (ex: Inter, Roboto Flex)
2. **Componentes de navegação** (TopAppBar, BottomNavBar)
3. **Componentes de input** (TextField, SearchBar)
4. **Componentes de feedback** (Snackbar, Dialog)
5. **Animações** (transições de tela, scroll effects)
6. **Variantes de estado** (loading, error, empty states)

---

## 📚 Referências

- [Material Design 3](https://m3.material.io/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Design baseado na UI de "Minha Coleção de Séries"

---

**Desenvolvido com ❤️ usando Jetpack Compose**
