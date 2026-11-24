package ca.bcit.binged.data.local

import androidx.room.TypeConverter
import ca.bcit.binged.data.model.MediaType

// Tells Room how to store a MediaType in the database (as text: "TV" / "MOVIE").
class Converters {

    @TypeConverter
    fun fromMediaType(type: MediaType): String = type.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = MediaType.valueOf(value)
}