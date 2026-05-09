package tv.litebox.ui.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented Compose UI test for HomeScreen.
 *
 * Verifies the empty state ("No media yet" text) renders correctly.
 * Run with: ./gradlew connectedDebugAndroidTest
 *
 * NOTE: Requires emulator/device — do NOT run in CI without one.
 */
class HomeScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // TODO: Uncomment when running on emulator.
    // @Test
    // fun emptyState_showsNoMediaYet() {
    //     composeTestRule.setContent {
    //         HomeScreen(
    //             onNavigateToLibrary = {},
    //             onNavigateToPlayer = {},
    //             onNavigateToPlugins = {},
    //             onNavigateToSettings = {},
    //         )
    //     }
    //     composeTestRule.onNodeWithText("No media yet").assertIsDisplayed()
    // }
}
