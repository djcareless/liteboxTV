package tv.litebox.data.scanner

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import tv.litebox.LiteBoxApp
import tv.litebox.data.db.entity.MediaItemEntity

/**
 * Background service that orchestrates media-source scanning.
 *
 * Start with an intent; use [ACTION_SCAN_ALL] or [ACTION_SCAN_SOURCE].
 *
 *   val intent = Intent(context, MediaScannerService::class.java)
 *   intent.action = MediaScannerService.ACTION_SCAN_ALL
 *   context.startService(intent)
 */
class MediaScannerService : Service() {

    private val mediaScanner = MediaScanner()
    private val db get() = LiteBoxApp.instance.database

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val scanJobs = mutableMapOf<String, Job>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SCAN_ALL -> scanAll()
            ACTION_SCAN_SOURCE -> {
                val sourceId = intent.getStringExtra(EXTRA_SOURCE_ID)
                if (sourceId != null) scanSource(sourceId)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    // -------------------------------------------------------------------------
    // Public interface
    // -------------------------------------------------------------------------

    /** Scan every source that has [scanEnabled] = true. */
    fun scanAll() {
        serviceScope.launch {
            try {
                val sources = db.mediaSourceDao().getAllScannable()
                Log.d(TAG, "scanAll: found ${sources.size} scannable source(s)")
                sources.forEach { entity ->
                    launchScan(entity.id, entity.toDomain())
                }
            } catch (e: Exception) {
                Log.e(TAG, "scanAll failed: ${e.message}", e)
            }
        }
    }

    /** Scan a single source by its [sourceId]. */
    fun scanSource(sourceId: String) {
        serviceScope.launch {
            try {
                val entity = db.mediaSourceDao().getById(sourceId)
                    ?: run {
                        Log.w(TAG, "scanSource: source $sourceId not found")
                        return@launch
                    }
                launchScan(sourceId, entity.toDomain())
            } catch (e: Exception) {
                Log.e(TAG, "scanSource($sourceId) failed: ${e.message}", e)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private fun launchScan(
        sourceId: String,
        source: tv.litebox.domain.model.MediaSource,
    ) {
        scanJobs[sourceId]?.cancel()
        scanJobs[sourceId] = serviceScope.launch {
            Log.i(TAG, "Scanning source: ${source.name} (${source.type})")
            mediaScanner.scan(source).collect { progress ->
                when (progress) {
                    is ScanProgress.Started -> Log.d(TAG, "[${source.name}] scan started")
                    is ScanProgress.Progress -> Log.v(TAG, "[${source.name}] ${progress.found} found — ${progress.current}")
                    is ScanProgress.Complete -> {
                        Log.i(TAG, "[${source.name}] complete: ${progress.items.size} item(s)")
                        val entities = progress.items.map { MediaItemEntity.from(it) }
                        db.mediaItemDao().upsertAll(entities)
                    }
                    is ScanProgress.Error -> Log.e(TAG, "[${source.name}] error: ${progress.message}")
                }
            }
        }
    }

    companion object {
        private const val TAG = "MediaScannerService"
        const val ACTION_SCAN_ALL = "tv.litebox.action.SCAN_ALL"
        const val ACTION_SCAN_SOURCE = "tv.litebox.action.SCAN_SOURCE"
        const val EXTRA_SOURCE_ID = "source_id"
    }
}
