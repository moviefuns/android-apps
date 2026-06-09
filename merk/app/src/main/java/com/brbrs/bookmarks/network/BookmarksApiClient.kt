package com.brbrs.bookmarks.network

import com.brbrs.bookmarks.auth.AuthCredentials
import com.brbrs.bookmarks.data.local.BookmarkEntity
import com.brbrs.bookmarks.data.local.FolderEntity
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarksApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
) {
    private fun baseUrl(creds: AuthCredentials) =
        "${creds.serverUrl}/index.php/apps/bookmarks/public/rest/v2"

    private fun authHeader(creds: AuthCredentials) =
        Credentials.basic(creds.username, creds.appPassword)

    private fun get(url: String, creds: AuthCredentials): JSONObject {
        val req  = Request.Builder().url(url).header("Authorization", authHeader(creds)).get().build()
        val resp = okHttpClient.newCall(req).execute()
        check(resp.isSuccessful) { "GET $url → HTTP ${resp.code}" }
        return JSONObject(resp.body!!.string())
    }

    private fun post(url: String, body: JSONObject, creds: AuthCredentials): JSONObject {
        val reqBody = body.toString().toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url(url).header("Authorization", authHeader(creds))
            .post(reqBody).build()
        val resp = okHttpClient.newCall(req).execute()
        check(resp.isSuccessful) { "POST $url → HTTP ${resp.code}" }
        return JSONObject(resp.body!!.string())
    }

    private fun put(url: String, body: JSONObject, creds: AuthCredentials): JSONObject {
        val reqBody = body.toString().toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url(url).header("Authorization", authHeader(creds))
            .put(reqBody).build()
        val resp = okHttpClient.newCall(req).execute()
        check(resp.isSuccessful) { "PUT $url → HTTP ${resp.code}" }
        return JSONObject(resp.body!!.string())
    }

    private fun delete(url: String, creds: AuthCredentials) {
        val req  = Request.Builder().url(url).header("Authorization", authHeader(creds)).delete().build()
        val resp = okHttpClient.newCall(req).execute()
        check(resp.isSuccessful) { "DELETE $url → HTTP ${resp.code}" }
    }

    // ── Bookmarks ────────────────────────────────────────────────────────────

    suspend fun fetchAllBookmarks(creds: AuthCredentials): List<BookmarkEntity> {
        val result = mutableListOf<BookmarkEntity>()
        var page = 0
        while (true) {
            val url  = "${baseUrl(creds)}/bookmark?limit=100&page=$page&sortby=lastmodified&order=desc"
            val json = get(url, creds)
            val arr  = json.getJSONArray("data")
            if (arr.length() == 0) break
            for (i in 0 until arr.length()) result += arr.getJSONObject(i).toEntity()
            page++
        }
        return result
    }

    suspend fun createBookmark(creds: AuthCredentials, entity: BookmarkEntity): BookmarkEntity {
        val body = JSONObject().apply {
            put("url",   entity.url)
            put("title", entity.title)
            put("description", entity.description)
            put("tags",  JSONArray(entity.tags.split(",").filter { it.isNotBlank() }))
            if (entity.folderId > 0) put("folders", JSONArray().put(entity.folderId))
        }
        val resp = post("${baseUrl(creds)}/bookmark", body, creds)
        return resp.getJSONObject("item").toEntity()
    }

    suspend fun updateBookmark(creds: AuthCredentials, entity: BookmarkEntity): BookmarkEntity {
        val body = JSONObject().apply {
            put("url",   entity.url)
            put("title", entity.title)
            put("description", entity.description)
            put("tags",  JSONArray(entity.tags.split(",").filter { it.isNotBlank() }))
        }
        val resp = put("${baseUrl(creds)}/bookmark/${entity.id}", body, creds)
        return resp.getJSONObject("item").toEntity()
    }

    suspend fun deleteBookmark(creds: AuthCredentials, id: Long) {
        delete("${baseUrl(creds)}/bookmark/$id", creds)
    }

    // ── Folders ──────────────────────────────────────────────────────────────

    suspend fun fetchFolders(creds: AuthCredentials): List<FolderEntity> {
        val json = get("${baseUrl(creds)}/folder", creds)
        return parseFolders(json.getJSONArray("data"))
    }

    private fun parseFolders(arr: JSONArray, parentId: Long = -1): List<FolderEntity> {
        val result = mutableListOf<FolderEntity>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            result += FolderEntity(
                id             = obj.getLong("id"),
                title          = obj.optString("title", ""),
                parentFolderId = parentId,
            )
            if (obj.has("children")) {
                result += parseFolders(obj.getJSONArray("children"), obj.getLong("id"))
            }
        }
        return result
    }

    // ── JSON mapping ─────────────────────────────────────────────────────────

    private fun JSONObject.toEntity(): BookmarkEntity {
        val tags = buildString {
            val arr = optJSONArray("tags") ?: JSONArray()
            for (i in 0 until arr.length()) {
                if (i > 0) append(",")
                append(arr.getString(i))
            }
        }
        val folderId   = optJSONArray("folders")?.optLong(0) ?: -1L
        val faviconUrl = optString("favicon", "")
        return BookmarkEntity(
            id          = getLong("id"),
            url         = optString("url", ""),
            title       = optString("title", optString("url", "")),
            description = optString("description", ""),
            tags        = tags,
            folderId    = folderId,
            folderName  = "",
            faviconUrl  = faviconUrl,
            addedAt     = optLong("added", System.currentTimeMillis() / 1000),
        )
    }
}
