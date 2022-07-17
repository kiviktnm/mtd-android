package com.github.windore.mtd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.DayOfWeek;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {

    @Test
    public void savingAndReadingToJsonWorks() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        scenario.onActivity(activity -> {
            assertNull(activity.getMtd().getItemBody(new MtdItem(0, MtdItem.Type.Todo)));
            activity.getMtd().addTodo("Todo", DayOfWeek.MONDAY);
            assertEquals("Todo", activity.getMtd().getItemBody(new MtdItem(0, MtdItem.Type.Todo)));
        });

        scenario.recreate();

        scenario.onActivity(activity -> assertEquals("Todo", activity.getMtd().getItemBody(new MtdItem(0, MtdItem.Type.Todo))));
    }

}
