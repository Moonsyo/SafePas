package com.example.safepas.data

import androidx.room.TypeConverter

class CategoryConverter {
    @TypeConverter
    fun fromCategory(category: Category): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(value: String): Category {
        return Category.valueOf(value)
    }
}