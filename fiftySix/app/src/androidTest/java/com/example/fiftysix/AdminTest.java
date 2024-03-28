
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

import static java.lang.Thread.sleep;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

public class AdminTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);
    /* Todo browsing profile images and deleting them from the database
    *   if the image or event is deleted by admin then check whether the profile image is being deleted from attenddees profile as or now
    *   need to fix the problem of data loading for this.
    * */
    @Test
    public void check_admin_navigation_buttons() throws InterruptedException {
        // checking all the navigation buttons for admin whether the admin can navigate through all its activities or not.
        // onView(withId(R.id.buttonAdmin)).perform((click()));
        // onView(withId(R.id.browseEvents)).perform(click());
        // onView(withId(R.id.backButton)).perform(click());

        // browsing profiles
//        onView(withId(R.id.browseImages)).perform(click());
//        onView(withId(R.id.profile_image)).perform(click());


    }
    @Test
    public void browse_events(){
        // going to admin panel
//         onView(withId(R.id.buttonAdmin)).perform((click()));
//         // going to browse events
//         onView(withId(R.id.browseEvents)).perform(click());
//
//        //  showing list of events
//        onData(allOf(is(instanceOf(String.class)), is("Brady's Event"))).perform(click());


    }
}
