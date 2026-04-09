package com.kiwizitos.collection.presentation.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kiwizitos.siege.components.card.SiegeCard
import com.kiwizitos.siege.components.card.SiegeCardStyle
import com.kiwizitos.siege.components.foundation.SiegeSearchBar
import com.kiwizitos.siege.components.foundation.SiegeText
import com.kiwizitos.siege.components.foundation.SiegeTextStyle
import com.kiwizitos.siege.components.layout.SiegeList
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeSpacing

@Composable
fun SearchScreen() {
    var query by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SiegeSpacing.Regular)
    ) {
        SiegeSearchBar(
            value = query,
            onValueChange = { query = it },
            placeholder = "Pesquisar séries, volumes..."
        )

        if (query.isEmpty()) {
            SiegeText(
                text = "Digite algo para começar a busca",
                style = SiegeTextStyle.Body,
                modifier = Modifier.padding(top = SiegeSpacing.Large)
            )
        } else {
            // Placeholder para resultados de busca
            SiegeList(
                items = List(10) { "Resultado ${it + 1} para \"$query\"" },
                itemContent = { item ->
                    SiegeCard(
                        style = SiegeCardStyle.Filled,
                        content = {
                            SiegeText(text = item, style = SiegeTextStyle.Body)
                        },
                        modifier = Modifier.padding(vertical = SiegeSpacing.Small)
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    SiegeTheme { SearchScreen() }
}
