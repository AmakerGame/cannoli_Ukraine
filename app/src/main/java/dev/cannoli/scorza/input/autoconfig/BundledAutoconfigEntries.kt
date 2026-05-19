package dev.cannoli.scorza.input.autoconfig

// Bundled cfgs are loaded synchronously at first injection. The earlier async-on-Dispatchers.IO
// design produced a race between controller enumeration and bundled-cfg loading: when a device
// enumerated before the IO load completed, MappingResolver.bestRetroArchEntry returned null and
// the resolver fell through to ANDROID_DEFAULT, persisting a wrong-layout mapping that then
// dominated future resolves. Synchronous load costs some startup latency but guarantees the cfg
// database is available whenever any code asks for it.

class BundledAutoconfigEntries private constructor(
    private val eager: List<RetroArchCfgEntry>,
) {
    constructor(load: () -> List<RetroArchCfgEntry>) : this(load())

    fun entries(): List<RetroArchCfgEntry> = eager

    fun onLoaded(action: () -> Unit) {
        action()
    }

    companion object {
        fun forTest(entries: List<RetroArchCfgEntry>): BundledAutoconfigEntries =
            BundledAutoconfigEntries(entries)
    }
}
