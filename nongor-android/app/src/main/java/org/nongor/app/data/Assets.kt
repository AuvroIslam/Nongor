package org.nongor.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

/**
 * Everything Nongor knows ships inside the APK. There is no first-run download and no
 * "content pack" to fetch — if the app installed, it is complete.
 */
object Assets {

    private val gson = Gson()

    fun <T> read(context: Context, path: String, type: java.lang.reflect.Type): T =
        context.assets.open(path).use { stream ->
            InputStreamReader(stream, Charsets.UTF_8).use { reader ->
                gson.fromJson<T>(reader, type)
            }
        }

    inline fun <reified T> readObject(context: Context, path: String): T =
        read(context, path, object : TypeToken<T>() {}.type)
}
