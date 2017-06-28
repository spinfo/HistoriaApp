package de.smarthistory;

import android.content.Context;
import android.graphics.Point;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.internal.util.Checks;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.views.MapView;

import java.util.List;

import de.smarthistory.data.Area;
import de.smarthistory.data.DataFacade;
import de.smarthistory.data.Mapstop;
import de.smarthistory.data.Tour;

import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.Espresso.*;
import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule mainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("de.smarthistory", appContext.getPackageName());
    }

    @Test
    public void testChooseDefaultAreaLandscape() {

        testDefaultArea();
    }




    private void testDefaultArea() {
        // get the neccessary data
        DataFacade data = new DataFacade(mainActivityRule.getActivity());
        Area defaultArea = data.getDefaultArea();
        List<Tour> tours = defaultArea.getTours();
        Checks.checkState(!tours.isEmpty(), "Should test an area with at least one tour");

        // open the selection for areas and make sure that every area is present
        openDrawerMenu();
        onView(withText(R.string.menu_select_area))
                .perform(click());

        for (Area area : data.getAreas()) {
            onData(areaWIthName(area.getName()))
                    .inAdapterView(withId(R.id.area_list))
                    .check(matches(isDisplayed()));
        }

        // choose the default area
        onData(areaWIthName(defaultArea.getName()))
                .perform(click());

        // better to idle: https://stackoverflow.com/a/22563297/1879728
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d("XXX", "interrupted....");
        }

        // at this point the map should have been drawn, check that a mapstop is drawn
        // NOTE: clicking a mapstop moves the map and might bring other mapstops out of the
        // visible area. Therefore only one mapstop is checked at the moment.
        // TODO: In order to check all mapstops we'd need a method to scroll the map to each
        checkMapstopOnMap(tours.get(0).getMapstops().get(0));
    }

    private void checkMapstopOnMap(Mapstop stop) {
        // get the location of the stop marker on the screen
        MapView map = (MapView) mainActivityRule.getActivity().findViewById(R.id.map);
        Point p = map.getProjection().toPixels(stop.getPlace().getLocation(), null);

        // clicking the marker should bring up an Infowindow
        onView(withId(R.id.map))
                .perform(clickXY(p.x, p.y));

        // better to idle: https://stackoverflow.com/a/22563297/1879728
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d("XXX", "interrupted....");
        }

        onView(withId(R.id.map_my_bonuspack_bubble))
                .check(matches(isDisplayed()));
    }

    // opens the left nav drawer
    private static void openDrawerMenu() {
        onView(allOf(
                isDescendantOfA(withId(R.id.main_toolbar)),
                withClassName(endsWith("AppCompatImageButton"))
        )).perform(click());
    }

    // closes the left nav drawer
    private static void closeDrawerMenu() {
        onView(withId(R.id.left_drawer))
                .perform(swipeLeft());
    }

    // A custom view action used to click on a specific screen coordinate
    private static ViewAction clickXY(final int x, final int y){
        return new GeneralClickAction(
                Tap.SINGLE,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);

                        final float screenX = screenPos[0] + x;
                        final float screenY = screenPos[1] + y;
                        return new float[] {screenX, screenY};
                    }
                },
                Press.FINGER);
    }

    // A matcher for an area
    private static Matcher<Object> areaWIthName(String expectedName) {
        Checks.checkNotNull(expectedName);
        return areaWithName(equalTo(expectedName));
    }

    private static Matcher<Object> areaWithName(final Matcher<String> itemMatcher) {
        Checks.checkNotNull(itemMatcher);

        return new BoundedMatcher<Object, Area>(Area.class) {
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("ArtistTime with name: ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(Area area) {
                return itemMatcher.matches(area.getName());
            }
        };
    }
}
