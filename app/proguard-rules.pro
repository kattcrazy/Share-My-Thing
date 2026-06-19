-keep class com.sharemyththing.data.** { *; }
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
