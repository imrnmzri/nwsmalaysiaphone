package my.gov.met.nwsmalaysia.data.repository

import android.graphics.BitmapFactory
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import my.gov.met.nwsmalaysia.data.db.CachedSignifikanEntity
import my.gov.met.nwsmalaysia.data.db.SignifikanDao
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val TAG = "SignifikanRepo"
private const val CACHE_TTL_MS = 3 * 60 * 60 * 1000L  // 3 hours
private const val SIGNIFIKAN_URL =
    "https://www.met.gov.my/data/pocgn/ramalancuacasignifikan.jpg"

@Singleton
class SignifikanRepository @Inject constructor(
    private val signifikanDao: SignifikanDao,
    private val okHttpClient: OkHttpClient
) {

    suspend fun getSignifikanText(): String? {
        val cached = signifikanDao.get()
        val now = System.currentTimeMillis()
        if (cached != null && now - cached.fetchedAt < CACHE_TTL_MS && cached.extractedText.isNotBlank()) {
            Log.d(TAG, "Returning cached Signifikan text (${cached.extractedText.length} chars)")
            return cached.extractedText
        }
        return fetchAndExtract()
    }

    private suspend fun fetchAndExtract(): String? {
        return try {
            Log.d(TAG, "Downloading Signifikan image…")
            val bytes = withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(SIGNIFIKAN_URL)
                    .header("User-Agent", "NWSMalaysia-Android/1.0")
                    .build()
                okHttpClient.newCall(request).execute().use { response ->
                    Log.d(TAG, "HTTP ${response.code} content-length=${response.body?.contentLength()}")
                    if (!response.isSuccessful) return@withContext null
                    response.body?.bytes()
                }
            } ?: run {
                Log.e(TAG, "Empty response body")
                return null
            }

            Log.d(TAG, "Downloaded ${bytes.size} bytes, decoding bitmap…")
            val bitmap = withContext(Dispatchers.Default) {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } ?: run {
                Log.e(TAG, "BitmapFactory failed to decode image")
                return null
            }
            Log.d(TAG, "Bitmap ${bitmap.width}x${bitmap.height}, running ML Kit OCR…")

            val text = runMlKit(bitmap)
            Log.d(TAG, "ML Kit result: ${text?.length ?: 0} chars — snippet: ${text?.take(120)}")

            if (!text.isNullOrBlank()) {
                val parsed = parseText(text)
                signifikanDao.insert(
                    CachedSignifikanEntity(extractedText = parsed, fetchedAt = System.currentTimeMillis())
                )
                parsed
            } else {
                Log.w(TAG, "ML Kit returned blank text")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAndExtract failed: ${e.message}", e)
            signifikanDao.get()?.extractedText
        }
    }

    /**
     * Mirrors PHP parseAdvisoryOcr() exactly:
     *  1. Body = everything before "Dikeluarkan"
     *  2. Strip leading dashes/symbols
     *  3. Strip trailing "Orang awam..." boilerplate
     *  4. Collapse whitespace to single spaces
     *  5. Issued = "Dikeluarkan : …" line (regex match)
     */
    private fun parseText(raw: String): String {
        var body = ""
        val dikeluarkanIdx = Regex("Dikeluarkan", RegexOption.IGNORE_CASE).find(raw)?.range?.first ?: -1

        if (dikeluarkanIdx > 0) {
            body = raw.substring(0, dikeluarkanIdx).trim()
            body = body.trimStart('-', '*', ' ', '\n')
            // Strip trailing "Orang awam..." boilerplate (mirrors PHP regex)
            body = Regex("Orang awam.+$", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                .replace(body, "").trim()
            // Collapse all whitespace to single spaces
            body = Regex("\\s+").replace(body, " ").trim()
        } else {
            body = raw.trim()
        }

        // Extract "Dikeluarkan : …" line
        val issued = Regex("Dikeluarkan\\s*:\\s*.+?(?:\\n|\$)", RegexOption.IGNORE_CASE)
            .find(raw)?.value?.trim() ?: ""

        return if (issued.isNotBlank()) "$body\n\n$issued" else body
    }

    private suspend fun runMlKit(bitmap: android.graphics.Bitmap): String? {
        return suspendCancellableCoroutine { cont ->
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    cont.resume(result.text.ifBlank { null })
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "ML Kit failed: ${e.message}")
                    cont.resume(null)
                }
        }
    }
}
