package com.brbrs.bookmarks.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("auth")

data class AuthCredentials(
    val serverUrl: String,
    val username: String,
    val appPassword: String,
)

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
) {
    companion object {
        private val KEY_SERVER    = stringPreferencesKey("server_url")
        private val KEY_USER      = stringPreferencesKey("username")
        private val KEY_PASSWORD  = stringPreferencesKey("app_password")
        private val KEY_BIOMETRIC = booleanPreferencesKey("biometric_enabled")
    }

    val credentials: Flow<AuthCredentials?> = context.dataStore.data.map { prefs ->
        val server = prefs[KEY_SERVER]   ?: return@map null
        val user   = prefs[KEY_USER]     ?: return@map null
        val pass   = prefs[KEY_PASSWORD] ?: return@map null
        AuthCredentials(server, user, pass)
    }

    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BIOMETRIC] ?: false
    }

    suspend fun saveCredentials(server: String, username: String, appPassword: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVER]   = server.trimEnd('/')
            prefs[KEY_USER]     = username
            prefs[KEY_PASSWORD] = appPassword
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BIOMETRIC] = enabled }
    }

    suspend fun clearCredentials() {
        context.dataStore.edit { it.clear() }
    }

    // ── Login Flow v2 ─────────────────────────────────────────────────────────

    data class LoginFlowInit(
        val loginUrl: String,
        val pollEndpoint: String,   // exact URL as returned by Nextcloud — never rewrite this
        val token: String,
    )

    private fun normaliseUrl(input: String): String {
        var url = input.trim().trimEnd('/')
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        return url
    }

    /** Step 1 — POST to /index.php/login/v2, get back loginUrl + poll endpoint + token. */
    suspend fun initiateLoginFlow(serverUrl: String): Result<LoginFlowInit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val base = normaliseUrl(serverUrl)
                val url  = "$base/index.php/login/v2"
                val body = "".toRequestBody("application/x-www-form-urlencoded".toMediaType())
                val req  = Request.Builder().url(url).post(body).build()

                val resp = try {
                    okHttpClient.newCall(req).execute()
                } catch (e: Exception) {
                    throw Exception(friendlyNetworkError(e))
                }

                if (!resp.isSuccessful) {
                    throw Exception("Server returned HTTP ${resp.code} — check the URL and try again")
                }

                val bodyStr = resp.body?.string()
                    ?: throw Exception("Empty response from server")

                val json = try {
                    JSONObject(bodyStr)
                } catch (e: Exception) {
                    throw Exception("Unexpected response — is this a Nextcloud instance?")
                }

                // Log what we got so we can debug poll endpoint issues
                val pollObj      = json.getJSONObject("poll")
                val pollEndpoint = pollObj.getString("endpoint")
                val token        = pollObj.getString("token")
                val loginUrl     = json.getString("login")

                android.util.Log.d("MerkAuth", "Login flow initiated")
                android.util.Log.d("MerkAuth", "  loginUrl     = $loginUrl")
                android.util.Log.d("MerkAuth", "  pollEndpoint = $pollEndpoint")

                LoginFlowInit(
                    loginUrl     = loginUrl,
                    pollEndpoint = pollEndpoint,   // use exactly as-is from Nextcloud
                    token        = token,
                )
            }
        }

    /**
     * Step 2 — poll the endpoint Nextcloud gave us until the user approves in the browser.
     * Uses coroutine delay (not Thread.sleep) so it yields properly and survives
     * the app coming back from background.
     */
    suspend fun pollLoginFlow(pollEndpoint: String, token: String): Result<AuthCredentials> =
        withContext(Dispatchers.IO) {
            runCatching {
                // Body is rebuilt each iteration because RequestBody is single-use
                repeat(120) { attempt ->
                    android.util.Log.d("MerkAuth", "Poll attempt $attempt → $pollEndpoint")
                    val body = "token=$token".toRequestBody("application/x-www-form-urlencoded".toMediaType())
                    val req  = Request.Builder().url(pollEndpoint).post(body).build()

                    val resp = try {
                        okHttpClient.newCall(req).execute()
                    } catch (e: Exception) {
                        android.util.Log.w("MerkAuth", "Poll network error: ${e.message}")
                        delay(3_000)
                        return@repeat   // retry on network error
                    }

                    android.util.Log.d("MerkAuth", "Poll response: HTTP ${resp.code}")

                    when (resp.code) {
                        200 -> {
                            val json   = JSONObject(resp.body!!.string())
                            val server = json.getString("server").trimEnd('/')
                            val user   = json.getString("loginName")
                            val pass   = json.getString("appPassword")
                            android.util.Log.d("MerkAuth", "Poll success! server=$server user=$user")
                            saveCredentials(server, user, pass)
                            return@runCatching AuthCredentials(server, user, pass)
                        }
                        404 -> {
                            // Expected while waiting for user to approve — just keep polling
                            delay(2_000)
                        }
                        else -> {
                            android.util.Log.w("MerkAuth", "Unexpected poll status: ${resp.code}")
                            delay(2_000)
                        }
                    }
                }
                throw Exception("Login timed out — please try again")
            }
        }

    private fun friendlyNetworkError(e: Exception): String {
        val msg = e.message ?: e.javaClass.simpleName
        return when {
            msg.contains("Unable to resolve host", ignoreCase = true) ->
                "Cannot reach server — check the URL and your internet connection"
            msg.contains("CERTIFICATE", ignoreCase = true) ||
            msg.contains("SSL", ignoreCase = true) ||
            msg.contains("handshake", ignoreCase = true) ->
                "SSL certificate error"
            msg.contains("timeout", ignoreCase = true) ||
            msg.contains("timed out", ignoreCase = true) ->
                "Connection timed out"
            msg.contains("Connection refused", ignoreCase = true) ->
                "Connection refused — check the server address"
            else -> "Connection failed: $msg"
        }
    }
}
