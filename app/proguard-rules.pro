# kotlinx.serialization uses class names at runtime for polymorphic deserialization
-keepclassmembers class com.example.dosagecalc.data.model.** {
    *;
}

# Hilt generates all wiring at compile time via KSP — no runtime reflection needed.

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
