// admin test
package com.example.fiftysix;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

public class AdminTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void check_admin_navigation_buttons(){
        onView(withId(R.id.buttonAdmin)).perform((click()));
        onView(allOf(withId(R.id.browseEvents), isDescendantOfA(withId(R.id.home_page_admin))))
                .perform(click());
    }
    @Test
    public void browse_events(){
        // going to admin panel
        onView(withId(R.id.buttonAdmin)).perform((click()));
        // going to browse events
        onView(withId(R.id.browseEvents)).perform(click());
//        onView(allOf(withId(R.id.browseEvents), isDescendantOfA(withId(R.id.home_page_admin))))
//                .perform(click());
//
//        //  showing list of events
//        // onData(allOf(is(instanceOf(String.class)), is("Brady's Event"))).perform(click());


    }
}
