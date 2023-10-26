package com.dicoding.storyapp.ui.activities

import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.dicoding.storyapp.EspressoIdlingResource
import com.dicoding.storyapp.R
import com.dicoding.storyapp.wrapEspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private val testEmail = "kucing@mail.com"
    private val testPassword = "kucing123"

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        Intents.release()
    }

    @Test
    fun testSuccessLogin() {
        Intents.init()

        Espresso.onView(ViewMatchers.withId(R.id.ed_login_email))
            .perform(ViewActions.typeText(testEmail))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.ed_login_password))
            .perform(ViewActions.typeText(testPassword))
        Espresso.closeSoftKeyboard()

        Espresso.onView(ViewMatchers.withId(R.id.loginButton))
            .perform(ViewActions.click())

        wrapEspressoIdlingResource {
            intended(hasComponent(MainActivity::class.java.name))
        }
    }

    @Test
    fun testFailedLogin() {
        Intents.init()

        Espresso.onView(ViewMatchers.withId(R.id.ed_login_email))
            .perform(ViewActions.typeText(testEmail))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.ed_login_password))
            .perform(ViewActions.typeText("salahhhhhhh"))
        Espresso.closeSoftKeyboard()

        Espresso.onView(ViewMatchers.withId(R.id.loginButton))
            .perform(ViewActions.click())

        wrapEspressoIdlingResource {
            Espresso.onView(ViewMatchers.withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Invalid Password")))
        }
    }
}