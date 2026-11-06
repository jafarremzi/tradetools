package app.tradertoolbox.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.tradertoolbox.data.Tool
import app.tradertoolbox.data.ToolsData

@Composable
fun FavoritesScreen(vm: MainViewModel, data: ToolsData?, favorites: Set<String>) {
    var selected by remember { mutableStateOf<Tool?>(null) }
    val favoriteTools = data?.tools?.filter { it.id in favorites }.orEmpty()

    if (favoriteTools.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "هنوز ابزاری به علاقه‌مندی‌ها اضافه نکرده‌اید.\nروی آیکن قلب در فهرست ابزارها بزنید.",
                textAlign = TextAlign.Center,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = ScreenPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(favoriteTools, key = { it.id }) { tool ->
                ToolCard(
                    tool = tool,
                    isFavorite = true,
                    onToggleFavorite = { vm.toggleFavorite(tool.id) },
                    onClick = { selected = tool },
                )
            }
        }
    }

    selected?.let { tool ->
        ToolDetailDialog(tool = tool, onDismiss = { selected = null })
    }
}
