package com.example.fiftysix;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withInputType;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.widget.EditText;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;

public class OrganizerUiTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);
    /** Testing whether the navigation buttons are working otr not **/
    @Test
    public void Organizer_page_navigation(){
        // going to organizer home
        onView(withId(R.id.buttonOrganizer)).perform(click());
        // going to create event page and coming back without creating an event
        onView(withId(R.id.buttonAddEvent)).perform(click());
        onView(withId(R.id.buttonBackCreateEvent)).perform(click());

    }
    /**  Testing whether the add event page i is working fine or not**/
    @Test
    public void Add_event_details() {
        // going to organizer home
        onView(withId(R.id.buttonOrganizer)).perform(click());

        // going to create event page.
        onView(withId(R.id.buttonAddEvent)).perform(click());

        String event_name = "Arsh_Arora";
        String event_details = "this is a test event to check whether everything is working fine or not ";
        String address = "2048 88st SW Edmonton";
        String date = "March 9 2024";

        // Perform action: Clear existing text and type new text into the EditText
        onView(withId(R.id.eventNameEditText)).perform(clearText(), typeText(event_name), closeSoftKeyboard());

        // Perform action: Clear existing text and type new text into the event_details
        onView(withId(R.id.eventDetailsEditText)).perform(clearText(), typeText(event_details), closeSoftKeyboard());

        // Perform action: Clear existing text and type new text into the address
        onView(withId(R.id.eventAddressEditText)).perform(clearText(), typeText(address), closeSoftKeyboard());

        // Perform action: Clear existing text and type new text into the date section
        onView(withId(R.id.eventDateEditText)).perform(clearText(), typeText(date), closeSoftKeyboard());
        // perform action creating the event .
        onView(withId(R.id.buttonBackCreateEvent)).perform(click());
    }

    @Test
    public void check_set_limit(){
        // going to organizer home
        onView(withId(R.id.buttonOrganizer)).perform(click());

        // going to create event page.
        onView(withId(R.id.buttonAddEvent)).perform(click());

        // click on the set attendee limit button and
        onView(withId(R.id.switchAttendeeLimit)).perform(click());
        // popping up adapter view to set the limit .
        onView(ViewMatchers.withInputType(InputType.TYPE_CLASS_NUMBER))
                .inRoot(isDialog()) // Specify that we're looking inside a dialog
                .perform(ViewActions.typeText("100"), ViewActions.closeSoftKeyboard());

        // Click OK button in the AlertDialog
        onView(ViewMatchers.withText("OK"))
                .inRoot(isDialog()) // Specify that we're looking inside a dialog
                .perform(ViewActions.click());

    }
  /* todo - need to fix the adapter loading data problem */
    @Test
    public void send_notification() throws InterruptedException {
        // going to organizer home
//        onView(withId(R.id.buttonOrganizer)).perform(click());
//        // clicking on one of the events
//        sleep(3000);
//        onData(withText("Arsh_Arora")).check(matches(isDisplayed()));
//        onData(withText("Arsh_Arora")).perform(click());
//        onView(withId(R.id.notify)).perform(scrollTo(),click());
//        onView(withId(R.id.message)).perform(clearText(),
//        typeText("This is just a test notification"));
//        onView(withId(R.id.send_button)).perform(click());


    }


}


