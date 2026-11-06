package app.tradertoolbox.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tradertoolbox.data.Tool
import app.tradertoolbox.data.ToolsData
import app.tradertoolbox.logic.MarketFilter
import app.tradertoolbox.logic.filterTools

@Composable
fun DirectoryScreen(
    vm: MainViewModel,
    data: ToolsData?,
    loadError: Boolean,
    favorites: Set<String>,
) {
    if (data == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (loadError) {
                Text("خواندن فهرست ابزارها ممکن نشد.")
            } else {
                CircularProgressIndicator(color = Palette.Accent)
            }
        }
    } else {
        DirectoryContent(vm, data, favorites)
    }
}

@Composable
private fun DirectoryContent(vm: MainViewModel, data: ToolsData, favorites: Set<String>) {
    var selected by remember { mutableStateOf<Tool?>(null) }
    val tools = filterTools(
        tools = data.tools,
        query = vm.query,
        market = vm.marketFilter,
        onlyPersian = vm.onlyPersian,
        category = vm.categoryFilter,
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ScreenPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = vm.query,
                onValueChange = { vm.query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                placeholder = { Text("جستجو در ابزارها...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (vm.query.isNotEmpty()) {
                        IconButton(onClick = { vm.query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "پاک کردن جستجو")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Palette.Accent,
                    unfocusedBorderColor = Palette.BorderStrong,
                    focusedContainerColor = Palette.Surface,
                    unfocusedContainerColor = Palette.Surface,
                    cursorColor = Palette.Accent,
                ),
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MarketFilter.entries) { filter ->
                    Pill(
                        text = filter.label,
                        selected = vm.marketFilter == filter,
                        onClick = { vm.marketFilter = filter },
                    )
                }
                item {
                    Pill(
                        text = "فقط فارسی",
                        selected = vm.onlyPersian,
                        onClick = { vm.onlyPersian = !vm.onlyPersian },
                    )
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Pill(
                        text = "همه دسته‌ها",
                        selected = vm.categoryFilter == null,
                        onClick = { vm.categoryFilter = null },
                    )
                }
                items(data.categories) { category ->
                    Pill(
                        text = category.title,
                        selected = vm.categoryFilter == category.id,
                        onClick = { vm.categoryFilter = category.id },
                    )
                }
            }
        }
        item {
            Text(
                text = "${tools.size} مورد",
                style = MaterialTheme.typography.labelMedium,
                color = Palette.Muted,
            )
        }
        if (tools.isEmpty()) {
            item { Text("موردی با این فیلترها پیدا نشد.") }
        }
        items(tools, key = { it.id }) { tool ->
            ToolCard(
                tool = tool,
                isFavorite = tool.id in favorites,
                onToggleFavorite = { vm.toggleFavorite(tool.id) },
                onClick = { selected = tool },
            )
        }
    }

    selected?.let { tool ->
        ToolDetailDialog(tool = tool, onDismiss = { selected = null })
    }
}
