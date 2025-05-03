package com.example.aistudy.data

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Utility class to manage the user's streak data
 */
class StreakManager(private val context: Context) {
    
    private val PREFS_NAME = "streak_prefs"
    private val LAST_OPEN_DATE_KEY = "last_open_date"
    private val CURRENT_STREAK_KEY = "current_streak"
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Checks if the app was opened on a consecutive day and updates the streak accordingly
     * @return the updated streak count
     */
    fun checkAndUpdateStreak(): Int {
        val currentTimeMillis = System.currentTimeMillis()
        val lastOpenTimeMillis = prefs.getLong(LAST_OPEN_DATE_KEY, 0)
        var currentStreak = prefs.getInt(CURRENT_STREAK_KEY, 0)
        
        // If this is the first time opening the app
        if (lastOpenTimeMillis == 0L) {
            // First time opening the app, set streak to 1
            currentStreak = 1
            saveStreakData(currentTimeMillis, currentStreak)
            return currentStreak
        }
        
        // Check if last open was today (do nothing to avoid double counting)
        if (isSameDay(lastOpenTimeMillis, currentTimeMillis)) {
            return currentStreak
        }
        
        // Check if last open was yesterday (increment streak)
        if (isConsecutiveDay(lastOpenTimeMillis, currentTimeMillis)) {
            currentStreak++
            saveStreakData(currentTimeMillis, currentStreak)
            return currentStreak
        }
        
        // Last open was not yesterday, reset streak to 1
        currentStreak = 1
        saveStreakData(currentTimeMillis, currentStreak)
        return currentStreak
    }
    
    /**
     * Saves streak data to SharedPreferences
     */
    private fun saveStreakData(timestamp: Long, streak: Int) {
        prefs.edit()
            .putLong(LAST_OPEN_DATE_KEY, timestamp)
            .putInt(CURRENT_STREAK_KEY, streak)
            .apply()
    }
    
    /**
     * Checks if two timestamps are from the same day
     */
    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Checks if two timestamps are from consecutive days
     */
    private fun isConsecutiveDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        
        // Clear time portion to compare dates only
        cal1.set(Calendar.HOUR_OF_DAY, 0)
        cal1.set(Calendar.MINUTE, 0)
        cal1.set(Calendar.SECOND, 0)
        cal1.set(Calendar.MILLISECOND, 0)
        
        cal2.set(Calendar.HOUR_OF_DAY, 0)
        cal2.set(Calendar.MINUTE, 0)
        cal2.set(Calendar.SECOND, 0)
        cal2.set(Calendar.MILLISECOND, 0)
        
        // Add one day to cal1
        cal1.add(Calendar.DAY_OF_YEAR, 1)
        
        // Check if cal1 + 1 day equals cal2
        return cal1.timeInMillis == cal2.timeInMillis
    }
    
    /**
     * Gets the current streak value without updating it
     */
    fun getCurrentStreak(): Int {
        return prefs.getInt(CURRENT_STREAK_KEY, 0)
    }
} 