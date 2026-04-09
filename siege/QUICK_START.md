# 🚀 Quick Start Guide - Siege Design System

Guia rápido para começar a usar o **Siege Design System** no seu app Android.

---

## 📦 Instalação

O módulo `siege` já está configurado como uma biblioteca Android no projeto.

### No seu `build.gradle.kts` do módulo app:

```kotlin
dependencies {
    implementation(project(":siege"))
    // ... outras dependências
}
```

---

## 🎨 Configuração Básica

### 1. Envolver o app com SiegeTheme

No seu `MainActivity.kt` ou composable raiz:

```kotlin
import com.kiwizitos.siege.theme.SiegeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SiegeTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                }
            }
        }
    }
}
```

---

## 🧩 Usando Componentes

### Exemplo 1: StatCard (Card de Estatística)

```kotlin
import com.kiwizitos.siege.components.card.StatCard
import com.kiwizitos.siege.tokens.SiegeColors

@Composable
fun MyStatsScreen() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            label = "SÉRIES SALVAS",
            value = "18",
            accentColor = SiegeColors.AccentPink
        )
        
        StatCard(
            label = "EDIÇÕES",
            value = "142",
            accentColor = SiegeColors.AccentCyan
        )
    }
}
```

### Exemplo 2: ContentCard (Card de Série/Edição)

```kotlin
import com.kiwizitos.siege.components.card.ContentCard
import com.kiwizitos.siege.components.card.BadgeData
import com.kiwizitos.siege.tokens.SiegeColors

@Composable
fun SeriesCard() {
    ContentCard(
        coverImage = painterResource(R.drawable.watchmen_cover),
        title = "Watchmen",
        subtitle = "DC Comics / Alan Moore",
        progress = 0.92f,
        progressText = "11/12",
        badges = listOf(
            BadgeData("RARO", SiegeColors.BadgeRare),
            BadgeData("POSSUÍDA", SiegeColors.BadgePossessed)
        ),
        onClick = { /* Navegar para detalhes */ }
    )
}
```

### Exemplo 3: Botões

```kotlin
import com.kiwizitos.siege.components.button.*
import com.kiwizitos.siege.tokens.SiegeColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit

@Composable
fun ActionsSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botão outlined (secundário)
        OutlinedAppButton(
            text = "Quero completar",
            onClick = { /* ação */ },
            icon = Icons.Default.Check,
            borderColor = SiegeColors.AccentPink
        )
        
        // Botão filled (primário)
        FilledAppButton(
            text = "Importar de 'Lido'",
            onClick = { /* ação */ },
            icon = Icons.Default.Add,
            containerColor = SiegeColors.AccentCyan
        )
        
        // Botão accent (crítico)
        AccentButton(
            text = "Atualizar Status",
            onClick = { /* ação */ },
            icon = Icons.Default.Edit
        )
    }
}
```

### Exemplo 4: SynopsisPanel

```kotlin
import com.kiwizitos.siege.components.card.SynopsisPanel

@Composable
fun SeriesDetails() {
    SynopsisPanel(
        synopsis = "Um feiticeiro tentando capturar a Morte para barganhar " +
                "pela vida eterna acabou prendendo seu irmão mais novo, Morfeu, " +
                "o Senhor dos Sonhos. Após décadas de encarceramento, Morfeu " +
                "escapa e inicia sua jornada para recuperar seus objetos de " +
                "poder e restaurar seu reino em ruínas.",
        displayNumber = "99"  // Opcional
    )
}
```

---

## 🎨 Acessando Tokens Diretamente

### Cores

```kotlin
import com.kiwizitos.siege.tokens.SiegeColors

Box(
    modifier = Modifier
        .background(SiegeColors.SurfaceDark)
        .padding(16.dp)
)

Text(
    text = "Título",
    color = SiegeColors.TextPrimary
)

Text(
    text = "Descrição",
    color = SiegeColors.TextSecondary
)
```

### Spacing

```kotlin
import com.kiwizitos.siege.tokens.SiegeSpacing

Column(
    modifier = Modifier.padding(SiegeSpacing.Regular),
    verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Small)
) {
    // conteúdo
}
```

### Shapes

```kotlin
import com.kiwizitos.siege.tokens.SiegeShapes

Box(
    modifier = Modifier
        .clip(SiegeShapes.Large)
        .background(SiegeColors.SurfaceDark)
)
```

### Tipografia

```kotlin
import androidx.compose.material3.MaterialTheme

// Headline (títulos grandes)
Text(
    text = "Watchmen",
    style = MaterialTheme.typography.headlineMedium,
    color = SiegeColors.TextPrimary
)

// Body (conteúdo)
Text(
    text = "Uma obra-prima de Alan Moore...",
    style = MaterialTheme.typography.bodyMedium,
    color = SiegeColors.TextSecondary
)

// Label (status, badges)
Text(
    text = "RARO",
    style = MaterialTheme.typography.labelSmall,
    color = SiegeColors.TextTertiary
)
```

---

## 📱 Exemplo Completo de Tela

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeSpacing
import com.kiwizitos.siege.components.card.*
import com.kiwizitos.siege.components.button.*

@Composable
fun MyCollectionScreen() {
    SiegeTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SiegeColors.BackgroundDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(SiegeSpacing.Regular),
                verticalArrangement = Arrangement.spacedBy(SiegeSpacing.Large)
            ) {
                // Título da página
                Text(
                    text = "Minha Coleção de Séries",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SiegeColors.TextPrimary
                )
                
                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Medium)
                ) {
                    StatCard(
                        label = "SÉRIES",
                        value = "18",
                        accentColor = SiegeColors.AccentPink,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "EDIÇÕES",
                        value = "142",
                        accentColor = SiegeColors.AccentCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Botões de ação
                OutlinedAppButton(
                    text = "Adicionar nova série",
                    onClick = { /* ação */ },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Lista de séries
                Text(
                    text = "Minhas Séries",
                    style = MaterialTheme.typography.titleMedium,
                    color = SiegeColors.AccentPink
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(SiegeSpacing.Medium)
                ) {
                    items(seriesList) { series ->
                        ContentCard(
                            coverImage = series.cover,
                            title = series.title,
                            subtitle = series.author,
                            progress = series.progress,
                            progressText = series.progressText,
                            badges = series.badges,
                            onClick = { /* navegar */ }
                        )
                    }
                }
            }
        }
    }
}
```

---

## 🎯 Boas Práticas

### ✅ Fazer

- Sempre use `SiegeTheme` como wrapper principal
- Use os tokens (`SiegeColors`, `SiegeSpacing`, etc.) para manter consistência
- Use os componentes prontos sempre que possível
- Siga a hierarquia de tipografia (Headline → Body → Label)

### ❌ Evitar

- Não defina cores hardcoded (use `SiegeColors`)
- Não defina espaçamentos fixos em dp (use `SiegeSpacing`)
- Não ignore a hierarquia de tipografia
- Não modifique os componentes base sem necessidade

---

## 📚 Documentação Completa

Para mais detalhes, consulte:
- [DESIGN_SYSTEM.md](./DESIGN_SYSTEM.md) - Documentação completa
- [DesignSystemShowcase.kt](./src/main/java/com/kiwizitos/siege/samples/DesignSystemShowcase.kt) - Exemplo visual

---

## 🐛 Problemas?

Se encontrar algum problema:
1. Verifique se o módulo `siege` está corretamente incluído no `settings.gradle.kts`
2. Certifique-se de que todas as dependências do Material 3 estão atualizadas
3. Limpe e reconstrua o projeto (`Build → Clean Project → Rebuild Project`)

---

**Happy coding! 🎨✨**
