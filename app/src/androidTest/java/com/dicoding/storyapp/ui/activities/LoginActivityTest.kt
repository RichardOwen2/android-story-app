package com.dicoding.storyapp.ui.activities

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.dicoding.storyapp.EspressoIdlingResource
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.Result
import com.dicoding.storyapp.ui.viewmodels.LoginViewModel
import com.dicoding.storyapp.wrapEspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import javax.inject.Inject

class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Inject
    lateinit var loginViewModel: LoginViewModel

    private val testEmail = "testuser@example.com"
    private val testPassword = "testpassword"

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun testLogin() {
        val successResult = MutableLiveData<Result<Boolean>>()
        successResult.value = Result.Success(true)

        `when`(loginViewModel.login(testEmail, testPassword)).thenReturn(successResult)

        Espresso.onView(ViewMatchers.withId(R.id.ed_login_email))
            .perform(ViewActions.typeText(testEmail))
        Espresso.onView(ViewMatchers.withId(R.id.ed_login_password))
            .perform(ViewActions.typeText(testPassword))
        Espresso.onView(ViewMatchers.withId(R.id.loginButton))
            .perform(ViewActions.click())

        wrapEspressoIdlingResource {
            Espresso.onView(ViewMatchers.withId(R.id.activity_main))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }
}