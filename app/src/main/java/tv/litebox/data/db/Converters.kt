package tv.litebox.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun fromList(list: List<String>): String = list.joinToString("|")
    @TypeConverter fun toList(s: String): List<String> = if (s.isEmpty()) emptyList() else s.split("|")
    @TypeConverter fun fromMap(map: Map<String, String>): String =
        map.entries.joinToString(";") { "${it.key}=${it.value}" }
    @TypeConverter fun toMap(s: String): Map<String, String> =
        if (s.isEmpty()) emptyMap()
        else s.split(";").associate { it.substringBefore("=") to it.substringAfter("=") }
}
