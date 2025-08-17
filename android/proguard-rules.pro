# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Capacitor
-keep class com.getcapacitor.** { *; }
-keep class com.leeskies.capacitorbixolonprinter.** { *; }

# Bixolon SDK (add specific rules as needed)
# -keep class com.bixolon.** { *; }