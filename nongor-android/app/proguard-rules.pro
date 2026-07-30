# ---- Nongor release keep rules ------------------------------------------------
# Keep line numbers so a crash in the field maps back to source.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature,*Annotation*,EnclosingMethod,InnerClasses

# ---- Gson (reflection-based (de)serialization) --------------------------------
# Gson reads/writes these by field reflection; obfuscating field names silently
# corrupts persisted chat history and mesh envelopes, so keep the model types whole.
# Gson maps JSON keys onto Kotlin field NAMES by reflection. R8 renames those fields, so any
# model parsed from a bundled asset must be kept verbatim or it silently deserialises to empty.
#
# This bit us in the worst possible way: these rules used to name com.example.gemmachat.*, the
# package this app was renamed away from. They therefore protected nothing, while every real
# model went unprotected — and because the failure is silent (Gson returns an object with all
# fields null/empty rather than throwing) it only showed up as "Browse all 0 phrases" in a
# release build. Debug builds are not minified, so it never appeared in development or in the
# JVM unit tests.
-keep class org.nongor.app.core.** { *; }
-keep class org.nongor.app.data.** { *; }
-keep class org.nongor.app.actions.** { *; }
# Keep any @SerializedName-annotated members generally.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# ---- On-device Gemma: LiteRT-LM + TFLite (native + reflection bindings) --------
# These load native handles and call across JNI; let R8 leave them untouched so a
# minified release can still load and run the model.
-keep class com.google.ai.edge.litertlm.** { *; }
-dontwarn com.google.ai.edge.litertlm.**
-keep class org.tensorflow.** { *; }
-dontwarn org.tensorflow.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ---- BouncyCastle (Ed25519 mesh signing) --------------------------------------
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# ---- Kotlin ------------------------------------------------------------------
-keepclassmembers class **$WhenMappings { <fields>; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
