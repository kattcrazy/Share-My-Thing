-keep class com.sharemyththing.data.** { *; }
-keep class com.sharemyththing.sync.** { *; }
-keep class com.sharemyththing.util.** { *; }
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract ** *;
}

-keep class com.google.zxing.** { *; }
