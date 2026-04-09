# 📊 Siege Design System - Componentes Visuais

Referência visual rápida de todos os componentes disponíveis.

---

## 🎴 Cards

### StatCard
**Uso:** Exibir estatísticas numéricas com destaque  
**Tamanho:** Flexível (geralmente em Row com weight)  
**Elementos:**
- Borda vertical colorida (4dp)
- Label em UPPERCASE (labelSmall)
- Valor numérico grande (displaySmall)

**Exemplo visual:**
```
┌─────────────────────────┐
│█  SÉRIES SALVAS         │
│█                        │
│█  18                    │
│                         │
└─────────────────────────┘
```

**Código:**
```kotlin
StatCard(
    label = "SÉRIES SALVAS",
    value = "18",
    accentColor = SiegeColors.AccentPink
)
```

---

### SynopsisPanel
**Uso:** Exibir sinopse/descrição de conteúdo  
**Tamanho:** fillMaxWidth  
**Elementos:**
- Título "Sinopse" em AccentPink
- Texto descritivo em bodyMedium
- Número decorativo opcional (displayLarge, opacidade 20%)

**Exemplo visual:**
```
┌─────────────────────────────────────┐
│ Sinopse                          99 │
│                                     │
│ Um feiticeiro tentando capturar     │
│ a Morte para barganhar pela vida    │
│ eterna acabou prendendo seu irmão   │
│ mais novo, Morfeu...                │
│                                     │
└─────────────────────────────────────┘
```

**Código:**
```kotlin
SynopsisPanel(
    synopsis = "Um feiticeiro tentando capturar...",
    displayNumber = "99"
)
```

---

### ContentCard
**Uso:** Card premium para séries e edições  
**Tamanho:** 280dp (width)  
**Elementos:**
- Imagem de capa (aspect ratio ~0.68)
- Badges sobrepostos (top-right)
- Título (headlineSmall)
- Subtítulo opcional (bodyMedium)
- Barra de progresso com texto

**Exemplo visual:**
```
┌─────────────────────┐
│                     │
│   [CAPA IMAGE]      │ ← RARO POSSUÍDA
│                     │
│ ─ ─ ─ ─ ─ ─ ─ ─ ─  │
│                     │
│ Watchmen            │ ← headlineSmall
│ DC Comics /         │ ← bodyMedium
│ Alan Moore          │
│                     │
│ 11/12    92% CONCL  │ ← labelMedium
│ ▓▓▓▓▓▓▓▓▓▓░░        │ ← Progress bar
│                     │
└─────────────────────┘
```

**Código:**
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

---

### MiniSeriesCard
**Uso:** Versão compacta para listas e carrosséis  
**Tamanho:** 160dp (width)  
**Elementos:**
- Imagem de capa (aspect ratio ~0.7)
- Badge único opcional
- Título (titleSmall)
- Subtítulo opcional (bodySmall)

**Exemplo visual:**
```
┌───────────────┐
│               │
│ [CAPA IMG]    │ ← NOVA
│               │
│ ─ ─ ─ ─ ─ ─  │
│               │
│ One Piece     │
│ Vol. 101      │
│               │
└───────────────┘
```

**Código:**
```kotlin
MiniSeriesCard(
    coverImage = painterResource(R.drawable.one_piece),
    title = "One Piece",
    subtitle = "Vol. 101",
    badge = BadgeData("NOVA", SiegeColors.AccentCyan),
    onClick = { /* ação */ }
)
```

---

## 🔘 Botões

### FilledAppButton
**Uso:** Ações primárias  
**Cores:** AccentCyan (padrão), ou customizado  
**Elementos:**
- Ícone opcional (20dp)
- Texto (labelLarge)
- Elevação: 4dp → 8dp (pressed)

**Exemplo visual:**
```
┌─────────────────────────────────┐
│  [+]  Importar de 'Lido'        │
└─────────────────────────────────┘
    Cyan background, texto preto
```

**Código:**
```kotlin
FilledAppButton(
    text = "Importar de 'Lido'",
    onClick = { /* ação */ },
    icon = Icons.Default.Add,
    containerColor = SiegeColors.AccentCyan
)
```

---

### OutlinedAppButton
**Uso:** Ações secundárias  
**Cores:** Borda colorida, fundo transparente  
**Elementos:**
- Borda 1.5dp
- Ícone opcional (20dp, colorido com borderColor)
- Texto (labelLarge)

**Exemplo visual:**
```
┌────────────────────────────────┐
│  [✓]  Quero completar          │
└────────────────────────────────┘
   Pink border, fundo transparente
```

**Código:**
```kotlin
OutlinedAppButton(
    text = "Quero completar",
    onClick = { /* ação */ },
    icon = Icons.Default.Check,
    borderColor = SiegeColors.AccentPink
)
```

---

### AccentButton
**Uso:** Ações críticas ou de alta importância  
**Cores:** AccentPink background, texto branco  
**Elementos:**
- Mesmo layout que FilledAppButton
- Pré-configurado com AccentPink

**Exemplo visual:**
```
┌─────────────────────────────────────┐
│  [✎]  Atualizar Status da Edição    │
└─────────────────────────────────────┘
    Pink background, texto branco
```

