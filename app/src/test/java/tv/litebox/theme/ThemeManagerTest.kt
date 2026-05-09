package tv.litebox.theme

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tv.litebox.theme.builtin.BlueSteelTheme
import tv.litebox.theme.builtin.DarkDefaultTheme

class ThemeManagerTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)

        every { mockContext.getSharedPreferences("litebox_prefs", Context.MODE_PRIVATE) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `default theme is DarkDefaultTheme when no preference saved`() {
        // When no saved preference, getString returns the default DarkDefaultTheme.id
        every { mockPrefs.getString("active_theme_id", DarkDefaultTheme.id) } returns DarkDefaultTheme.id

        val manager = ThemeManager(mockContext)

        val theme = manager.currentTheme.value
        assertEquals("Default theme id should match", DarkDefaultTheme.id, theme.id)
        assertEquals("Default theme name", "Dark", theme.name)
    }

    @Test
    fun `builtinThemes list is not empty`() {
        every { mockPrefs.getString("active_theme_id", DarkDefaultTheme.id) } returns DarkDefaultTheme.id

        val manager = ThemeManager(mockContext)

        assertTrue("builtinThemes should not be empty", manager.builtinThemes.isNotEmpty())
        // Should contain at least the two known themes
        assertTrue("Should contain DarkDefaultTheme",
            manager.builtinThemes.any { it.id == "litebox.dark" })
        assertTrue("Should contain BlueSteelTheme",
            manager.builtinThemes.any { it.id == "litebox.blue-steel" })
    }

    @Test
    fun `builtinThemes has expected count`() {
        every { mockPrefs.getString("active_theme_id", DarkDefaultTheme.id) } returns DarkDefaultTheme.id

        val manager = ThemeManager(mockContext)

        assertEquals("Should have exactly 2 built-in themes", 2, manager.builtinThemes.size)
    }

    @Test
    fun `setTheme switches to BlueSteelTheme`() {
        every { mockPrefs.getString("active_theme_id", DarkDefaultTheme.id) } returns DarkDefaultTheme.id

        val manager = ThemeManager(mockContext)

        // Switch to Blue Steel
        manager.setTheme("litebox.blue-steel")

        // Verify preference was saved
        verify { mockEditor.putString("active_theme_id", "litebox.blue-steel") }
        verify { mockEditor.apply() }

        // Verify currentTheme flow updated
        val theme = manager.currentTheme.value
        assertEquals("litebox.blue-steel", theme.id)
        assertEquals("Blue Steel", theme.name)
    }

    @Test
    fun `setTheme switches back to DarkDefaultTheme`() {
        // Start with Blue Steel saved
        every { mockPrefs.getString("active_theme_id", DarkDefaultTheme.id) } returns "litebox.blue-steel"

        val manager = ThemeManager(mockContext)
        assertEquals("Should start with Blue Steel", "litebox.blue-steel", manager.currentTheme.value.id)

        // Switch back to Dark
        manager.setTheme("litebox.dark")

        verify { mockEditor.putString("active_theme_id", "litebox.dark") }
        assertEquals("litebox.dark", manager.currentTheme.value.id)
    }

    @Test
    fun `setTheme with unknown id does nothing`() {
        every { mockPrefs.getString("active_theme_id", DarkDefaultTheme.id) } returns DarkDefaultTheme.id

        val manager = ThemeManager(mockContext)
        val themeBefore = manager.currentTheme.value

        manager.setTheme("nonexistent-theme-id")

        // Should not have written to prefs
        verify(exactly = 0) { mockEditor.putString(any(), any()) }
        // Theme should be unchanged
        assertEquals(themeBefore.id, manager.currentTheme.value.id)
    }

    @Test
    fun `loads BlueSteelTheme from saved preference`() {
        every { mockPrefs.getString("active_theme_id", DarkDefaultTheme.id) } returns "litebox.blue-steel"

        val manager = ThemeManager(mockContext)

        assertEquals("Should load Blue Steel from prefs", "litebox.blue-steel", manager.currentTheme.value.id)
        assertEquals("Blue Steel", manager.currentTheme.value.name)
    }

    @Test
    fun `falls back to DarkDefaultTheme for invalid saved preference`() {
        // Simulate a saved theme id that doesn't match any builtin
        every { mockPrefs.getString("active_theme_id", DarkDefaultTheme.id) } returns "deleted.theme"

        val manager = ThemeManager(mockContext)

        // Should fall back to DarkDefaultTheme
        assertEquals("Should fall back to DarkDefaultTheme",
            DarkDefaultTheme.id, manager.currentTheme.value.id)
    }

    @Test
    fun `builtin themes have distinct ids`() {
        every { mockPrefs.getString("active_theme_id", DarkDefaultTheme.id) } returns DarkDefaultTheme.id

        val manager = ThemeManager(mockContext)
        val ids = manager.builtinThemes.map { it.id }

        assertEquals("All theme ids should be unique", ids.size, ids.toSet().size)
    }

    @Test
    fun `builtin themes have non-blank required fields`() {
        every { mockPrefs.getString("active_theme_id", DarkDefaultTheme.id) } returns DarkDefaultTheme.id

        val manager = ThemeManager(mockContext)
        for (theme in manager.builtinThemes) {
            assertTrue("Theme id should not be blank (${theme.name})", theme.id.isNotBlank())
            assertTrue("Theme name should not be blank (${theme.id})", theme.name.isNotBlank())
            assertTrue("Theme version should not be blank (${theme.id})", theme.version.isNotBlank())
            assertTrue("Theme author should not be blank (${theme.id})", theme.author.isNotBlank())
        }
    }
}
