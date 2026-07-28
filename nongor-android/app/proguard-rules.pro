# Gson reflects over the model classes deserialised from bundled assets.
-keep class org.nongor.app.data.** { *; }
-keep class org.nongor.app.core.** { *; }
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

# BouncyCastle provides the Ed25519 signer used by the mesh.
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-dontwarn javax.naming.**
