package com.v2ray.ang.ui.main

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val navy = Color(0xFF101B2C)
private val panel = Color(0xFF1D2C43)
private val green = Color(0xFF21A66C)
private val muted = Color(0xFFA9BBD2)

@Composable
fun MyVpnHomeScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
) {
    var showServers by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var configText by remember { mutableStateOf("") }
    BackHandler(showServers) { showServers = false }
    if (showServers) {
        MainScreen(mainViewModel, onAction, onNavigate)
        return
    }
    val state by mainViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("افزودن کانفیگ MyVPN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("لینک کانفیگ vmess، vless، trojan یا ss را وارد کنید. برای لینک اشتراک، گزینه مدیریت اشتراک را انتخاب کنید.")
                    OutlinedTextField(
                        value = configText,
                        onValueChange = { configText = it },
                        label = { Text("لینک کانفیگ") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    TextButton(onClick = {
                        showImportDialog = false
                        onNavigate(MainDestination.Subscriptions)
                    }) { Text("افزودن لینک اشتراک") }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = configText.isNotBlank(),
                    onClick = {
                        if (configText.trim().startsWith("https://", ignoreCase = true) ||
    configText.trim().startsWith("http://", ignoreCase = true)) {
    if (MyVpnSubscriptionHelper.addSubscription(context, configText)) {
        onAction(MainAction.UpdateSubscriptions)
    }
} else {
    onAction(MainAction.ImportBatchConfig(configText.trim()))
}
                        configText = ""
                        showImportDialog = false
                    }
                ) { Text("افزودن") }
            },
            dismissButton = { TextButton(onClick = { showImportDialog = false }) { Text("انصراف") } }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().background(navy)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("MyVPN", color = Color.White, style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold)
        Text("اتصال امن و مدیریت اشتراک", color = muted)
        if (!state.isRunning && state.selectedGuid == null) {
            Text("برای اتصال ابتدا یک کانفیگ وارد کنید و از فهرست سرورها آن را انتخاب کنید.", color = Color(0xFFFFD28A))
        }
        Column(
            modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(26.dp)).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(if (state.isRunning) "● متصل — VPN فعال است" else "● قطع — VPN فعال نیست",
                color = if (state.isRunning) green else Color(0xFFFF7777))
            Button(onClick = { onAction(MainAction.ToggleService) },
                enabled = state.isRunning || state.selectedGuid != null,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (state.isRunning) Color(0xFFB83C4D) else green)) {
                Text(if (state.isRunning) "قطع اتصال" else "اتصال VPN")
            }
            Text("سرور انتخابی: ${if (state.selectedGuid == null) "هیچ‌کدام" else "انتخاب شده"}", color = Color.White)
            OutlinedButton(onClick = { showServers = true }, modifier = Modifier.fillMaxWidth()) {
                Text("مشاهده و انتخاب سرورها")
            }
        }
        Column(modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(22.dp)).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("وضعیت و آزمایش اتصال", color = Color.White, fontWeight = FontWeight.Bold)
            Text("تأخیر TCP: — ms    |    دانلود: —    |    آپلود: —", color = muted)
            Text(mainViewModel.formatStatus(state.status), color = muted)
            Button(onClick = { onAction(MainAction.TestCurrentServer) }, modifier = Modifier.fillMaxWidth()) {
                Text("آزمایش تأخیر سرور انتخابی")
            }
        }
        Column(modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(22.dp)).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("اطلاعات اشتراک و مصرف", color = Color.White, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("حجم کل", "مصرف‌شده", "باقی‌مانده").forEach { label ->
                    Column(
                        modifier = Modifier.weight(1f)
                            .background(navy, RoundedCornerShape(12.dp)).padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(label, color = muted, style = MaterialTheme.typography.labelSmall)
                        Text("—", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Text("میزان مصرف: —", color = Color.White)
            LinearProgressIndicator(
                progress = { 0f }, modifier = Modifier.fillMaxWidth(),
                color = green, trackColor = Color(0xFF394B60)
            )
            Text("اعتبار اشتراک: اطلاعات در دسترس نیست", color = muted)
            Text("نمایش حجم و انقضا نیازمند اطلاعات ترافیک از سرویس اشتراک است.", color = muted,
                style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { onAction(MainAction.UpdateSubscriptions) }, modifier = Modifier.fillMaxWidth()) {
                Text("به‌روزرسانی اشتراک")
            }
            Button(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Mric67_bot")))
            }, modifier = Modifier.fillMaxWidth()) { Text("خرید یا تمدید از ربات تلگرام") }
        }
        Button(onClick = { showImportDialog = true }, modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = green)) {
            Text("+ افزودن کانفیگ / لینک اشتراک")
        }
        OutlinedButton(onClick = { onAction(MainAction.ImportClipboard) }, modifier = Modifier.fillMaxWidth()) {
            Text("وارد کردن از کلیپ‌بورد")
        }
        OutlinedButton(onClick = { onNavigate(MainDestination.Subscriptions) }, modifier = Modifier.fillMaxWidth()) {
            Text("افزودن و مدیریت لینک اشتراک")
        }
    }
}
