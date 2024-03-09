package com.example.fiftysix;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.fiftysix", appContext.getPackageName());
    }
    /**
     * testing whether an attendee is able to go to the profile section
     * testing whether an attendee can edit his / her profile or not**/
    @Test
    public void edit_profile_check(){
        // going to attendee main page
        onView(withId(R.id.buttonAttendee)).perform(click());

        // going to profile section
        onView(withId(R.id.buttonAttendeeProfile)).perform(click());

        // person editing their name, email and contact info
        onView(withId(R.id.profile_name)).perform(ViewActions.typeText("Arsh Arora"));
        onView(withId(R.id.profile_email)).perform(ViewActions.typeText("arsharora0388@gmail.com"));
        onView(withId(R.id.profile_phone)).perform(ViewActions.typeText("5877784938"));
        onView(withId(R.id.profile_save)).perform(click());

    }

}