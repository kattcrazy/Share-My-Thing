-keep class com.sharemyththing.data.** { *; }
-keep class com.sharemyththing.sync.** { *; }
-keep class com.sharemyththing.util.** { *; }
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract ** *;
}

-keep class com.google.zxing.** { *; }

-keepclassmembers class * extends androidx.wear.tiles.TileService {
    public <init>(...);
}

-keepclassmembers class * extends androidx.wear.watchface.complications.datasource.ComplicationDataSourceService {
    public <init>(...);
}
