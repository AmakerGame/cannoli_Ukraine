package dev.cannoli.scorza.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AtomicRenameRelocateTest {

    @get:Rule val tmp = TemporaryFolder()

    private fun write(f: File, text: String) { f.parentFile?.mkdirs(); f.writeText(text) }

    @Test fun `relocate moves state dir inner files and srm to new base`() {
        val root = tmp.root
        val oldBase = "Inner Name (USA)"
        val newBase = "Outer"
        val tag = "SNES"
        write(File(root, "Save States/$tag/$oldBase/$oldBase.state"), "S")
        write(File(root, "Save States/$tag/$oldBase/$oldBase.state.auto"), "A")
        write(File(root, "Save States/$tag/$oldBase/$oldBase.state.png"), "P")
        write(File(root, "Saves/$tag/$oldBase.srm"), "R")

        val result = AtomicRename(root).relocateSaveData(tag, oldBase, newBase)

        assertTrue(result.success)
        assertFalse(File(root, "Save States/$tag/$oldBase").exists())
        assertEquals("S", File(root, "Save States/$tag/$newBase/$newBase.state").readText())
        assertEquals("A", File(root, "Save States/$tag/$newBase/$newBase.state.auto").readText())
        assertEquals("P", File(root, "Save States/$tag/$newBase/$newBase.state.png").readText())
        assertEquals("R", File(root, "Saves/$tag/$newBase.srm").readText())
    }

    @Test fun `relocate is a noop when nothing exists under old base`() {
        val result = AtomicRename(tmp.root).relocateSaveData("SNES", "Nope", "Other")
        assertTrue(result.success)
    }

    @Test fun `relocate moves srm when no state dir exists`() {
        val root = tmp.root
        write(File(root, "Saves/SNES/Old.srm"), "R")
        val result = AtomicRename(root).relocateSaveData("SNES", "Old", "New")
        assertTrue(result.success)
        assertFalse(File(root, "Saves/SNES/Old.srm").exists())
        assertEquals("R", File(root, "Saves/SNES/New.srm").readText())
    }

    @Test fun `relocate succeeds when an empty target state dir exists`() {
        val root = tmp.root
        val tag = "SNES"
        write(File(root, "Save States/$tag/Old/Old.state"), "S")
        File(root, "Save States/$tag/New").mkdirs()
        val result = AtomicRename(root).relocateSaveData(tag, "Old", "New")
        assertTrue(result.success)
        assertEquals("S", File(root, "Save States/$tag/New/New.state").readText())
    }

    @Test fun `relocate moves all matching save files not just srm`() {
        val root = tmp.root
        val tag = "GBC"
        write(File(root, "Saves/$tag/Old.srm"), "S")
        write(File(root, "Saves/$tag/Old.rtc"), "T")
        val result = AtomicRename(root).relocateSaveData(tag, "Old", "New")
        assertTrue(result.success)
        assertEquals("S", File(root, "Saves/$tag/New.srm").readText())
        assertEquals("T", File(root, "Saves/$tag/New.rtc").readText())
        assertFalse(File(root, "Saves/$tag/Old.srm").exists())
        assertFalse(File(root, "Saves/$tag/Old.rtc").exists())
    }
}
