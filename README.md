# MAHMOUD VPN PRO
Kotlin + Jetpack Compose + Material 3. افتح المجلد في Android Studio ثم Build > Build APK.
- `SERVERS_URL` / `SUPABASE_ANON_KEY` في app/build.gradle.kts (الـ anon key عام وآمن مع RLS).
- JSON السيرفرات: [{"id","country","flag","name","protocol","host","port"}]
- JSON الإعدادات: [{"id","name","flag","carrier","plan","type","recommended"}]
- الأنفاق: نفّذ TunnelEngine لكل بروتوكول وسجّله في Engines.register(...).
- Admin Panel: ابنِه كموقع ويب منفصل (Supabase Auth + جداول servers/configs/subscriptions/notifications/app_versions)
  مع Row Level Security بحيث يكتب المدير فقط. لا تضع لوحة الإدارة داخل APK.
