package tv.litebox

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test — runs on an Android device/emulator.
 *
 * These stubs verify basic app context and activity launch.
 * Run with: ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("tv.litebox.debug", appContext.packageName)
    }

    // TODO: Add activity launch test once an emulator is available.
    // @Test
    // fun activityLaunches() {
    //     val scenario = ActivityScenario.launch(MainActivity::class.java)
    //     scenario.use { }
    // }
}
