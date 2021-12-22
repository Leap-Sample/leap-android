package `is`.leap.android.sample.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class LeapSampleSharedPref private constructor(mContext: Context) {
    private val sharedPreferences: SharedPreferences
    private fun save(key: String, `object`: JSONObject) {
        save(key, `object`.toString())
    }

    private fun save(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }

    private fun save(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun save(key: String?, value: Long) {
        editor.putLong(key, value).apply()
    }

    fun save(key: String?, value: Float) {
        editor.putFloat(key, value).apply()
    }

    fun save(key: String?, values: Set<String?>?) {
        editor.putStringSet(key, values).apply()
    }

    fun save(key: String?, value: String?) {
        editor.putString(key, value).apply()
    }

    // Remove & Clear methods
    fun remove(key: String?) {
        editor.remove(key).apply()
    }

    fun clear() {
        editor.clear().apply()
    }

    private fun readString(key: String, defaultValue: String?): String? {
        return try {
            sharedPreferences.getString(key, defaultValue)
        } catch (e: Exception) {
            defaultValue
        }
    }

    private val editor: SharedPreferences.Editor
        private get() = sharedPreferences.edit()
    var apiKey: String?
        get() = readString(APP_API_KEY, null)
        set(apiKey) {
            save(APP_API_KEY, apiKey)
        }
    var webUrl: String?
        get() = readString(WEB_URL, null)
        set(webUrl) {
            save(WEB_URL, webUrl)
        }

    companion object {
        private const val LEAP_SAMPLE_CACHE = "leap_sample_shared_pref"
        private var INSTANCE: LeapSampleSharedPref? = null
        const val APP_API_KEY = "apiKey"
        const val WEB_URL = "webUrl"
        @Synchronized
        fun init(mContext: Context) {
            if (INSTANCE == null) INSTANCE = LeapSampleSharedPref(mContext)
        }

        @kotlin.jvm.JvmStatic
        @get:Synchronized
        val instance: LeapSampleSharedPref?
            get() {
                if (INSTANCE == null) throw RuntimeException("Make sure to call init at-least once.")
                return INSTANCE
            }
    }

    init {
        sharedPreferences = mContext.getSharedPreferences(LEAP_SAMPLE_CACHE, Context.MODE_PRIVATE)
    }
}