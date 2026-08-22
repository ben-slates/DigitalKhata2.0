@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ben.khata

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.ben.khata.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private const val Currency = "Rs."
private fun money(value: Long) = if (value < 0) "-$Currency ${NumberFormat.getIntegerInstance(Locale.US).format(-value)}" else "$Currency ${NumberFormat.getIntegerInstance(Locale.US).format(value)}"
private fun date(value: Long) = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(value))
private fun day(value: Long) = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(value))


// ── Premium Dark Fintech Palette ──
val FintechBackground = Color(0xFF000000)
val FintechCard = Color(0xFF101010)
val FintechElevated = Color(0xFF1A1A1A)
val FintechPrimary = Color(0xFFD4AF37)
val FintechPrimaryLight = Color(0xFFF3E5AB)
val FintechPrimaryDark = Color(0xFFAA8A25)
val FintechText = Color(0xFFFFFFFF)
val FintechTextSecondary = Color(0xFFB0B0B0)
val FintechTextMuted = Color(0xFF757575)
val FintechBorder = Color(0xFF332A00)
val FintechExpense       = Color(0xFFEF4444)
val FintechWarning       = Color(0xFFF59E0B)

fun hisaabLightColorScheme() = darkColorScheme(
    primary = FintechPrimary,
    onPrimary = FintechBackground,
    secondary = FintechExpense,
    onSecondary = Color.White,
    tertiary = FintechPrimary,
    onTertiary = FintechBackground,
    background = FintechBackground,
    onBackground = FintechText,
    surface = FintechCard,
    onSurface = FintechText,
    surfaceVariant = FintechElevated,
    onSurfaceVariant = FintechTextSecondary,
    outline = FintechTextMuted,
    primaryContainer = FintechPrimary.copy(alpha = 0.12f),
    onPrimaryContainer = FintechPrimaryLight,
    secondaryContainer = FintechExpense.copy(alpha = 0.12f),
    onSecondaryContainer = FintechExpense,
    tertiaryContainer = FintechPrimary.copy(alpha = 0.12f),
    onTertiaryContainer = FintechPrimaryLight,
    error = FintechExpense,
    onError = Color.White,
    errorContainer = FintechExpense.copy(alpha = 0.12f),
    onErrorContainer = Color(0xFFFCA5A5),
    outlineVariant = FintechBorder
)

fun hisaabDarkColorScheme() = darkColorScheme(
    primary = FintechPrimary,
    onPrimary = FintechBackground,
    secondary = FintechExpense,
    onSecondary = Color.White,
    tertiary = FintechPrimary,
    onTertiary = FintechBackground,
    background = FintechBackground,
    onBackground = FintechText,
    surface = FintechCard,
    onSurface = FintechText,
    surfaceVariant = FintechElevated,
    onSurfaceVariant = FintechTextSecondary,
    outline = FintechTextMuted,
    primaryContainer = FintechPrimary.copy(alpha = 0.12f),
    onPrimaryContainer = FintechPrimaryLight,
    secondaryContainer = FintechExpense.copy(alpha = 0.12f),
    onSecondaryContainer = FintechExpense,
    tertiaryContainer = FintechPrimary.copy(alpha = 0.12f),
    onTertiaryContainer = FintechPrimaryLight,
    error = FintechExpense,
    onError = Color.White,
    errorContainer = FintechExpense.copy(alpha = 0.12f),
    onErrorContainer = Color(0xFFFCA5A5),
    outlineVariant = FintechBorder
)

class KhataViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = KhataDatabase.create(app).dao()
    val people = dao.people().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val total = dao.total().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val peopleCount = dao.peopleCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val recent = dao.recent().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val history = dao.history().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val budget = dao.budget().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val reminders = dao.reminders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun add(name: String, amount: Long, note: String, personId: Long? = null, imageUri: String? = null) = viewModelScope.launch { dao.addEntry(name, amount, note, personId, imageUri) }
    fun update(tx: KhataTransaction, amount: Long, note: String) = viewModelScope.launch { dao.updateTransaction(tx.id, amount, note) }
    fun delete(tx: KhataTransaction) = viewModelScope.launch { dao.deleteTransaction(tx.id) }
    fun deletePerson(id: Long) = viewModelScope.launch { dao.deletePerson(id) }
    fun saveBudget(amount: Long) = viewModelScope.launch { dao.saveSettings(AppSettings(totalBudget = amount)) }
    fun addToBudget(amount: Long) = viewModelScope.launch { dao.addToBudget(amount) }
    fun detail(id: Long) = dao.person(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    fun entries(id: Long) = dao.transactions(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {
        setContent {
            val preferences = remember { getSharedPreferences("settings", MODE_PRIVATE) }
            var theme by remember { mutableStateOf("dark") }
            MaterialTheme(colorScheme = hisaabDarkColorScheme()) {
                KhataApp(
                    theme = theme,
                    setTheme = { value ->
                        theme = value
                        preferences.edit().putString("theme", value).apply()
                    }
                )
            }
        }
    }
}


@Composable private fun WelcomeScreen(onNameEntered: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                Text("H", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(32.dp))
            Text("Welcome to Hisaab", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Your simple digital Khata.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(48.dp))
            OutlinedTextField(
                value = name, 
                onValueChange = { name = it; error = false }, 
                label = { Text("What should we call you?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = error,
                shape = RoundedCornerShape(12.dp)
            )
            if (error) {
                Text("Please enter your name to continue", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp).align(Alignment.Start))
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { 
                    if (name.trim().isEmpty()) error = true else onNameEntered(name.trim()) 
                }, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue", fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp), fontWeight = FontWeight.Bold)
            }
        }
    }
}



@Composable fun Avatar(name: String, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 48.dp) {
    val colors = listOf(Color(0xFF2D8B5C), Color(0xFF3B7FA8), Color(0xFF7C5CA8), Color(0xFF5C8FA8), Color(0xFF8B6B4E), Color(0xFF4E8B7A), Color(0xFF6B7C8B), Color(0xFF8B5C6B), Color(0xFF5C8B6B), Color(0xFF7A8B4E))
    val color = colors[kotlin.math.abs(name.hashCode()) % colors.size]
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
    Box(modifier = modifier.size(size).background(color, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
        Text(initial, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable private fun KhataApp(theme: String, setTheme: (String) -> Unit, vm: KhataViewModel = viewModel()) {
    val preferences = androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
    var userName by remember { mutableStateOf(preferences.getString("user_name", null)) }
    
    if (userName == null) {
        WelcomeScreen { name ->
            preferences.edit().putString("user_name", name).apply()
            userName = name
        }
        return
    }

    val nav = rememberNavController()

    val tabs = listOf("home" to "Home", "people" to "Khata", "add" to "Add", "reminders" to "Reminders", "settings" to "Settings")
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    fun goToRoot(route: String) {
        if (currentRoute == route) return
        if (route == "add") { nav.navigate("add"); return }
        if (!nav.popBackStack(route, inclusive = false)) {
            nav.navigate(route) { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true }
        }
    }
    Scaffold(bottomBar = { 
        Surface(color = FintechCard.copy(alpha = 0.95f), shadowElevation = 16.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), border = androidx.compose.foundation.BorderStroke(1.dp, FintechBorder.copy(alpha = 0.5f))) {
            Row(Modifier.fillMaxWidth().height(80.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                tabs.forEach { (route, label) ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (route == "add") {
                            FloatingActionButton(onClick = { goToRoot("add") }, shape = androidx.compose.foundation.shape.CircleShape, containerColor = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp)) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                            }
                        } else {
                            val isSelected = currentRoute == route
                            val icon = when (route) { "home" -> Icons.Default.Home; "people" -> Icons.Default.People; "reminders" -> Icons.Default.Notifications; else -> Icons.Default.Settings }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { goToRoot(route) }.padding(8.dp)) {
                                Icon(icon, contentDescription = label, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                
                            }
                        }
                    }
                }
            }
        }
    }) { padding ->
        var horizontalDrag by remember(currentRoute) { mutableFloatStateOf(0f) }
        val rootIndex = tabs.indexOfFirst { it.first == currentRoute }
        val swipeModifier = if (rootIndex >= 0) Modifier.pointerInput(currentRoute) {
            detectHorizontalDragGestures(
                onHorizontalDrag = { _, delta -> horizontalDrag += delta },
                onDragEnd = {
                    if (abs(horizontalDrag) > 120f) {
                        val next = if (horizontalDrag < 0) rootIndex + 1 else rootIndex - 1
                        if (next in tabs.indices) goToRoot(tabs[next].first)
                    }
                    horizontalDrag = 0f
                },
                onDragCancel = { horizontalDrag = 0f }
            )
        } else Modifier
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(padding).then(swipeModifier),
            enterTransition = { slideInHorizontally(animationSpec = tween(220)) { it / 7 } + fadeIn(tween(180)) },
            exitTransition = { slideOutHorizontally(animationSpec = tween(180)) { -it / 10 } + fadeOut(tween(140)) },
            popEnterTransition = { slideInHorizontally(animationSpec = tween(220)) { -it / 7 } + fadeIn(tween(180)) },
            popExitTransition = { slideOutHorizontally(animationSpec = tween(180)) { it / 10 } + fadeOut(tween(140)) }
        ) {
        composable("home") { Home(vm, { nav.navigate("add") }, { nav.navigate("person/$it") }, navToReminders = { goToRoot("reminders") }, navToKhata = { goToRoot("people") }) }
        composable("reminders") { RemindersScreen(vm, { nav.popBackStack() }, { nav.navigate("add_reminder") }) }
        composable("add_reminder") { AddReminderScreen(vm) { nav.popBackStack() } }
        composable("people") { People(vm) { nav.navigate("person/$it") } }
        composable("history") { History(vm) { nav.navigate("person/$it") } }
        composable("settings") { Settings(vm, theme, setTheme) }
        composable("add") { EntryForm("Add entry", { nav.popBackStack() }) { name, amount, note, imageUri -> vm.add(name, amount, note, null, imageUri); nav.popBackStack() } }
        composable("person/{id}", listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val id = entry.arguments!!.getLong("id")
            PersonScreen(vm, id, { nav.popBackStack() }, { nav.navigate("add/$id") })
        }
        composable("add/{id}", listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val id = entry.arguments!!.getLong("id")
            val personFlow = remember(id) { vm.detail(id) }
            val person by personFlow.collectAsState()
            EntryForm("Add amount", { nav.popBackStack() }, person?.name) { _, amount, note, imageUri -> vm.add("", amount, note, id, imageUri); nav.popBackStack() }
        }
        }
    }
}

