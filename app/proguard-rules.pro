# Proguard rules for Paper
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
