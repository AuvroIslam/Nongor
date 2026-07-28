# ---- Nongor release keep rules -------------------------------------------------
# Keep line numbers so a crash in the field maps back to source.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature,*Annotation*,EnclosingMethod,InnerClasses

# ---- Gson (reflection-based (de)serialization) --------------------------------
# Gson reads/writes these by field reflection; obfuscating field names silently
# corrupts persisted chat history and mesh envelopes, so keep the model types whole.
-keep class com.example.gemmachat.actions.AssistantAction { *; }
-keep class com.example.gemmachat.actions.ParsedAssistantResponse { *; }
-keep class com.example.gemmachat.data.ConversationEntity { *; }
-keep class com.example.gemmachat.data.MessageEntity { *; }
-keep class com.example.gemmachat.data.ChatStore { *; }
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
