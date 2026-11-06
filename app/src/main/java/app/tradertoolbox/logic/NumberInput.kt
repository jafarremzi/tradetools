package app.tradertoolbox.logic

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** ارقام فارسی و عربی را به انگلیسی تبدیل می‌کند و جداکننده‌های هزارگان را حذف می‌کند. */
fun normalizeDigits(input: String): String {
    val sb = StringBuilder(input.length)
    for (ch in input) {
        when (ch) {
            in '\u06F0'..'\u06F9' -> sb.append('0' + (ch - '\u06F0'))
            in '\u0660'..'\u0669' -> sb.append('0' + (ch - '\u0660'))
            '\u066B', '/' -> sb.append('.')
            '\u066C', ',', '\u060C', ' ' -> Unit
            else -> sb.append(ch)
        }
    }
    return sb.toString()
}

fun parseNumber(input: String): Double? {
    val s = normalizeDigits(input.trim())
    if (s.isEmpty()) return null
    val v = s.toDoubleOrNull() ?: return null
    return if (v.isFinite()) v else null
}

fun formatNumber(value: Double, maxDecimals: Int = 4): String {
    val df = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))
    df.minimumFractionDigits = 0
    df.maximumFractionDigits = maxDecimals
    return df.format(value)
}
