package app.tradertoolbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

private data class TabItem(val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem("ابزارها", Icons.Default.Home),
    TabItem("بازار امروز", Icons.Default.Star),
    TabItem("ماشین‌حساب", Icons.Default.Build),
    TabItem("ساعت بازار", Icons.Default.DateRange),
    TabItem("مورد علاقه", Icons.Default.Favorite),
)

private const val RISK_NOTICE =
    "این اپ فقط مجموعه‌ای از لینک‌ها و ابزارهای کمکی است و هیچ توصیه معاملاتی، سیگنال یا تضمین سودی ارائه نمی‌کند.\n\n" +
        "معامله در بازارهای فارکس و ارز دیجیتال ریسک بالایی دارد و ممکن است بخشی از سرمایه یا همه آن را از دست بدهید.\n\n" +
        "ماشین‌حساب‌ها تقریبی هستند و باید با اطلاعات کارگزار یا صرافی شما کنترل شوند.\n\n" +
        "وب‌سایت‌های معرفی‌شده متعلق به اشخاص ثالث‌اند و مسئولیت محتوا و خدمات آن‌ها با اپ نیست. " +
        "مواردی که برچسب «حمایت‌شده» دارند، تبلیغاتی هستند."

@Composable
fun AppRoot(vm: MainViewModel = viewModel()) {
    // اپ همیشه راست‌به‌چین است، مستقل از زبان دستگاه.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        var tab by rememberSaveable { mutableIntStateOf(0) }
        var showAbout by remember { mutableStateOf(false) }

        val data by vm.data.collectAsState()
        val loadError by vm.loadError.collectAsState()
        val favorites by vm.favorites.collectAsState()
        val accepted by vm.disclaimerAccepted.collectAsState()

        Scaffold(
            containerColor = Palette.Bg,
            topBar = { TopBar(onInfo = { showAbout = true }) },
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (tab) {
                    0 -> DirectoryScreen(vm, data, loadError, favorites)
                    1 -> MarketScreen()
                    2 -> CalculatorsScreen()
                    3 -> SessionsScreen()
                    else -> FavoritesScreen(vm, data, favorites)
                }
                FloatingNav(
                    selected = tab,
                    onSelect = { tab = it },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 14.dp, vertical = 14.dp),
                )
            }
        }

        if (!accepted) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("هشدار ریسک") },
                text = { Text(RISK_NOTICE, modifier = Modifier.verticalScroll(rememberScrollState())) },
                confirmButton = {
                    TextButton(onClick = { vm.acceptDisclaimer() }) { Text("متوجه شدم") }
                },
            )
        }

        if (showAbout) {
            AlertDialog(
                onDismissRequest = { showAbout = false },
                title = { Text("درباره و هشدار ریسک") },
                text = { Text(RISK_NOTICE, modifier = Modifier.verticalScroll(rememberScrollState())) },
                confirmButton = {
                    TextButton(onClick = { showAbout = false }) { Text("بستن") }
                },
            )
        }
    }
}

@Composable
private fun TopBar(onInfo: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(68.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LogoMark()
            Text(
                text = "جعبه ابزار تریدر",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        CircleIconButton(onClick = onInfo, contentDescription = "درباره و هشدار ریسک") {
            Icon(Icons.Default.Info, contentDescription = null, tint = Palette.Text)
        }
    }
}

@Composable
private fun FloatingNav(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(32.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .background(Palette.Raised)
            .border(1.dp, Palette.BorderStrong, shape)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, item ->
            val isSelected = index == selected
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) Palette.Accent else Color.Transparent)
                    .clickable(role = Role.Tab, onClickLabel = item.label) { onSelect(index) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = if (isSelected) null else item.label,
                    tint = if (isSelected) Palette.OnAccent else Color(0xFFA9BAC7),
                )
                if (isSelected) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = item.label,
                        color = Palette.OnAccent,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