@Composable private fun Home(vm: KhataViewModel, add: () -> Unit, open: (Long) -> Unit, navToReminders: () -> Unit = {}, navToKhata: () -> Unit = {}) {
    val recent by vm.recent.collectAsState(); val people by vm.people.collectAsState()
    val reminders by vm.reminders.collectAsState(emptyList())
    val budget by vm.budget.collectAsState()
    
    // Compute from people totals (real-time, updates when transactions change)
    val toReceive = people.filter { it.total > 0 }.sumOf { it.total }
    val toPay = people.filter { it.total < 0 }.sumOf { kotlin.math.abs(it.total) }
    
    val pending = reminders.filter { it.status == 0 }
    val today = remember { java.util.Calendar.getInstance().apply { set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis }
    val overdueAmount = pending.sumOf { it.amount }
    val upcoming = pending.sortedBy { it.dueDate }.take(5)
    
    Scaffold(topBar = { AppHeader("Hello, ${androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE).getString("user_name", "")} 👋", "Good morning") }) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            
            // Summary 4 boxes in 2x2 grid
            item {
                Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard("Received", money(toReceive), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
                        SummaryCard("Paid", money(toPay), Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard("Pending", money(overdueAmount), Modifier.weight(1f), MaterialTheme.colorScheme.error)
                        SummaryCard("Budget", money(budget ?: 0), Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            // Upcoming Payments
            if (upcoming.isNotEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Upcoming Payments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = navToReminders) { Text("View All", color = MaterialTheme.colorScheme.primary) }
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(upcoming) { r ->
                                Card(Modifier.width(260.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = FintechElevated), border = androidx.compose.foundation.BorderStroke(1.dp, FintechBorder)) {
                                    Column(Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Avatar(r.personName, size = 40.dp)
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(r.personName, color = FintechText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(if (r.dueDate < today + 86400000L) "Due Today" else "In ${((r.dueDate - today)/86400000L).coerceAtLeast(1)} days", color = FintechTextMuted, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(if (r.type == 0) "Paid" else "Received", color = if (r.type == 0) FintechExpense else FintechPrimary, style = MaterialTheme.typography.labelMedium)
                                            Text(money(r.amount), color = FintechText, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Recent Transactions
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = navToKhata) { Text("View All", color = MaterialTheme.colorScheme.primary) }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (recent.isEmpty()) {
                        Empty("No recent transactions")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            recent.forEach { tx ->
                                val person = people.find { it.id == tx.personId }
                                val pName = person?.name ?: "Unknown"
                                Card(Modifier.fillMaxWidth().clickable { open(tx.personId) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Avatar(pName)
                                        Spacer(Modifier.width(16.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(pName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(date(tx.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            val isReceive = tx.amount > 0
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(if (isReceive) "+" else "-", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = if (isReceive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary)
                                                Spacer(Modifier.width(2.dp))
                                                Text(money(kotlin.math.abs(tx.amount)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = if (isReceive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun People(vm: KhataViewModel, open: (Long) -> Unit) {
    val people by vm.people.collectAsState()
    
    val toReceive = people.filter { it.total > 0 }
    val toPay = people.filter { it.total < 0 }
    val settled = people.filter { it.total == 0L }

    Scaffold(topBar = { TopAppBar(title = { Text("Khata", fontWeight = FontWeight.ExtraBold) }, actions = { IconButton({}) { Icon(Icons.Default.Search, "Search") } }) }) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (people.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("No customers yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Add your first customer to start managing your khata.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                if (toReceive.isNotEmpty()) {
                    item { Text("To Receive (${toReceive.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
                    items(toReceive, key = { it.id }) { p -> PersonCard(p, open) }
                }
                if (toPay.isNotEmpty()) {
                    item { Text("To Pay (${toPay.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
                    items(toPay, key = { it.id }) { p -> PersonCard(p, open) }
                }
                if (settled.isNotEmpty()) {
                    item { Text("Settled (${settled.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
                    items(settled, key = { it.id }) { p -> PersonCard(p, open) }
                }
            }
        }
    }
}

@Composable private fun PersonCard(p: com.ben.khata.data.PersonSummary, open: (Long) -> Unit) {
    val isReceive = p.total > 0
    val isSettled = p.total == 0L
    val status = if (isSettled) "Settled" else if (isReceive) "Received" else "Paid"
    val statusColor = if (isSettled) MaterialTheme.colorScheme.outline else if (isReceive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
    
    Card(Modifier.fillMaxWidth().clickable { open(p.id) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(p.name)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(p.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(status, style = MaterialTheme.typography.labelSmall, color = statusColor)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(money(kotlin.math.abs(p.total)), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = statusColor)
            }
        }
    }
}

@Composable private fun History(vm: KhataViewModel, open: (Long) -> Unit) {
    val history by vm.history.collectAsState()
    val groups = history.groupBy { day(it.createdAt) }
    Scaffold(topBar = { AppHeader("Daily history", "Payments grouped by day") }) { padding -> Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        if (history.isEmpty()) Empty("No payment history yet.") else LazyColumn(contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            groups.forEach { (label, entries) -> item(label) {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(money(entries.sumOf { it.amount }), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        entries.forEach { tx -> Row(Modifier.fillMaxWidth().clickable { open(tx.personId) }.padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text(tx.personName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium); if (tx.imageUri != null) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Image, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(4.dp)); Text("Bill attached", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) } } } ; Text(money(tx.amount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) } }
                    }
                }
            } }
        }
    } }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun PersonScreen(vm: KhataViewModel, id: Long, back: () -> Unit, add: () -> Unit) {
    val personFlow = remember(id) { vm.detail(id) }
    val entriesFlow = remember(id) { vm.entries(id) }
    val person by personFlow.collectAsState()
    val entries by entriesFlow.collectAsState()
    
    var viewTx by remember { mutableStateOf<KhataTransaction?>(null) }
    var deletePerson by remember { mutableStateOf(false) }
    
    if (viewTx != null) {
        val tx = viewTx!!
        val isReceive = tx.amount > 0
        var edit by remember { mutableStateOf(false) }
        var confirmDel by remember { mutableStateOf(false) }
        
        if (edit) {
            EditDialog(tx, { edit = false }) { amt, n -> vm.update(tx, amt, n); edit = false; viewTx = tx.copy(amount = amt, note = n) }
        }
        
        if (confirmDel) {
            AlertDialog({ confirmDel = false }, title = { Text("Delete Transaction?") }, confirmButton = { TextButton({ vm.delete(tx); confirmDel = false; viewTx = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton({ confirmDel = false }) { Text("Cancel") } })
        }
        
        Scaffold(topBar = { TopAppBar(title = { Text("Transaction Details") }, navigationIcon = { IconButton({ viewTx = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }) { padding ->
            Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Avatar(person?.name ?: "", size = 64.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(person?.name ?: "", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(if (isReceive) "Receive" else "Pay", color = Color.White.copy(alpha=0.8f), modifier = Modifier.background(Color.Black.copy(alpha=0.2f), RoundedCornerShape(4.dp)).padding(horizontal=8.dp, vertical=4.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(money(kotlin.math.abs(tx.amount)), color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
                        Text(date(tx.createdAt), color = Color.White.copy(alpha=0.7f), style = MaterialTheme.typography.labelMedium)
                    }
                }
                
                                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row { Text("Person:", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text(person?.name ?: "", fontWeight = FontWeight.Bold) }
                        Row { Text("Type:", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text(if (isReceive) "Receive" else "Pay", fontWeight = FontWeight.Bold) }
                        Row { Text("Amount:", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text(money(kotlin.math.abs(tx.amount)), fontWeight = FontWeight.Bold) }
                        if (tx.note.isNotBlank()) {
                            Row { Text("Note:", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text(tx.note, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        }
                    }
                }
                
                if (tx.imageUri != null) {
                    var showBill by remember { mutableStateOf(false) }
                    if (showBill) {
                        androidx.compose.ui.window.Dialog(onDismissRequest = { showBill = false }) {
                            OutlinedCard(shape = RoundedCornerShape(16.dp)) {
                                coil.compose.AsyncImage(model = java.io.File(tx.imageUri), contentDescription = "Bill", modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                            }
                        }
                    }
                    OutlinedButton(onClick = { showBill = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("View Attached Bill")
                    }
                }
                
                Spacer(Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { edit = true }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(12.dp)) { Text("Edit", fontWeight = FontWeight.Bold) }
                    OutlinedButton(onClick = { confirmDel = true }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete", fontWeight = FontWeight.Bold) }
                }
            }
        }
        return
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(person?.name ?: "") },
            navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            actions = { IconButton({ deletePerson = true }) { Icon(Icons.Default.Delete, "Delete person") } }
        )
    }, floatingActionButton = {
        FloatingActionButton(add, shape = RoundedCornerShape(16.dp), containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Default.Add, "Add Transaction", tint = Color.White) }
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            val total = person?.total ?: 0
            val isReceive = total > 0
            val isSettled = total == 0L
            val statusColor = if (isSettled) MaterialTheme.colorScheme.outline else if (isReceive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
            
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(person?.name ?: "", size = 64.dp)
                Spacer(Modifier.height(8.dp))
                Text(person?.name ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(if (isSettled) "Settled" else if (isReceive) "Received" else "Paid", style = MaterialTheme.typography.labelMedium, color = statusColor)
                Text(money(kotlin.math.abs(total)), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = statusColor)
            }
            
            Text("Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(8.dp))
            
            if (entries.isEmpty()) {
                Empty("No transactions yet.")
            } else {
                LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(entries, key = { it.id }) { tx -> 
                        Card(Modifier.fillMaxWidth().clickable { viewTx = tx }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(48.dp).background(if (tx.amount < 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Text(if (tx.amount < 0) "PAY" else "REC", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (tx.amount < 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(if (tx.amount > 0) "Received" else "Paid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(date(tx.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(money(kotlin.math.abs(tx.amount)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = if (tx.amount > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }
    if (deletePerson) AlertDialog({ deletePerson = false }, title = { Text("Delete ${person?.name ?: "person"}?") }, text = { Text("All of this person's transactions will be deleted. Totals and remaining budget will update automatically.") }, confirmButton = { TextButton({ vm.deletePerson(id); deletePerson = false; back() }) { Text("Delete", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton({ deletePerson = false }) { Text("Cancel") } })
}

@Composable private fun Settings(vm: KhataViewModel, theme: String, setTheme: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
    var userName by remember { mutableStateOf(prefs.getString("user_name", "") ?: "") }
    var notifEnabled by remember { mutableStateOf(prefs.getBoolean("notif_enabled", true)) }
    var editingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    val budget by vm.budget.collectAsState()
    var editing by remember { mutableStateOf(false) }
    var addingBudget by remember { mutableStateOf(false) }
    var showingAbout by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    val history by vm.history.collectAsState(emptyList())
    
    if (editingName) AlertDialog(
        onDismissRequest = { editingName = false }, 
        title = { Text("Personal Information") }, 
        text = { 
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(tempName, { tempName = it }, label = { Text("Your Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                
            }
        }, 
        confirmButton = { TextButton({ prefs.edit().putString("user_name", tempName).apply(); userName = tempName; editingName = false }) { Text("Save") } }, 
        dismissButton = { TextButton({ editingName = false }) { Text("Cancel") } }
    )
    if (editing) BudgetDialog(budget ?: 0, { editing = false }) { vm.saveBudget(it); editing = false }
    if (addingBudget) AddBudgetDialog({ addingBudget = false }) { vm.addToBudget(it); addingBudget = false }
    if (showingAbout) AlertDialog(
        onDismissRequest = { showingAbout = false },
        title = { Text("About DigitalKhata", fontWeight = FontWeight.Bold) },
        text = { 
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Version 1.0", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text("DigitalKhata is a premium finance and transaction tracker. Manage your Khata, track payments, and never miss a due date with built-in smart reminders.")
            }
        },
        confirmButton = { TextButton({ showingAbout = false }) { Text("Close") } }
    )

    Scaffold(topBar = { TopAppBar(title = { Text("Settings", fontWeight = FontWeight.ExtraBold) }) }) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(userName, size = 64.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(userName.ifBlank { "Your Name" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            item {
                Column {
                    Text("General", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column {
                            Row(Modifier.fillMaxWidth().clickable { tempName = userName; editingName = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(16.dp)); Text("Personal Information", Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha=0.1f))
                            Row(Modifier.fillMaxWidth().clickable { editing = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(16.dp)); Text("Set Total Khata", Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha=0.1f))
                            Row(Modifier.fillMaxWidth().clickable { addingBudget = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AddCircleOutline, null, tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(16.dp)); Text("Add Amount to Khata", Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha=0.1f))
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AttachMoney, null, tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(16.dp)); Text("Currency", Modifier.weight(1f)); Text("PKR", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha=0.1f))
                            Row(Modifier.fillMaxWidth().clickable { showHistory = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.ReceiptLong, null, tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(16.dp)); Text("View Transaction History", Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }
            
            item {
                Column {
                    Text("Preferences", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(16.dp)); Text("Notifications", Modifier.weight(1f)); androidx.compose.material3.Switch(checked = notifEnabled, onCheckedChange = { notifEnabled = it; prefs.edit().putBoolean("notif_enabled", it).apply() }) }
                        }
                    }
                }
            }
            
            item {
                Column {
                    Text("About", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column {
                            Row(Modifier.fillMaxWidth().clickable { showingAbout = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(16.dp)); Text("About DigitalKhata", Modifier.weight(1f)) }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha=0.1f))
                            Row(Modifier.fillMaxWidth().clickable {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:umerf2700@gmail.com")
                                }
                                context.startActivity(intent)
                            }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Help, null, tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(16.dp)); Text("Help & Support", Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }
            
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Text("Hisaab App v1.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showHistory) {
        val grouped = remember(history) {
            history.groupBy { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(it.createdAt)) }
        }
        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Transaction History", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton({ showHistory = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }) { pad ->
            if (history.isEmpty()) {
                Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Text("No transactions yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    grouped.forEach { (dayLabel, txns) ->
                        item {
                            Text(dayLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
                        }
                        items(txns) { tx ->
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(40.dp).background(if (tx.amount >= 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                        Text(if (tx.amount >= 0) "+" else "−", fontWeight = FontWeight.Bold, color = if (tx.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(tx.personName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (tx.note.isNotBlank()) Text(tx.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(money(kotlin.math.abs(tx.amount)), fontWeight = FontWeight.Bold, color = if (tx.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                                        Text(if (tx.amount >= 0) "Received" else "Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable private fun EntryForm(title: String, back: () -> Unit, fixedName: String? = null, save: (String, Long, String, String?) -> Unit) {
    var name by rememberSaveable { mutableStateOf(fixedName.orEmpty()) }; var amount by rememberSaveable { mutableStateOf("") }; var note by rememberSaveable { mutableStateOf("") }; var error by rememberSaveable { mutableStateOf<String?>(null) }
    var isReceive by rememberSaveable { mutableStateOf(true) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = copyImageToInternalStorage(context, uri)
    }
    
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) imageUri = tempCameraUri.toString()
    }

    LaunchedEffect(fixedName) { if (!fixedName.isNullOrBlank()) name = fixedName }
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (fixedName == null) {
                OutlinedTextField(name, { name = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Person name") }, singleLine = true, shape = RoundedCornerShape(12.dp))
            } else {
                GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Adding transaction for", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(fixedName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) } }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = isReceive, onClick = { isReceive = true }, label = { Text("Receive") }, modifier = Modifier.weight(1f), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White))
                FilterChip(selected = !isReceive, onClick = { isReceive = false }, label = { Text("Pay") }, modifier = Modifier.weight(1f), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White))
            }
            
            OutlinedTextField(amount, { amount = it.filter(Char::isDigit); error = null }, Modifier.fillMaxWidth(), label = { Text("Amount (PKR)") }, prefix = { Text("$Currency ") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), label = { Text("Note (optional)") }, minLines = 2, shape = RoundedCornerShape(12.dp))
            
            if (!isReceive) {
                if (imageUri == null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Gallery")
                        }
                        OutlinedButton(onClick = {
                            val tmpFile = java.io.File.createTempFile("bill_", ".jpg", context.cacheDir)
                            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", tmpFile)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Camera")
                        }
                    }
                } else {
                    OutlinedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            coil.compose.AsyncImage(model = imageUri, contentDescription = "Bill", modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                            Spacer(Modifier.width(16.dp))
                            Text("Bill attached", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            IconButton({ imageUri = null }) { Icon(Icons.Default.Delete, "Remove bill", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
            
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Button(onClick = {
                val value = amount.toLongOrNull()
                val personName = fixedName ?: name
                error = when { personName.trim().isEmpty() -> "Enter a person name."; value == null || value <= 0 -> "Enter an amount greater than zero."; else -> null }
                if (error == null) save(personName, if (isReceive) value!! else -value!!, note, if (isReceive) null else imageUri)
            }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text(if (fixedName == null) "Save entry" else "Add amount", fontWeight = FontWeight.Bold, fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp)) }
        }
    }
}

@Composable private fun BudgetDialog(current: Long, dismiss: () -> Unit, save: (Long) -> Unit) { var input by remember { mutableStateOf(if (current == 0L) "" else current.toString()) }; var error by remember { mutableStateOf(false) }; AlertDialog({ dismiss() }, title = { Text("Set total budget", fontWeight = FontWeight.Bold) }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(input, { input = it.filter(Char::isDigit); error = false }, label = { Text("Budget (PKR)") }, prefix = { Text("$Currency ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), singleLine = true); if (error) Text("Enter a valid budget.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) } }, confirmButton = { TextButton({ val value = input.toLongOrNull(); if (value == null) error = true else save(value) }) { Text("Save", fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(dismiss) { Text("Cancel") } }) }
@Composable private fun AddBudgetDialog(dismiss: () -> Unit, add: (Long) -> Unit) { var input by remember { mutableStateOf("") }; var error by remember { mutableStateOf(false) }; AlertDialog({ dismiss() }, title = { Text("Add to total budget", fontWeight = FontWeight.Bold) }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("This amount will be added to your current total budget.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); OutlinedTextField(input, { input = it.filter(Char::isDigit); error = false }, label = { Text("Amount to add (PKR)") }, prefix = { Text("$Currency ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), singleLine = true); if (error) Text("Enter an amount greater than zero.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) } }, confirmButton = { TextButton({ val value = input.toLongOrNull(); if (value == null || value <= 0) error = true else add(value) }) { Text("Add", fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(dismiss) { Text("Cancel") } }) }
@Composable private fun EditDialog(tx: KhataTransaction, dismiss: () -> Unit, save: (Long, String) -> Unit) { 
    var amount by remember { mutableStateOf(kotlin.math.abs(tx.amount).toString()) }
    var note by remember { mutableStateOf(tx.note) }
    var isReceive by remember { mutableStateOf(tx.amount >= 0) }
    var error by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = dismiss, 
        title = { Text("Edit entry", fontWeight = FontWeight.Bold) }, 
        text = { 
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = isReceive, onClick = { isReceive = true }, label = { Text("Receive") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = !isReceive, onClick = { isReceive = false }, label = { Text("Pay") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit); error = false }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text("Note") }, shape = RoundedCornerShape(12.dp))
                if (error) Text("Enter a valid amount.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) 
            } 
        }, 
        confirmButton = { TextButton({ val value = amount.toLongOrNull(); if (value == null || value <= 0) error = true else save(if (isReceive) value else -value, note) }) { Text("Save", fontWeight = FontWeight.Bold) } }, 
        dismissButton = { TextButton(dismiss) { Text("Cancel") } }
    ) 
}
@Composable private fun AppHeader(title: String, subtitle: String) = Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
    color = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    shadowElevation = 4.dp
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(subtitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
@Composable private fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) = Card(
    modifier = modifier, 
    shape = RoundedCornerShape(20.dp), 
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), 
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
) { Column(content = content) }
@Composable private fun SummaryCard(label: String, value: String, modifier: Modifier, color: Color? = null) = Card(modifier.height(110.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color?.copy(alpha = 0.05f) ?: MaterialTheme.colorScheme.secondaryContainer.copy(alpha=0.4f)), border = BorderStroke(1.dp, color?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))) { Column(Modifier.padding(12.dp)) { Text(label, style = MaterialTheme.typography.labelMedium, color = color ?: MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp)); Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color ?: MaterialTheme.colorScheme.onSurface, maxLines = 1) } }
@Composable private fun TransactionCard(tx: KhataTransaction, name: String?, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
    var showBill by remember { mutableStateOf(false) }
    if (showBill && tx.imageUri != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showBill = false }) {
            OutlinedCard(shape = RoundedCornerShape(16.dp)) {
                coil.compose.AsyncImage(model = java.io.File(tx.imageUri), contentDescription = "Bill", modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
            }
        }
    }
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.05f)), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.5f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text(text = (name?.firstOrNull()?.toString() ?: "T").uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(name ?: money(tx.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color=MaterialTheme.colorScheme.onSurface)
                if (name != null) Text(money(tx.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                if (tx.note.isNotBlank()) Text(tx.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(date(tx.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (tx.imageUri != null) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = { showBill = true }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                        Icon(Icons.Default.Image, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("View Bill", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Row(content = actions)
        }
    }
}

@Composable private fun Empty(text: String) = Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)); Spacer(Modifier.height(16.dp)); Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }

fun copyImageToInternalStorage(context: android.content.Context, uri: android.net.Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = java.io.File(context.filesDir, "bill_${System.currentTimeMillis()}.jpg")
        val outputStream = java.io.FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        file.absolutePath
    } catch (e: Exception) { null }
}
