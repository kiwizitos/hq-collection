# Siege — Design System

Siege é o **Design System** do app *Minha Coleção*. Ele fornece os blocos de construção visuais reutilizáveis que garantem consistência de aparência e comportamento em todo o projeto.

---

## ⚠️ Regra fundamental

> **Siege é uma biblioteca de componentes, não um módulo de features.**

### ✅ O que PERTENCE ao Siege

| Categoria | Exemplos |
|---|---|
| **Tokens** | Cores (`SiegeColors`), tipografia (`SiegeTypography`), espaçamento (`SiegeSpacing`), formas (`SiegeShapes`) |
| **Tema** | `SiegeTheme`, `SiegeColorScheme`, `SiegeThemeColors` |
| **Componentes atômicos** | `SiegeText`, `SiegeButton`, `SiegeSearchBar` |
| **Componentes moleculares** | `SiegeCard`, `SiegeContentCell`, `MiniSeriesCard`, `StatCard`, `SiegeList` |
| **Utilitários de UI** | Modifiers reutilizáveis (`foundation/Modifiers.kt`) |

### ❌ O que NÃO pertence ao Siege

| O que é | Onde deve ficar |
|---|---|
| Telas / Screens (`*Screen.kt`) | `app/presentation/view/` |
| Modelos de dados de negócio (`Colecao`, `Edicao`, etc.) | `app/presentation/view/` ou futuramente `domain/model/` |
| ViewModels | `app/presentation/viewmodel/` |
| Lógica de navegação (`NavHost`, rotas) | `app/navigation/` |
| Qualquer composable que conhece regras de negócio | `app/...` |

**Resumo:** se um arquivo precisa importar dados específicos do app ou conhece o conceito de "Coleção", "Edição", "Série", ele **não é parte do Design System**.

---

## Estrutura do módulo

```
siege/
└── src/main/java/com/kiwizitos/siege/
    ├── Siege.kt                  ← API pública / índice de imports
    ├── tokens/
    │   ├── SiegeColors.kt        ← Paleta completa de cores
    │   ├── SiegeTypography.kt    ← Escala tipográfica
    │   ├── SiegeSpacing.kt       ← Escala de espaçamento (múltiplos de 4dp)
    │   └── SiegeShapes.kt        ← Raios de borda
    ├── theme/
    │   ├── SiegeTheme.kt         ← Ponto de entrada do tema
    │   ├── SiegeColorScheme.kt   ← Mapeamento dark/light para Material3
    │   └── SiegeThemeColors.kt   ← Cores sensíveis ao tema via CompositionLocal
    ├── components/
    │   ├── foundation/
    │   │   ├── SiegeText.kt      ← Texto unificado (Headline / Body / Label)
    │   │   ├── SiegeButton.kt    ← Botão unificado (Primary / Outlined / Accent / Ghost)
    │   │   └── SiegeSearchBar.kt ← Campo de busca
    │   ├── card/
    │   │   ├── SiegeCard.kt         ← Card genérico (Filled / Elevated / Outlined)
    │   │   ├── SiegeContentCell.kt  ← Card de conteúdo (Cover / Grid / Row) + ContentType
    │   │   ├── MiniSeriesCard.kt    ← Card compacto para carrosséis
    │   │   └── StatCard.kt          ← Card de estatística (número + label)
    │   └── layout/
    │       └── SiegeList.kt      ← Lista unificada (Vertical / Horizontal / Grid)
    └── foundation/
        └── Modifiers.kt          ← Modifier extensions reutilizáveis
```

---

## Uso no app

```kotlin
// No módulo :app, importe os componentes diretamente:
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeColors
import com.kiwizitos.siege.tokens.SiegeSpacing
import com.kiwizitos.siege.components.card.SiegeContentCell
import com.kiwizitos.siege.components.foundation.SiegeButton
```

O tema deve ser aplicado **uma única vez**, no topo da árvore de composição em `MainActivity`:

```kotlin
SiegeTheme(darkTheme = true) {
    AppNavHost(navController = navController)
}
```

---

## Tokens principais

### Cores accent
| Token | Hex | Uso |
|---|---|---|
| `AccentPink` | `#FF6B9D` | Botões primários, destaques, bordas |
| `AccentCyan` | `#00E5FF` | Botões de ação secundária, contadores |

### Espaçamento
| Token | Valor | Uso típico |
|---|---|---|
| `XSmall` | 4dp | Padding interno mínimo |
| `Small` | 8dp | Gap entre elementos próximos |
| `Regular` | 16dp | Padding horizontal de tela |
| `Large` | 24dp | Separação entre seções |
| `XLarge` | 32dp | Separação maior |

### Tipografia (hierarquia)
| Token | Tamanho | Uso |
|---|---|---|
| `headlineLarge` | 32sp | Título principal de tela |
| `headlineMedium` | 24sp | Título de seção ou detalhe |
| `titleMedium` | 16sp | AppBar title |
| `bodyMedium` | 14sp | Corpo de texto |
| `labelMedium` | 12sp | Rótulos, badges, captions |

---

## Dependências do módulo

```kotlin
// siege/build.gradle.kts
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.material3)
implementation(libs.coil.compose)              // carregamento de imagens
implementation("androidx.compose.material:material-icons-extended:1.7.0")
```

> O módulo `:siege` **não depende** do módulo `:app`. A dependência é sempre unidirecional: `:app` → `:siege`.

