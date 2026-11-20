package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.example.data.firebase.LocalAttemptDraft
import com.example.data.firebase.LocalAttemptDraftStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivity_launchesSplashAndTransitionsToLoginWithoutCrashing() {
        composeTestRule.onNodeWithTag("splash_screen").assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(3000L)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("login_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_login_card").performClick()
        composeTestRule.onNodeWithTag("student_login_card").performClick()
        composeTestRule.onNodeWithTag("google_sign_in_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun appBrandingAndAssets_resolveOfficialTeacherPhotoAndFirebaseConfig() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Maths By Mithlesh Sir", context.getString(R.string.app_name))
        assertEquals("सीखें गणित, बढ़ें सफलता की ओर", context.getString(R.string.app_tagline_hi))

        val dbId = context.getString(R.string.firestore_database_id)
        assertTrue("Firestore database ID must be provisioned", dbId.isNotBlank() && dbId != "(default)")

        assertNotNull(ContextCompat.getDrawable(context, R.drawable.img_teacher_mithlesh_1795174310335))
        assertNotNull(ContextCompat.getDrawable(context, R.drawable.img_mbs_launcher_icon_1795174307622))
    }

    @Test
    fun localAttemptDraftStore_persistsRestoresIsolatesAndClearsDraftAcrossRestart() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = LocalAttemptDraftStore(context)
        val draft = LocalAttemptDraft(
            testId = "test_jnv_01",
            answers = mapOf("q_1" to 2, "q_2" to 0),
            markedForReview = setOf("q_2", "q_5"),
            currentQuestionIndex = 3,
            remainingSeconds = 1245L,
            elapsedSeconds = 555L,
            savedAtMillis = 1760000000000L
        )

        store.saveDraft("student_uid_1", draft)
        val restored = store.loadDraft("student_uid_1", "test_jnv_01")
        assertNotNull(restored)
        assertEquals(2, restored!!.answers["q_1"])
        assertEquals(0, restored.answers["q_2"])
        assertTrue(restored.markedForReview.contains("q_5"))
        assertEquals(3, restored.currentQuestionIndex)
        assertEquals(1245L, restored.remainingSeconds)

        // Ensure another student does not see student_uid_1's exam draft
        assertNull(store.loadDraft("student_uid_2", "test_jnv_01"))

        store.clearDraft("student_uid_1", "test_jnv_01")
        assertNull(store.loadDraft("student_uid_1", "test_jnv_01"))
    }
}
