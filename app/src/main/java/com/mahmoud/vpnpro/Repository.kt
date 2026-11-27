package com.mahmoud.vpnpro

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/** يجلب السيرفرات من الـ backend. لا توجد أي بيانات سرية داخل التطبيق. */
object Repository {
    private val countries = listOf("Japan" to "🇯🇵", "Singapore" to "🇸🇬", "Germany" to "🇩🇪", "USA" to "🇺🇸",
        "UK" to "🇬🇧", "France" to "🇫🇷", "Netherlands" to "🇳🇱", "Canada" to "🇨🇦")
    private val protos = listOf("OpenVPN", "WebSocket", "SSH", "V2Ray")

    fun sampleServers() = countries.flatMapIndexed { ci, (c, f) ->
        (1..3).map { Server("$c-$it", c, f, "$c VIP $it", protos[(ci + it) % 4], "example.invalid", 443) }
    }
    fun sampleConfigs() = listOf(
        NetConfig("1", "Network A", "🇺🇸", "Carrier A", "Unlimited", "WebSocket", true),
        NetConfig("2", "Network B", "🇬🇧", "Carrier B", "Social", "SSH", true),
        NetConfig("3", "Network C", "🇫🇷", "Carrier C", "Basic", "V2Ray", false))

    suspend fun servers(ctx: Context): List<Server> = withContext(Dispatchers.IO) {
        if (BuildConfig.SERVERS_URL.isBlank()) return@withContext sampleServers()
        try {
            val c = URL(BuildConfig.SERVERS_URL).openConnection() as HttpURLConnection
            c.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            c.connectTimeout = 8000; c.readTimeout = 8000
            val body = c.inputStream.bufferedReader().readText()
            Parser.servers(body).also { ctx.getSharedPreferences("cache", 0).edit().putString("servers", body).apply() }
        } catch (e: Exception) {
            ctx.getSharedPreferences("cache", 0).getString("servers", null)
                ?.let { runCatching { Parser.servers(it) }.getOrNull() } ?: sampleServers()
        }
    }

    /** مدة الاشتراك: يجب أن يُتحقق منها من الـ backend (Supabase/Firebase) لا محلياً. */
    fun expiry(ctx: Context): Long {
        val p = ctx.getSharedPreferences("sub", 0)
        if (!p.contains("expiry")) p.edit().putLong("expiry", System.currentTimeMillis() + 3_600_000).apply()
        return p.getLong("expiry", 0)
    }
}
