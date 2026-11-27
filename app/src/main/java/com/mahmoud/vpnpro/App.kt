@file:OptIn(ExperimentalMaterial3Api::class)
package com.mahmoud.vpnpro

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun act(f: () -> Unit) = f

@Composable
fun App(dark: Boolean, setDark: (Boolean) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val snack = remember { SnackbarHostState() }
    var screen by remember { mutableStateOf("home") }
    var servers by remember { mutableStateOf(emptyList<Server>()) }
    var configs by remember { mutableStateOf(Repository.sampleConfigs()) }
    var selected by remember { mutableStateOf<Server?>(null) }
    var showServers by remember { mutableStateOf(false) }
    val status by VpnState.status.collectAsState()
    val err by VpnState.error.collectAsState()

    LaunchedEffect(Unit) { servers = Repository.servers(ctx) }
    LaunchedEffect(err) { err?.let { snack.showSnackbar(it) } }

    fun target() = selected ?: servers.firstOrNull()
    val vpnPerm = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) target()?.let { s -> MahmoudVpnService.start(ctx, s) }
    }
    fun connect() {
        if (System.currentTimeMillis() > Repository.expiry(ctx)) { scope.launch { snack.showSnackbar("انتهى الاشتراك") }; return }
        val i = VpnService.prepare(ctx)
        if (i != null) vpnPerm.launch(i) else target()?.let { MahmoudVpnService.start(ctx, it) }
    }
    fun importText(t: String) = runCatching { Parser.configs(t) }
        .onSuccess { configs = it; scope.launch { snack.showSnackbar("تم استيراد ${it.size} إعداد") } }
        .onFailure { scope.launch { snack.showSnackbar("ملف غير صالح") } }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importText(ctx.contentResolver.openInputStream(it)!!.bufferedReader().readText()) }
    }
    fun pasteClip() {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        importText(cm.primaryClip?.getItemAt(0)?.text?.toString().orEmpty())
    }
    fun soon(n: String) { scope.launch { drawer.close(); snack.showSnackbar("$n — قريباً") } }

    ModalNavigationDrawer(drawerState = drawer, drawerContent = {
        ModalDrawerSheet {
            Text("MAHMOUD VPN PRO", Modifier.padding(20.dp), color = Cyan, fontWeight = FontWeight.Bold)
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Enable Dark Theme", Modifier.weight(1f)); Switch(dark, setDark)
            }
            listOf<Pair<String, () -> Unit>>(
                "Network Config" to act { screen = "configs"; scope.launch { drawer.close() } },
                "IP Hunt" to act { soon("IP Hunt") },
                "Tethering" to act { ctx.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) },
                "Online Update" to act { scope.launch { servers = Repository.servers(ctx); drawer.close(); snack.showSnackbar("تم التحديث") } },
                "Settings" to act { soon("Settings") },
                "Offline Update" to act { picker.launch(arrayOf("*/*")) },
                "Update from Clipboard" to act { pasteClip() },
                "Clear Data" to act { ctx.getSharedPreferences("cache", 0).edit().clear().apply(); soon("تم المسح") },
                "Earn More Time" to act { soon("Earn More Time") },
                "Beginner Tutorial" to act { soon("Tutorial") },
                "Updates Notes" to act { soon("Notes") },
            ).forEach { (t, a) -> NavigationDrawerItem(label = { Text(t) }, selected = false, onClick = a) }
        }
    }) {
        Box(Modifier.fillMaxSize()) {
            TechBackground()
            Scaffold(containerColor = Color.Transparent, snackbarHost = { SnackbarHost(snack) }, topBar = {
                TopAppBar(title = {}, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = { IconButton({ scope.launch { drawer.open() } }) { Icon(Icons.Default.Menu, null, tint = Cyan) } },
                    actions = { if (screen != "home") IconButton({ screen = "home" }) { Icon(Icons.Default.Home, null, tint = Cyan) } })
            }) { pad ->
                Box(Modifier.padding(pad)) {
                    if (screen == "home") Home(status, selected, { showServers = true },
                        { if (status == Status.Disconnected) connect() },
                        { MahmoudVpnService.stop(ctx) }, ctx, { scope.launch { snack.showSnackbar("أضف وقتاً عبر الاشتراك من الـ backend") } })
                    else ConfigsScreen(configs, { picker.launch(arrayOf("*/*")) }, { pasteClip() })
                }
            }
        }
    }
    if (showServers) ModalBottomSheet({ showServers = false }) {
        ServerList(servers, { selected = it; showServers = false })
    }
}

