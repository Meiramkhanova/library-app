package com.adina.spendlog.model

object Validation {

    fun amountError(text: String, emptyMessage: String = "Enter an amount"): String? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return emptyMessage
        val value = trimmed.toDoubleOrNull() ?: return "Use numbers only, e.g. 1500 or 99.50"
        if (value <= 0) return "Must be greater than 0"
        if (trimmed.substringAfter('.', "").length > 2) return "Use at most 2 decimal places"
        if (value > FinanceRules.MAX_AMOUNT) return "Too large"
        return null
    }

    fun noteError(text: String): String? = when {
        text.isBlank() -> "Describe what you did"
        text.length > FinanceRules.MAX_NOTE_LENGTH -> "Max ${FinanceRules.MAX_NOTE_LENGTH} characters"
        else -> null
    }

    fun accountNameError(name: String, takenNames: List<String>): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Enter a name"
            trimmed.length > FinanceRules.MAX_NAME_LENGTH -> "Max ${FinanceRules.MAX_NAME_LENGTH} characters"
            takenNames.any { it.equals(trimmed, ignoreCase = true) } -> "This name is already used"
            else -> null
        }
    }

    fun startingBalanceError(text: String, type: String): String? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return "Enter a starting balance (0 is fine)"
        val value = trimmed.toDoubleOrNull() ?: return "Use numbers only"
        if (value < 0 && type != AccountType.CARD) return "Only a card can start below 0"
        if (kotlin.math.abs(value) > FinanceRules.MAX_AMOUNT) return "Too large"
        return null
    }

    fun creditLimitError(text: String): String? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return "Enter a credit limit (0 is fine)"
        val value = trimmed.toDoubleOrNull() ?: return "Use numbers only"
        if (value < 0) return "Can't be negative"
        if (value > FinanceRules.MAX_AMOUNT) return "Too large"
        return null
    }

    fun cleanNumberInput(text: String, allowMinus: Boolean = false): String {
        val normalized = text.replace(',', '.')
        val filtered = normalized.filterIndexed { index, c ->
            c.isDigit() || c == '.' || (allowMinus && c == '-' && index == 0)
        }
        val firstDot = filtered.indexOf('.')
        return if (firstDot == -1) filtered
        else filtered.substring(0, firstDot + 1) + filtered.substring(firstDot + 1).replace(".", "")
    }
}
