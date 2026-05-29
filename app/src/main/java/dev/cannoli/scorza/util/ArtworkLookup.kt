package dev.cannoli.scorza.util

import dev.cannoli.scorza.config.CannoliPaths
import dev.cannoli.scorza.di.CannoliPathsProvider
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ArtworkLookup(private val pathsProvider: CannoliPathsProvider) {
    private val artDir: File get() = CannoliPaths(pathsProvider.root).artDir
    private val cache = ConcurrentHashMap<String, Map<String, File>>()

    fun find(platformTag: String, romFile: File): File? {
        val basename = artBasename(romFile)
        val map = cache.getOrPut(platformTag) { buildMap(platformTag) }
        val hit = map[basename]
        if (hit != null) return hit
        if (romFile.name.endsWith(".p8.png", ignoreCase = true)) return romFile
        return null
    }

    fun invalidate(platformTag: String) {
        cache.remove(platformTag)
    }

    fun invalidateAll() {
        cache.clear()
    }

    private fun artBasename(romFile: File): String =
        if (romFile.name.endsWith(".p8.png", ignoreCase = true)) {
            romFile.name.dropLast(".p8.png".length)
        } else {
            romFile.nameWithoutExtension
        }

    private fun buildMap(platformTag: String): Map<String, File> {
        val tagDir = File(artDir, platformTag)
        if (!tagDir.exists()) return emptyMap()
        val entries = tagDir.listFiles() ?: return emptyMap()
        val out = mutableMapOf<String, File>()
        for (file in entries) if (file.isFile) out[file.nameWithoutExtension] = file
        return out
    }
}
