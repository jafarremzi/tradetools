package app.tradertoolbox.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import app.tradertoolbox.logic.CalcResult
import app.tradertoolbox.logic.Calculators
import app.tradertoolbox.logic.Direction
import app.tradertoolbox.logic.formatNumber
import app.tradertoolbox.logic.parseNumber

private val calculatorNames = listOf("اندازه پوزیشن", "ریسک به ریوارد", "سود و زیان", "قیمت لیکوئید")

@Composable
fun CalculatorsScreen() {
    var selected by rememberSaveable { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ScreenPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(calculatorNames) { index, name ->
                    Pill(
                        text = name,
                        selected = selected == index,
                        onClick = { selected = index },
                    )
                }
            }
        }
        item {
            when (selected) {
                0 -> PositionSizeCalculator()
                1 -> RiskRewardCalculator()
                2 -> ProfitLossCalculator()
                else -> LiquidationCalculator()
            }
        }
        item {
            Text(
                text = "نتیجه ماشین‌حساب‌ها تقریبی است و توصیه معاملاتی نیست. پیش از هر معامله، اعداد را با اطلاعات کارگزار یا صرافی خودتان کنترل کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---------- اجزای مشترک ----------

@Composable
private fun NumberField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Palette.Accent,
            unfocusedBorderColor = Palette.BorderStrong,
            focusedContainerColor = Palette.Surface,
            unfocusedContainerColor = Palette.Surface,
            cursorColor = Palette.Accent,
            focusedLabelColor = Palette.Accent,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun ResultBox(content: @Composable ColumnScope.() -> Unit) {
    AppCard(hero = true, content = content)
}

@Composable
private fun ResultLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Palette.TextSoft)
        Text("\u200E$value", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(message, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun HintText() {
    Text(
        text = "مقادیر را وارد کنید تا نتیجه نمایش داده شود.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun NoteText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DirectionChips(selected: Int, onSelect: (Int) -> Unit) {
    Segmented(options = listOf("خرید (Long)", "فروش (Short)"), selectedIndex = selected, onSelect = onSelect)
}

// ---------- اندازه پوزیشن ----------

@Composable
private fun PositionSizeCalculator() {
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var balance by rememberSaveable { mutableStateOf("") }
    var riskPct by rememberSaveable { mutableStateOf("1") }
    var pips by rememberSaveable { mutableStateOf("") }
    var pipValue by rememberSaveable { mutableStateOf("10") }
    var entry by rememberSaveable { mutableStateOf("") }
    var stop by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Segmented(options = listOf("فارکس (لات)", "کریپتو و سهام"), selectedIndex = mode, onSelect = { mode = it })
        NumberField("موجودی حساب", balance) { balance = it }
        NumberField("درصد ریسک در هر معامله", riskPct) { riskPct = it }

        if (mode == 0) {
            NumberField("حد ضرر (پیپ)", pips) { pips = it }
            NumberField("ارزش هر پیپ برای یک لات استاندارد", pipValue) { pipValue = it }
            NoteText("برای جفت‌ارزهایی که ارز دوم آن‌ها دلار است (مثل EURUSD) ارزش هر پیپ برای یک لات استاندارد حدود ۱۰ دلار است. برای بقیه جفت‌ارزها از کارگزار خود بپرسید.")

            val b = parseNumber(balance)
            val r = parseNumber(riskPct)
            val p = parseNumber(pips)
            val v = parseNumber(pipValue)
            if (b == null || r == null || p == null || v == null) {
                HintText()
            } else {
                when (val res = Calculators.forexLots(b, r, p, v)) {
                    is CalcResult.Error -> ErrorText(res.message)
                    is CalcResult.Ok -> ResultBox {
                        BigNumber("حجم پیشنهادی", formatNumber(res.value.lots, 2), "لات")
                        ResultLine("مبلغ ریسک", formatNumber(res.value.riskAmount, 2))
                    }
                }
            }
        } else {
            NumberField("قیمت ورود", entry) { entry = it }
            NumberField("قیمت حد ضرر", stop) { stop = it }

            val b = parseNumber(balance)
            val r = parseNumber(riskPct)
            val e = parseNumber(entry)
            val s = parseNumber(stop)
            if (b == null || r == null || e == null || s == null) {
                HintText()
            } else {
                when (val res = Calculators.unitsByPrice(b, r, e, s)) {
                    is CalcResult.Error -> ErrorText(res.message)
                    is CalcResult.Ok -> ResultBox {
                        BigNumber("تعداد واحد (کوین یا سهم)", formatNumber(res.value.units, 6))
                        ResultLine("مبلغ ریسک", formatNumber(res.value.riskAmount, 2))
                        ResultLine("ارزش پوزیشن", formatNumber(res.value.positionValue, 2))
                    }
                }
            }
        }
    }
}

// ---------- ریسک به ریوارد ----------

@Composable
private fun RiskRewardCalculator() {
    var entry by rememberSaveable { mutableStateOf("") }
    var stop by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NumberField("قیمت ورود", entry) { entry = it }
        NumberField("قیمت حد ضرر", stop) { stop = it }
        NumberField("قیمت حد سود", target) { target = it }

        val e = parseNumber(entry)
        val s = parseNumber(stop)
        val t = parseNumber(target)
        if (e == null || s == null || t == null) {
            HintText()
        } else {
            when (val res = Calculators.riskReward(e, s, t)) {
                is CalcResult.Error -> ErrorText(res.message)
                is CalcResult.Ok -> ResultBox {
                    BigNumber("نسبت ریسک به ریوارد", "1 : " + formatNumber(res.value.ratio, 2))
                    ResultLine("جهت معامله", if (res.value.isLong) "خرید (Long)" else "فروش (Short)")
                    ResultLine("ریسک (فاصله تا حد ضرر)", formatNumber(res.value.risk, 6))
                    ResultLine("ریوارد (فاصله تا حد سود)", formatNumber(res.value.reward, 6))
                    ResultLine("نرخ برد لازم برای سر به سر شدن", formatNumber(res.value.breakEvenWinRatePct, 1) + "%")
                }
            }
        }
    }
}

// ---------- سود و زیان ----------

@Composable
private fun ProfitLossCalculator() {
    var dir by rememberSaveable { mutableIntStateOf(0) }
    var entry by rememberSaveable { mutableStateOf("") }
    var exit by rememberSaveable { mutableStateOf("") }
    var size by rememberSaveable { mutableStateOf("") }
    var fee by rememberSaveable { mutableStateOf("0") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DirectionChips(selected = dir, onSelect = { dir = it })
        NumberField("قیمت ورود", entry) { entry = it }
        NumberField("قیمت خروج", exit) { exit = it }
        NumberField("حجم (تعداد واحد)", size) { size = it }
        NumberField("کارمزد هر سمت (درصد)", fee) { fee = it }

        val e = parseNumber(entry)
        val x = parseNumber(exit)
        val z = parseNumber(size)
        val f = parseNumber(fee)
        if (e == null || x == null || z == null || f == null) {
            HintText()
        } else {
            val direction = if (dir == 0) Direction.LONG else Direction.SHORT
            when (val res = Calculators.profitLoss(direction, e, x, z, f)) {
                is CalcResult.Error -> ErrorText(res.message)
                is CalcResult.Ok -> ResultBox {
                    BigNumber(
                        "سود یا زیان خالص",
                        formatNumber(res.value.net, 2),
                        color = if (res.value.net < 0) Palette.Danger else Palette.Accent,
                    )
                    ResultLine("سود یا زیان ناخالص", formatNumber(res.value.gross, 2))
                    ResultLine("مجموع کارمزد", formatNumber(res.value.fees, 2))
                    ResultLine("بازده خالص", formatNumber(res.value.netPct, 2) + "%")
                }
            }
        }
    }
}

// ---------- قیمت لیکوئید ----------

@Composable
private fun LiquidationCalculator() {
    var dir by rememberSaveable { mutableIntStateOf(0) }
    var entry by rememberSaveable { mutableStateOf("") }
    var leverage by rememberSaveable { mutableStateOf("") }
    var mmr by rememberSaveable { mutableStateOf("0.5") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DirectionChips(selected = dir, onSelect = { dir = it })
        NumberField("قیمت ورود", entry) { entry = it }
        NumberField("لوریج (مثلاً ۱۰)", leverage) { leverage = it }
        NumberField("مارجین نگهداری (درصد)", mmr) { mmr = it }
        NoteText("این یک برآورد تقریبی برای مارجین ایزوله در قراردادهای خطی USDT است. فرمول دقیق و مارجین نگهداری هر صرافی متفاوت است؛ عدد نهایی را از خود صرافی بخوانید.")

        val e = parseNumber(entry)
        val l = parseNumber(leverage)
        val m = parseNumber(mmr)
        if (e == null || l == null || m == null) {
            HintText()
        } else {
            val direction = if (dir == 0) Direction.LONG else Direction.SHORT
            when (val res = Calculators.liquidationPrice(direction, e, l, m)) {
                is CalcResult.Error -> ErrorText(res.message)
                is CalcResult.Ok -> ResultBox {
                    BigNumber("قیمت تقریبی لیکوئید", formatNumber(res.value.price, 4))
                    ResultLine("فاصله تا قیمت ورود", formatNumber(res.value.distancePct, 2) + "%")
                }
            }
        }
    }
}