@Composable
fun Home(status: Status, sel: Server?, openServers: () -> Unit, onStart: () -> Unit, onStop: () -> Unit,
         ctx: Context, addTime: () -> Unit) {
    val since by VpnState.since.collectAsState()
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    fun fmt(ms: Long) = ms.coerceAtLeast(0).div(1000).let { s -> "%02d:%02d:%02d".format(s / 3600, s % 3600 / 60, s % 60) }
    val connected = status == Status.Connected
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.SmartToy, null, tint = Cyan, modifier = Modifier.size(84.dp))
        Text("MAHMOUD VPN PRO", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Cyan)
        Spacer(Modifier.height(20.dp))
        Text(status.name.uppercase(), color = if (connected) Color(0xFF00E676) else Color.Gray, fontSize = 18.sp)
        Text(if (connected) fmt(now - since) else "00:00:00", fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedCard(onClick = openServers, Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(sel?.flag ?: "🌐", fontSize = 28.sp); Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text(sel?.name ?: "Auto Select Server"); Text(sel?.protocol ?: "Best available", color = Color.Gray, fontSize = 12.sp) }
                Icon(Icons.Default.ChevronRight, null)
            }
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            CircleBtn("START", Cyan, Color.Black, onStart)
            CircleBtn("STOP", Color(0xFFFF5252), Color.White, onStop)
        }
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Time left: ${fmt(Repository.expiry(ctx) - now)}", Modifier.padding(end = 12.dp))
            FilledTonalButton(addTime) { Text("ADD TIME") }
        }
    }
}

@Composable fun CircleBtn(label: String, bg: Color, fg: Color, onClick: () -> Unit) =
    Box(Modifier.size(104.dp).clip(CircleShape).background(bg).clickable(onClick = onClick), Alignment.Center) {
        Text(label, color = fg, fontWeight = FontWeight.Bold)
    }

@Composable
fun ServerList(servers: List<Server>, pick: (Server?) -> Unit) {
    LazyColumn(Modifier.padding(horizontal = 16.dp)) {
        item { ListItem({ Text("Auto Select Server") }, Modifier.clickable { pick(null) }, leadingContent = { Text("⚡") }) }
        servers.groupBy { it.country }.forEach { (c, list) ->
            item { Text("${list[0].flag} $c", Modifier.padding(top = 12.dp), color = Cyan, fontWeight = FontWeight.Bold) }
            items(list) { s -> ListItem({ Text(s.name) }, Modifier.clickable { pick(s) }, trailingContent = { AssistChip({}, { Text(s.protocol) }) }) }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun ConfigsScreen(all: List<NetConfig>, importFile: () -> Unit, importClip: () -> Unit) {
    var q by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    val list = all.filter { it.name.contains(q, true) && (tab == 1 || it.recommended) }
    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), placeholder = { Text("Search network") }, singleLine = true)
        TabRow(tab, containerColor = Color.Transparent) {
            Tab(tab == 0, { tab = 0 }, text = { Text("Recommended") }); Tab(tab == 1, { tab = 1 }, text = { Text("All Networks") })
        }
        Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(importFile) { Text("Import file") }; OutlinedButton(importClip) { Text("Clipboard") }
        }
        LazyColumn { items(list) { c ->
            ListItem({ Text(c.name) }, supportingContent = { Text("${c.carrier} • ${c.plan} • ${c.type}") }, leadingContent = { Text(c.flag, fontSize = 26.sp) })
        } }
    }
}

@Composable
fun TechBackground() {
    val t = rememberInfiniteTransition(label = "bg")
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse), label = "p")
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF05080D), Color(0xFF071A22))))
        for (i in 0..12) { val x = size.width / 12 * i; drawLine(Cyan.copy(alpha = 0.05f), Offset(x, 0f), Offset(x, size.height)) }
        for (i in 0..24) { val y = size.height / 24 * i; drawLine(Cyan.copy(alpha = 0.05f), Offset(0f, y), Offset(size.width, y)) }
        drawCircle(Brush.radialGradient(listOf(Cyan.copy(alpha = 0.25f), Color.Transparent),
            Offset(size.width * p, size.height * 0.3f), size.width * 0.6f), size.width * 0.6f, Offset(size.width * p, size.height * 0.3f))
    }
}
