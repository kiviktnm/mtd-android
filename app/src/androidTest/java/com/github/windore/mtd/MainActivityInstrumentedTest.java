package com.github.windore.mtd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.DayOfWeek;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {

    @Test
    public void savingAndReadingToJsonWorks() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        scenario.onActivity(activity -> {
            assertNull(activity.getMtd().getItemBody(new MtdItemRef(0, MtdItemRef.Type.Todo)));
            activity.getMtd().addTodo("Todo", DayOfWeek.MONDAY);
            assertEquals("Todo", activity.getMtd().getItemBody(new MtdItemRef(0, MtdItemRef.Type.Todo)));
        });

        scenario.recreate();

        scenario.onActivity(activity -> assertEquals("Todo", activity.getMtd().getItemBody(new MtdItemRef(0, MtdItemRef.Type.Todo))));
    }
}
