package com.adina.spendlog.data.repository

import com.adina.spendlog.data.local.AccountDao
import com.adina.spendlog.data.local.BudgetDao
import com.adina.spendlog.data.local.BudgetEntity
import com.adina.spendlog.data.local.TransactionDao
import com.adina.spendlog.data.toDomain
import com.adina.spendlog.data.toEntity
import com.adina.spendlog.model.Account
import com.adina.spendlog.model.AccountDraft
import com.adina.spendlog.model.AccountType
import com.adina.spendlog.model.Budget
import com.adina.spendlog.model.CardAccount
import com.adina.spendlog.model.CashAccount
import com.adina.spendlog.model.Categories
import com.adina.spendlog.model.Expense
import com.adina.spendlog.model.FinanceRules
import com.adina.spendlog.model.OperationResult
import com.adina.spendlog.model.Transaction
import com.adina.spendlog.model.TransactionDraft
import com.adina.spendlog.model.TransactionKind
import com.adina.spendlog.model.formatMoney
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFinanceRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) : FinanceRepository {

    override val accounts: Flow<List<Account>> =
        accountDao.observeAll().map { list -> list.map { it.toDomain() } }

    override val transactions: Flow<List<Transaction>> =
        transactionDao.observeAll().map { list -> list.map { it.toDomain() } }

    override val budgets: Flow<List<Budget>> =
        budgetDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getTransaction(id: Int): Transaction? =
        transactionDao.getById(id)?.toDomain()

    override suspend fun saveTransaction(draft: TransactionDraft): OperationResult {
        val account = accountDao.getById(draft.accountId)
            ?: return OperationResult.Error("Choose an existing account")
        if (draft.amount <= 0) return OperationResult.Error("Amount must be greater than 0")
        if (draft.note.isBlank()) return OperationResult.Error("Describe what you did")
        if (draft.category !in Categories.forKind(draft.kind)) {
            return OperationResult.Error("Choose a category")
        }

        val allAccounts = accountDao.getAll().map { it.toDomain() }
        val current = transactionDao.getAll().map { it.toDomain() }
        val old = draft.id?.let { id -> current.find { it.id == id } ?: return OperationResult.NotFound }

        val candidate = draft.toDomain()
        val simulated = current.filterNot { it.id == draft.id } + candidate
        val affected = setOfNotNull(account.id, old?.accountId)
        FinanceRules.firstOverdrawn(allAccounts, simulated, affected)?.let {
            return OperationResult.Error(FinanceRules.overdraftMessage(it))
        }

        if (draft.id == null) {
            transactionDao.insert(candidate.toEntity())
        } else {
            transactionDao.update(candidate.toEntity())
        }

        val base = if (draft.id == null) {
            "${candidate.kind} ${formatMoney(candidate.amount)} saved"
        } else {
            "Entry updated"
        }
        val warning = if (candidate is Expense) {
            FinanceRules.budgetWarning(
                candidate.category,
                simulated,
                budgetDao.getAll().map { it.toDomain() }
            )
        } else {
            null
        }
        return OperationResult.Success(listOfNotNull(base, warning).joinToString(" · "))
    }

    override suspend fun deleteTransaction(id: Int): OperationResult {
        val existing = transactionDao.getById(id) ?: return OperationResult.NotFound
        val allAccounts = accountDao.getAll().map { it.toDomain() }
        val remaining = transactionDao.getAll().map { it.toDomain() }.filterNot { it.id == id }
        FinanceRules.firstOverdrawn(allAccounts, remaining, setOf(existing.accountId))?.let {
            return OperationResult.Error("Can't delete this income. ${FinanceRules.overdraftMessage(it)}")
        }
        transactionDao.delete(existing)
        return OperationResult.Success("Entry deleted")
    }

    override suspend fun restoreTransaction(transaction: Transaction): OperationResult {
        if (accountDao.getById(transaction.accountId) == null) {
            return OperationResult.Error("Can't restore: its account no longer exists")
        }
        transactionDao.insert(transaction.toEntity())
        return OperationResult.Success("Entry restored")
    }

    override suspend fun saveAccount(draft: AccountDraft): OperationResult {
        val name = draft.name.trim()
        if (name.isEmpty()) return OperationResult.Error("Account name is required")
        if (name.length > FinanceRules.MAX_NAME_LENGTH) {
            return OperationResult.Error("Name is too long")
        }

        val all = accountDao.getAll()
        if (all.any { it.id != draft.id && it.name.equals(name, ignoreCase = true) }) {
            return OperationResult.Error("An account named \"$name\" already exists")
        }
        if (draft.type != AccountType.CARD && draft.startingBalance < 0) {
            return OperationResult.Error("Only a card can start with a negative balance")
        }
        if (draft.creditLimit < 0) return OperationResult.Error("Credit limit can't be negative")

        val entity = draft.copy(name = name).toEntity()

        if (draft.id == null) {
            if (all.size >= FinanceRules.MAX_ACCOUNTS) {
                return OperationResult.Error("You can have up to ${FinanceRules.MAX_ACCOUNTS} accounts")
            }
            accountDao.insert(entity)
            return OperationResult.Success("Account \"$name\" added")
        }

        if (all.none { it.id == draft.id }) return OperationResult.NotFound
        val updatedAccounts = all.map { if (it.id == draft.id) entity else it }.map { it.toDomain() }
        val txs = transactionDao.getAll().map { it.toDomain() }
        FinanceRules.firstOverdrawn(updatedAccounts, txs, setOf(draft.id))?.let {
            return OperationResult.Error(FinanceRules.overdraftMessage(it))
        }
        accountDao.update(entity)
        return OperationResult.Success("Account \"$name\" updated")
    }

    override suspend fun deleteAccount(id: Int): OperationResult {
        val account = accountDao.getById(id) ?: return OperationResult.NotFound
        val count = transactionDao.countForAccount(id)
        if (count > 0) {
            return OperationResult.Error(
                "\"${account.name}\" has $count entries. Delete them or move them to another account first"
            )
        }
        accountDao.delete(account)
        return OperationResult.Success("Account \"${account.name}\" deleted")
    }

    override suspend fun saveBudget(category: String, monthlyLimit: Double, isNew: Boolean): OperationResult {
        if (category !in Categories.expense) return OperationResult.Error("Choose an expense category")
        if (monthlyLimit <= 0) return OperationResult.Error("Limit must be greater than 0")
        if (isNew && budgetDao.getByCategory(category) != null) {
            return OperationResult.Error("\"$category\" already has a budget. Edit it instead")
        }
        budgetDao.upsert(BudgetEntity(category, monthlyLimit))
        return OperationResult.Success(
            if (isNew) {
                "Budget for \"$category\" set to ${formatMoney(monthlyLimit)}"
            } else {
                "Budget for \"$category\" updated"
            }
        )
    }

    override suspend fun deleteBudget(category: String): OperationResult {
        val budget = budgetDao.getByCategory(category) ?: return OperationResult.NotFound
        budgetDao.delete(budget)
        return OperationResult.Success("Budget for \"$category\" removed")
    }

    override suspend fun loadDemoMonth(): OperationResult {
        delay(1500)
        val all = accountDao.getAll().map { it.toDomain() }
        if (all.isEmpty()) return OperationResult.Error("Add an account first")

        val cash = all.firstOrNull { it is CashAccount } ?: all.first()
        val card = all.firstOrNull { it is CardAccount } ?: cash
        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000

        val demo = listOf(
            demoDraft(card, TransactionKind.INCOME, "Salary", 45000.0, "стипендия за месяц", now - 5 * day),
            demoDraft(card, TransactionKind.EXPENSE, "Food", 1800.0, "кофе с Алией после пары", now - day),
            demoDraft(cash, TransactionKind.EXPENSE, "Transport", 400.0, "автобус до универа", now - 2 * day),
            demoDraft(card, TransactionKind.EXPENSE, "Study", 3500.0, "распечатала конспекты", now - 3 * day),
            demoDraft(card, TransactionKind.EXPENSE, "Fun", 5200.0, "кино в выходные", now - 6 * day)
        )
        val added = demo.count { saveTransaction(it) is OperationResult.Success }
        return if (added == 0) {
            OperationResult.Error("Demo entries don't fit your current balances")
        } else {
            OperationResult.Success("Added $added demo entries")
        }
    }

    private fun demoDraft(
        account: Account,
        kind: String,
        category: String,
        amount: Double,
        note: String,
        timestampMillis: Long
    ) = TransactionDraft(null, kind, account.id, category, amount, note, timestampMillis)
}
