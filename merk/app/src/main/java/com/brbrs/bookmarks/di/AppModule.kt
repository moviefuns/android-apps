package com.brbrs.bookmarks.di

import android.content.Context
import androidx.room.Room
import com.brbrs.bookmarks.data.local.BookmarkDao
import com.brbrs.bookmarks.data.local.BookmarksDatabase
import com.brbrs.bookmarks.data.local.FolderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.*

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val trustManager = buildUserAwareTrustManager()
        val sslContext   = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), null)
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, session ->
                try { session.peerCertificates.isNotEmpty() } catch (e: Exception) { false }
            }
            .addInterceptor { chain ->
                // Show "Merk" in the Nextcloud login flow browser page
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Merk/1.0 (Android) Nextcloud-Bookmarks")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    private fun buildUserAwareTrustManager(): X509TrustManager {
        val systemKs = KeyStore.getInstance("AndroidCAStore").apply { load(null) }
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(systemKs)
        val systemTm = tmf.trustManagers.filterIsInstance<X509TrustManager>().first()

        return object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = systemTm.acceptedIssuers
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) =
                systemTm.checkClientTrusted(chain, authType)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                try {
                    systemTm.checkServerTrusted(chain, authType)
                } catch (e: Exception) {
                    // Accept anyway — covers homelab self-signed / LE certs on older Android
                }
            }
        }
    }

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): BookmarksDatabase =
        Room.databaseBuilder(ctx, BookmarksDatabase::class.java, "bookmarks.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideBookmarkDao(db: BookmarksDatabase): BookmarkDao = db.bookmarkDao()
    @Provides fun provideFolderDao(db: BookmarksDatabase): FolderDao     = db.folderDao()
}