**Código:**
```kotlin
AccentButton(
    text = "Atualizar Status da Edição",
    onClick = { /* ação */ },
    icon = Icons.Default.Edit
)
```

---

## 🎨 Paleta de Cores Visual

```
ACCENT COLORS:
▓▓▓▓▓▓  AccentPink          #FF6B9D
▓▓▓▓▓▓  AccentCyan          #00E5FF

BACKGROUNDS:
▓▓▓▓▓▓  BackgroundDark      #121212
▓▓▓▓▓▓  SurfaceDark         #1E1E1E
▓▓▓▓▓▓  SurfaceVariantDark  #252525
▓▓▓▓▓▓  SurfaceElevatedDark #2A2A2A

TEXT:
▓▓▓▓▓▓  TextPrimary         #F5F5F5
▓▓▓▓▓▓  TextSecondary       #E0E0E0
▓▓▓▓▓▓  TextTertiary        #AAAAAA
▓▓▓▓▓▓  TextDisabled        #888888

BADGES:
▓▓▓▓▓▓  BadgeRare           #FFD700 (Gold)
▓▓▓▓▓▓  BadgePossessed      #00E676 (Green)
▓▓▓▓▓▓  BadgeRead           #00B8D4 (Cyan)

SEMANTIC:
▓▓▓▓▓▓  Success             #4CAF50
▓▓▓▓▓▓  Warning             #FFC107
▓▓▓▓▓▓  Error               #FF5252
▓▓▓▓▓▓  Info                #2196F3
```

---

## 📏 Spacing Scale

```
Nome           Valor    Uso típico
───────────────────────────────────────────
None           0dp      Reset
XXSmall        2dp      Micro ajustes
XSmall         4dp      Espaçamento mínimo
Small          8dp      Entre elementos próximos
Medium         12dp     Espaçamento interno de botões
Regular        16dp     Padding padrão de containers
Large          24dp     Entre seções
XLarge         32dp     Margens maiores
XXLarge        48dp     Separação significativa
Huge           64dp     Seções muito distantes
```

---

## 🔲 Shapes Reference

```
Nome           Valor    Uso típico
───────────────────────────────────────────
None           0dp      Sem arredondamento
ExtraSmall     4dp      Badges pequenos
Small          8dp      Status badges
Medium         12dp     Botões padrão
Large          16dp     Cards de conteúdo
ExtraLarge     28dp     Cards especiais
Full           50%      Botões circulares, avatars
```

---

## ✍️ Hierarquia Tipográfica

```
HEADLINES (Títulos principais - Bold)
────────────────────────────────────
headlineLarge    32sp    Títulos de tela
headlineMedium   24sp    Nomes de séries/edições ← MAIS USADO
headlineSmall    20sp    Subtítulos grandes

TITLES (Seções - Medium/SemiBold)
────────────────────────────────────
titleLarge       22sp    Seções principais
titleMedium      16sp    Categorias
titleSmall       14sp    Mini cards

BODY (Conteúdo - Regular)
────────────────────────────────────
bodyLarge        16sp    Descrições longas
bodyMedium       14sp    Sinopses, textos ← MAIS USADO
bodySmall        12sp    Textos auxiliares

LABELS (Status, badges - Medium, UPPERCASE)
────────────────────────────────────
labelLarge       14sp    Botões
labelMedium      12sp    Status, progresso ← MAIS USADO
labelSmall       10sp    Badges pequenos
```

---

## 📐 Layout Patterns

### Pattern 1: Stats Row
```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Medium)
) {
    StatCard(..., modifier = Modifier.weight(1f))
    StatCard(..., modifier = Modifier.weight(1f))
}
```

### Pattern 2: Content Grid/Carousel
```kotlin
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Medium),
    contentPadding = PaddingValues(horizontal = SiegeSpacing.Regular)
) {
    items(contentList) { item ->
        ContentCard(...)
    }
}
```

### Pattern 3: Screen Layout
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(SiegeSpacing.Regular),
    verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Large)
) {
    // Título
    Text(...)
    
    // Stats
    Row { StatCard(...) }
    
    // Sinopse
    SynopsisPanel(...)
    
    // Botões
    OutlinedAppButton(...)
    
    // Cards
    LazyRow { items(...) }
}
```

---

## 🎯 Decision Tree: Qual Componente Usar?

```
Preciso exibir...
│
├─ Um número/estatística?
│  └─→ StatCard
│
├─ Uma descrição longa?
│  └─→ SynopsisPanel
│
├─ Uma série/edição com detalhes?
│  │
│  ├─ Em destaque (grande)?
│  │  └─→ ContentCard
│  │
│  └─ Em lista/carrossel (pequeno)?
│     └─→ MiniSeriesCard
│
└─ Um botão de ação?
   │
   ├─ Ação primária importante?
   │  └─→ FilledAppButton ou AccentButton
   │
   └─ Ação secundária?
      └─→ OutlinedAppButton
```

---

**Dica:** Sempre consulte o arquivo `DesignSystemShowcase.kt` para ver todos os componentes em ação!
