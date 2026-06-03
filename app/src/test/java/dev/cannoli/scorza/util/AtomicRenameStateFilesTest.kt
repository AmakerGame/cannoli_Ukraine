package dev.cannoli.scorza.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AtomicRenameStateFilesTest {

    @get:Rule val tmp = TemporaryFolder()

    private fun write(f: File, text: String) { f.parentFile?.mkdirs(); f.writeText(text) }

    @Test fun `rename renames state dir AND the files inside it`() {
        val root = tmp.root
        val tag = "SNES"
        val old = "Old Game"
        val new = "New Game"
        val rom = File(root, "Roms/$tag/$old.sfc").apply { parentFile?.mkdirs(); writeText("rom") }
        write(File(root, "Save States/$tag/$old/$old.state"), "S")
        write(File(root, "Save States/$tag/$old/$old.state.png"), "P")

        val result = AtomicRename(root).rename(rom, new, tag)

        assertTrue(result.success)
        assertTrue(File(root, "Save States/$tag/$new/$new.state").exists())
        assertEquals("S", File(root, "Save States/$tag/$new/$new.state").readText())
        assertEquals("P", File(root, "Save States/$tag/$new/$new.state.png").readText())
        assertFalse(File(root, "Save States/$tag/$new/$old.state").exists())
    }
}
