// attendee test

package com.example.fiftysix;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static java.lang.Thread.sleep;
import static java.util.EnumSet.allOf;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;

public class AttendeeTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    /**
     * testing whether an attendee is able to go to the profile section
     * testing whether an attendee can edit his / her profile or not
     **/
    @Test
    public void edit_profile_check() {
        // going to attendee main page
        onView(withId(R.id.buttonAttendee)).perform(click());

        // going to profile section
        onView(withId(R.id.buttonAttendeeProfile)).perform(click());

        // person editing their name, email, and contact info
        // Clear the already written text in all those fields
        onView(withId(R.id.profile_name)).perform(click(), clearText()).perform(typeText("Arsh Arora")).perform(ViewActions.closeSoftKeyboard());
        onView(withId(R.id.profile_email)).perform(click(), clearText()).perform(typeText("arsharora0388@gmail.com")).perform(ViewActions.closeSoftKeyboard());
        onView(withId(R.id.profile_phone)).perform(click(), clearText()).perform((typeText("5877784938"))).perform(ViewActions.closeSoftKeyboard());


        // Click the save button and it goes back
        onView(withId(R.id.profile_save)).perform(click());

    }

    // working till now
    /* todo
        constraint this test is only working for the names which have unique names since recycler view actions is not supported til now.
        */
    @Test
    public void browse_event() throws InterruptedException {
        // going to attendee main page
        onView(withId(R.id.buttonAttendee)).perform(click());

        // clicking on the spinner item
        onView(withId(R.id.menuButtonMyEvents)).perform(click());

        onData(allOf(is(instanceOf(String.class)), is("Browse All Events"))).perform(click());
        // checking event details.
        onView(withText("Arsh_Arora")).perform(scrollTo(),click());
        onView(withId(R.id.SignupEvents)).perform(scrollTo(),click());
    }
    /* todo
    *   constraints - events should gave unique name . test cant pass if multiple events have same name.
    *   leaving the event is not working  properly */
    @Test
    public void check_sign_up_events_and_leave() {
        // going to attendee main page
        onView(withId(R.id.buttonAttendee)).perform(click());

        // clicking on the spinner item
        onView(withId(R.id.menuButtonMyEvents)).perform(click());

        onData(allOf(is(instanceOf(String.class)), is("Event Sign-ups"))).perform(click());
        // enter the event name. below.
        onView(withText("Arsh_Arora")).perform(scrollTo(),click());

        // leave the event...
        // onView(withId(R.id.leave_event)).perform(click());


    }

    @Test
    public void check_event_check_in() {
        onView(withId(R.id.buttonAttendee)).perform(click());

        // clicking on the spinner item
        onView(withId(R.id.menuButtonMyEvents)).perform(click());

        onData(allOf(is(instanceOf(String.class)), is("Event Check-ins"))).perform(click());
        // edit the event name according to yourself.
    }
}

