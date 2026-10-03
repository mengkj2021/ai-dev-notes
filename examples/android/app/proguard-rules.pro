# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.paging.**

# Kotlin metadata / coroutines (Room + DataStore)
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler

# FileProvider (export share)
-keep class androidx.core.content.FileProvider { *; }

# Compose / Navigation：依赖自带 consumer rules；保留反射用枚举名
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
