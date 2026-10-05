import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    val library = createLibrary()
    println("=== Welcome to ${library.name} ===")

    var running = true
    while (running) {
        printMenu()
        when (readLine()?.trim()) {
            "1" -> printItems("All items", library.allItems())
            "2" -> printItems("Available items", library.findItems { it.isAvailable })
            "3" -> searchByTitle(library)
            "4" -> borrowItem(library)
            "5" -> returnItem(library)
            "6" -> showLoans(library)
            "7" -> showStatistics(library)
            "8" -> loadNewArrivals(library)
            "0", null -> running = false
            else -> println("Unknown option, try again")
        }
    }
    println("Goodbye!")
}

fun createLibrary(): Library {
    val library = Library("City Library")

    val books = listOf(
        Book(1, "Clean Code", 2008, "Robert Martin", 464),
        Book(2, "Clean Architecture", 2017, "Robert Martin", 432),
        Book(3, "Effective Java", 2018, "Joshua Bloch", 412),
        Book(4, "The Pragmatic Programmer", 1999, "Andrew Hunt", 352)
    )
    books.forEach { library.addItem(it) }
    library.addItem(Magazine(5, "National Geographic", 2023, 7))
    library.addItem(Dvd(6, "Interstellar", 2014, 169))

    library.addMember(Member(1, "Adina", "adina@mail.com"))
    library.addMember(Member(2, "Aliya", "aliya@mail.com"))
    library.addMember(Member(3, "Timur", "timur@mail.com"))
    return library
}

fun printMenu() {
    println()
    println("1. Show all items")
    println("2. Show available items")
    println("3. Search by title")
    println("4. Borrow an item")
    println("5. Return an item")
    println("6. Show active loans")
    println("7. Statistics")
    println("8. Load new arrivals (coroutine)")
    println("0. Exit")
    print("Choose an option: ")
}

fun printItems(
    header: String,
    items: List<LibraryItem>,
    formatter: (LibraryItem) -> String = { it.describe() }
) {
    println("--- $header (${items.size}) ---")
    if (items.isEmpty()) {
        println("Nothing found")
        return
    }
    for (item in items) {
        println(formatter(item))
    }
}

fun searchByTitle(library: Library) {
    print("Enter part of the title: ")
    val query = readLine().orEmpty().trim()
    val found = library.findItems { it.title.contains(query, ignoreCase = true) }
    printItems("Search results for \"$query\"", found)
}

fun borrowItem(library: Library) {
    printItems("Available items", library.findItems { it.isAvailable }) { "[${it.id}] ${it.title}" }
    println("Members: " + library.allMembers().joinToString { "${it.id}=${it.name}" })
    val itemId = readNumber("Item id: ") ?: return
    val memberId = readNumber("Member id: ") ?: return
    printResult(library.borrow(itemId, memberId))
}

fun returnItem(library: Library) {
    showLoans(library)
    val itemId = readNumber("Item id to return: ") ?: return
    val days = readNumber("How many days was it kept? ") ?: return
    printResult(library.returnItem(itemId, days))
}

fun showLoans(library: Library) {
    val loans = library.activeLoans()
    println("--- Active loans (${loans.size}) ---")
    loans.forEach { println("[${it.item.id}] ${it.item.title} -> ${it.member.name}") }
}

fun showStatistics(library: Library) {
    val items = library.allItems()
    val available = items.count { it.isAvailable }
    val oldest = items.minByOrNull { it.year }
    val titlesUpper = items.map { it.title.uppercase() }

    println("--- Statistics ---")
    println("Total items: ${items.size}, available: $available, borrowed: ${items.size - available}")
    println("Items by type: ${library.countByType()}")
    println("Unique authors (Set): ${library.authors()}")
    println("Total pages of all books (reduce): ${library.totalPages()}")
    println("Oldest item: ${oldest?.title} (${oldest?.year})")
    println("Titles (map): $titlesUpper")

    val fines = library.finesByMember()
    if (fines.isEmpty()) {
        println("No fines")
    } else {
        fines.forEach { (name, amount) -> println("Fine for $name: $%.2f".format(amount)) }
    }
}

fun loadNewArrivals(library: Library) = runBlocking {
    println("Connecting to the publisher server...")

    val arrivals = async { library.fetchNewArrivals() }
    val loading = launch {
        repeat(10) {
            print(".")
            delay(200)
        }
    }

    val newItems = arrivals.await()
    loading.cancel()
    println()

    newItems.forEach { library.addItem(it) }
    printItems("New arrivals added", newItems)
}

fun printResult(result: LibraryResult) {
    when (result) {
        is LibraryResult.Success -> println("OK: ${result.message}")
        is LibraryResult.Error -> println("Error: ${result.reason}")
        LibraryResult.NotFound -> println("Error: item not found")
    }
}

fun readNumber(prompt: String): Int? {
    print(prompt)
    val number = readLine()?.trim()?.toIntOrNull()
    if (number == null) println("Please enter a valid number")
    return number
}
