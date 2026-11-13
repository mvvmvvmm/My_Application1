package com.example.mycalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mycalc.ui.theme.MyCalcTheme
import java.math.BigDecimal
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyCalcTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF2F7FF) // Пастельно-голубой фон
                ) {
                    Scaffold(
                        containerColor = Color.Transparent,
                    ) { innerPadding ->
                        Calculator(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun Calculator(modifier: Modifier = Modifier) {
    // rememberSaveable сохраняет состояние при смене ориентации
    var display by rememberSaveable { mutableStateOf("0") }
    var expression by rememberSaveable { mutableStateOf("") }
    var currentOperation by rememberSaveable { mutableStateOf("") }

    fun onInput(char: String) {
        if (display == "0" && char != ".") {
            display = char
        } else if (display == "Ошибка" || display == "∞") {
            display = char
            expression = ""
            currentOperation = ""
        } else {
            if (char == "." && display.contains(".")) return
            display += char
        }
    }

    fun onOperation(op: String) {
        if (display == "Ошибка" || display == "∞") return
        expression += display + " $op "
        display = "0"
        currentOperation = op
    }

    fun onEquals() {
        if (display == "Ошибка" || display == "∞") return
        val fullExpr = expression + display
        try {
            val result = evaluateExpression(fullExpr)
            expression = ""
            currentOperation = ""
            display = formatResult(result)
        } catch (e: Exception) {
            display = "Ошибка"
            expression = ""
            currentOperation = ""
        }
    }

    fun onClear() {
        display = "0"
        expression = ""
        currentOperation = ""
    }

    fun onBackspace() {
        if (display == "Ошибка" || display == "∞") {
            display = "0"
        } else if (display.length > 1) {
            display = display.dropLast(1)
        } else {
            display = "0"
        }
    }

    fun applyUnaryFunction(func: String) {
        if (display == "Ошибка" || display == "∞") return
        val num = display.toDoubleOrNull() ?: return
        val result = when (func) {
            "√" -> if (num >= 0) sqrt(num) else Double.NaN
            "x²" -> num * num
            "sin" -> sin(Math.toRadians(num))
            "cos" -> cos(Math.toRadians(num))
            "tan" -> tan(Math.toRadians(num))
            "ctg" -> 1.0 / tan(Math.toRadians(num))
            else -> num
        }
        display = formatResult(result)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // --- ЗОНА ДИСПЛЕЯ ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            // 1. Предыдущее выражение (маленький серый шрифт)
            Text(
                text = expression,
                color = Color.Gray,
                fontSize = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Текущая операция (голубой шрифт)
            Text(
                text = currentOperation,
                color = Color(0xFF9BC1E8),
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Текущее число (крупный шрифт с авто-уменьшением)
            val fontSize = when {
                display.length > 15 -> 20.sp
                display.length > 12 -> 24.sp
                display.length > 9 -> 32.sp
                display.length > 6 -> 44.sp
                else -> 56.sp
            }

            Text(
                text = display,
                color = Color(0xFF2C3E50),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- ЗОНА КЛАВИАТУРЫ ---

        // Ряд 1: Научные функции
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton("sin", isSci = true, modifier = Modifier.weight(1f)) { applyUnaryFunction("sin") }
            CalcButton("cos", isSci = true, modifier = Modifier.weight(1f)) { applyUnaryFunction("cos") }
            CalcButton("tan", isSci = true, modifier = Modifier.weight(1f)) { applyUnaryFunction("tan") }
            CalcButton("ctg", isSci = true, modifier = Modifier.weight(1f)) { applyUnaryFunction("ctg") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ряд 2: Корень, Квадрат, Очистка, Стереть
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton("√", isSci = true, modifier = Modifier.weight(1f)) { applyUnaryFunction("√") }
            CalcButton("x²", isSci = true, modifier = Modifier.weight(1f)) { applyUnaryFunction("x²") }
            CalcButton("AC", isAction = true, modifier = Modifier.weight(1f)) { onClear() }
            CalcButton("⌫", isAction = true, modifier = Modifier.weight(1f)) { onBackspace() }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ряд 3: 7, 8, 9, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton("7", modifier = Modifier.weight(1f)) { onInput("7") }
            CalcButton("8", modifier = Modifier.weight(1f)) { onInput("8") }
            CalcButton("9", modifier = Modifier.weight(1f)) { onInput("9") }
            CalcButton("÷", isOp = true, modifier = Modifier.weight(1f)) { onOperation("÷") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ряд 4: 4, 5, 6, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton("4", modifier = Modifier.weight(1f)) { onInput("4") }
            CalcButton("5", modifier = Modifier.weight(1f)) { onInput("5") }
            CalcButton("6", modifier = Modifier.weight(1f)) { onInput("6") }
            CalcButton("×", isOp = true, modifier = Modifier.weight(1f)) { onOperation("×") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ряд 5: 1, 2, 3, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton("1", modifier = Modifier.weight(1f)) { onInput("1") }
            CalcButton("2", modifier = Modifier.weight(1f)) { onInput("2") }
            CalcButton("3", modifier = Modifier.weight(1f)) { onInput("3") }
            CalcButton("−", isOp = true, modifier = Modifier.weight(1f)) { onOperation("-") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ряд 6: 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton("0", modifier = Modifier.weight(2f)) { onInput("0") }
            CalcButton(".", modifier = Modifier.weight(1f)) { onInput(".") }
            CalcButton("=", isOp = true, modifier = Modifier.weight(1f)) { onEquals() }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    isOp: Boolean = false,    // Операции
    isSci: Boolean = false,   // Научные функции
    isAction: Boolean = false,// Действия (AC, Backspace)
    onClick: () -> Unit
) {
    // Цветовая палитра по ТЗ (белый и пастельно-голубой)
    val containerColor = when {
        isOp -> Color(0xFF9BC1E8)      // Пастельно-голубой для операций
        isSci -> Color(0xFFE3F2FD)     // Светло-голубой для науки
        isAction -> Color(0xFFFFE0E0)  // Бледно-красный для очистки
        else -> Color.White            // Белый для цифр
    }

    val contentColor = when {
        isOp -> Color.White
        else -> Color(0xFF2C3E50)      // Темно-серый/синий текст
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = if (isSci || isAction) 16.sp else 24.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// --- МАТЕМАТИЧЕСКИЙ ДВИЖОК (Приоритет операций) ---
fun evaluateExpression(expression: String): Double {
    val tokens = expression.replace(" ", "")
        .replace("×", "*")
        .replace("÷", "/")
        .replace("−", "-")
        .split(Regex("(?<=[-+*/])|(?=[-+*/])"))
        .filter { it.isNotBlank() }

    val values = mutableListOf<Double>()
    val ops = mutableListOf<String>()

    fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            else -> -1
        }
    }

    fun applyOp(a: Double, b: Double, op: String): Double {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b != 0.0) a / b else throw ArithmeticException("Деление на ноль")
            else -> b
        }
    }

    var i = 0
    while (i < tokens.size) {
        val token = tokens[i]
        when {
            token.toDoubleOrNull() != null -> values.add(token.toDouble())
            token == "(" -> ops.add(token)
            token == ")" -> {
                while (ops.isNotEmpty() && ops.last() != "(") {
                    values.add(applyOp(values.removeAt(values.size - 2), values.removeAt(values.size - 1), ops.removeAt(ops.size - 1)))
                }
                if (ops.isNotEmpty()) ops.removeAt(ops.size - 1)
            }
            else -> {
                while (ops.isNotEmpty() && precedence(ops.last()) >= precedence(token)) {
                    values.add(applyOp(values.removeAt(values.size - 2), values.removeAt(values.size - 1), ops.removeAt(ops.size - 1)))
                }
                ops.add(token)
            }
        }
        i++
    }

    while (ops.isNotEmpty()) {
        values.add(applyOp(values.removeAt(values.size - 2), values.removeAt(values.size - 1), ops.removeAt(ops.size - 1)))
    }

    return if (values.isNotEmpty()) values[0] else 0.0
}

// --- ФОРМАТИРОВАНИЕ РЕЗУЛЬТАТА ---
fun formatResult(value: Double): String {
    if (value.isNaN()) return "Ошибка"
    if (value.isInfinite()) return "∞"

    return try {
        // Используем BigDecimal для избежания экспоненциальной записи (E) у больших чисел
        val bd = BigDecimal(value.toString()).stripTrailingZeros()
        // Ограничиваем количество знаков после запятой до 8, чтобы не было "мусора" от Double
        val rounded = bd.setScale(8, java.math.RoundingMode.HALF_UP).stripTrailingZeros()
        rounded.toPlainString()
    } catch (e: Exception) {
        value.toString()
    }
}