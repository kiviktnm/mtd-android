package com.github.windore.mtd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.DayOfWeek;
import java.util.List;

// Due to the fact that these methods test the underlying rust libraries, these test count as
// instrumented tests.
@RunWith(AndroidJUnit4.class)
public class MtdInstrumentedTest {

    @Test(expected = IllegalArgumentException.class)
    public void invalidJsonThrowsException() {
        new Mtd("invalid json");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullJsonThrowsException() {
        new Mtd(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullTodoBodyThrowsException() {
        Mtd mtd = new Mtd();
        mtd.addTodo(null, DayOfWeek.MONDAY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullTaskBodyThrowsException() {
        Mtd mtd = new Mtd();
        mtd.addTask(null, new DayOfWeek[] { DayOfWeek.MONDAY });
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyTaskWeekdayListThrowsException() {
        Mtd mtd = new Mtd();
        mtd.addTask("Task", new DayOfWeek[] {});
    }

    @Test
    public void invalidItemIdReturnsNullBody() {
        Mtd mtd = new Mtd();
        assertNull(mtd.getItemBody(MtdItem.Type.Todo, 0));
        assertNull(mtd.getItemBody(MtdItem.Type.Task, 0));
    }

    @Test
    public void validItemIdReturnsCorrectBody() {
        Mtd mtd = new Mtd();
        mtd.addTodo("Todo 1", DayOfWeek.MONDAY);
        mtd.addTask("Task 1", new DayOfWeek[] { DayOfWeek.MONDAY });

        assertEquals("Todo 1", mtd.getItemBody(MtdItem.Type.Todo, 0));
        assertEquals("Task 1", mtd.getItemBody(MtdItem.Type.Task, 0));
    }

    @Test
    public void getItemsForWeekdayReturnsEmpty() {
        Mtd mtd = new Mtd();

        assertTrue(mtd.getItemsForWeekday(MtdItem.Type.Todo, DayOfWeek.MONDAY, true).isEmpty());
        assertTrue(mtd.getItemsForWeekday(MtdItem.Type.Todo, DayOfWeek.MONDAY, false).isEmpty());
        assertTrue(mtd.getItemsForWeekday(MtdItem.Type.Task, DayOfWeek.MONDAY, true).isEmpty());
        assertTrue(mtd.getItemsForWeekday(MtdItem.Type.Task, DayOfWeek.MONDAY, false).isEmpty());
    }

    @Test
    public void getTodosReturnsCorrect() {
        Mtd mtd = new Mtd();

        mtd.addTodo("Monday", DayOfWeek.MONDAY);
        mtd.addTodo("Monday", DayOfWeek.MONDAY);
        mtd.addTodo("Tuesday", DayOfWeek.TUESDAY);

        List<MtdItem> mondaysItems = mtd.getItemsForWeekday(MtdItem.Type.Todo, DayOfWeek.MONDAY, false);

        assertEquals(2, mondaysItems.size());
        for (MtdItem monday : mondaysItems) {
            assertEquals("Monday", mtd.getItemBody(monday.getType(), monday.getId()));
        }

        List<MtdItem> tuesdaysItems = mtd.getItemsForWeekday(MtdItem.Type.Todo, DayOfWeek.TUESDAY, false);

        assertEquals(1, tuesdaysItems.size());
        for (MtdItem tuesday : tuesdaysItems) {
            assertEquals("Tuesday", mtd.getItemBody(tuesday.getType(), tuesday.getId()));
        }
    }

    @Test
    public void getTasksReturnsCorrect() {
        Mtd mtd = new Mtd();

        mtd.addTask("Saturday", new DayOfWeek[]{ DayOfWeek.SATURDAY });
        mtd.addTask("Saturday", new DayOfWeek[]{ DayOfWeek.SATURDAY });
        mtd.addTask("Sunday", new DayOfWeek[]{ DayOfWeek.SUNDAY });

        List<MtdItem> saturdayItems = mtd.getItemsForWeekday(MtdItem.Type.Task, DayOfWeek.SATURDAY, false);

        assertEquals(2, saturdayItems.size());
        for (MtdItem item : saturdayItems) {
            assertEquals("Saturday", mtd.getItemBody(item.getType(), item.getId()));
        }

        List<MtdItem> sundayItems = mtd.getItemsForWeekday(MtdItem.Type.Task, DayOfWeek.SUNDAY, false);

        assertEquals(1, sundayItems.size());
        for (MtdItem item : sundayItems) {
            assertEquals("Sunday", mtd.getItemBody(item.getType(), item.getId()));
        }
    }

    @Test
    public void removeItemRemovesItems() {
        Mtd mtd = new Mtd();

        mtd.addTodo("Todo", DayOfWeek.MONDAY);
        mtd.addTask("Task", new DayOfWeek[]{ DayOfWeek.SUNDAY });

        mtd.removeItem(new MtdItem(0, MtdItem.Type.Todo));
        mtd.removeItem(new MtdItem(0, MtdItem.Type.Task));

        assertEquals(0, mtd.getItemsForWeekday(MtdItem.Type.Todo, DayOfWeek.MONDAY, false).size());
        assertEquals(0, mtd.getItemsForWeekday(MtdItem.Type.Task, DayOfWeek.SUNDAY, false).size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removingNonExistentThrowsException() {
        Mtd mtd = new Mtd();
        mtd.removeItem(new MtdItem(0, MtdItem.Type.Todo));
    }

    @Test
    public void settingTodosDoneAndUndoneWorks() {
        Mtd mtd = new Mtd();

        mtd.addTodo("Done", DayOfWeek.MONDAY);
        mtd.addTodo("Done", DayOfWeek.MONDAY);
        mtd.addTodo("Done", DayOfWeek.MONDAY);

        List<MtdItem> undoneAtStart = mtd.getItemsForWeekday(MtdItem.Type.Todo, DayOfWeek.MONDAY, false);

        assertEquals(3, undoneAtStart.size());

        mtd.modifyItemDoneState(new MtdItem(0, MtdItem.Type.Todo), true, DayOfWeek.MONDAY);
        mtd.modifyItemDoneState(new MtdItem(1, MtdItem.Type.Todo), true, DayOfWeek.MONDAY);
        mtd.modifyItemDoneState(new MtdItem(2, MtdItem.Type.Todo), true, DayOfWeek.MONDAY);

        List<MtdItem> allDoneInTheMiddle = mtd.getItemsForWeekday(MtdItem.Type.Todo, DayOfWeek.MONDAY, true);

        assertEquals(3, allDoneInTheMiddle.size());

        mtd.modifyItemDoneState(new MtdItem(1, MtdItem.Type.Todo), false, DayOfWeek.MONDAY);

        List<MtdItem> undoneItemsAtEnd = mtd.getItemsForWeekday(MtdItem.Type.Todo, DayOfWeek.MONDAY, false);
        List<MtdItem> doneItemsAtEnd = mtd.getItemsForWeekday(MtdItem.Type.Todo, DayOfWeek.MONDAY, true);

        assertEquals(1, undoneItemsAtEnd.size());
        assertEquals(2, doneItemsAtEnd.size());
    }

    @Test
    public void settingTasksDoneAndUndoneWorks() {
        Mtd mtd = new Mtd();

        mtd.addTask("Done", new DayOfWeek[] { DayOfWeek.SUNDAY });
        mtd.addTask("Done", new DayOfWeek[] { DayOfWeek.SUNDAY });
        mtd.addTask("Done", new DayOfWeek[] { DayOfWeek.SUNDAY });

        List<MtdItem> undoneAtStart = mtd.getItemsForWeekday(MtdItem.Type.Task, DayOfWeek.SUNDAY, false);

        assertEquals(3, undoneAtStart.size());

        mtd.modifyItemDoneState(new MtdItem(0, MtdItem.Type.Task), true, DayOfWeek.SUNDAY);
        mtd.modifyItemDoneState(new MtdItem(1, MtdItem.Type.Task), true, DayOfWeek.SUNDAY);
        mtd.modifyItemDoneState(new MtdItem(2, MtdItem.Type.Task), true, DayOfWeek.SUNDAY);

        List<MtdItem> allDoneInTheMiddle = mtd.getItemsForWeekday(MtdItem.Type.Task, DayOfWeek.SUNDAY, true);

        assertEquals(3, allDoneInTheMiddle.size());

        mtd.modifyItemDoneState(new MtdItem(1, MtdItem.Type.Task), false, DayOfWeek.SUNDAY);

        List<MtdItem> undoneItemsAtEnd = mtd.getItemsForWeekday(MtdItem.Type.Task, DayOfWeek.SUNDAY, false);
        List<MtdItem> doneItemsAtEnd = mtd.getItemsForWeekday(MtdItem.Type.Task, DayOfWeek.SUNDAY, true);

        assertEquals(1, undoneItemsAtEnd.size());
        assertEquals(2, doneItemsAtEnd.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void modifyingNonExistentThrowsException() {
        Mtd mtd = new Mtd();

        mtd.modifyItemDoneState(new MtdItem(0, MtdItem.Type.Task), false, DayOfWeek.FRIDAY);
    }

    @Test
    public void mtdToFromJsonWorks() {
        String json = "{\"todos\":{\"items\":[{\"body\":\"Todo\",\"date\":\"2022-07-17\",\"id\":0,\"done\":null,\"sync_id\":2346981044091297941,\"state\":\"Unchanged\"}],\"server\":false},\"tasks\":{\"items\":[{\"body\":\"task\",\"weekdays\":[\"Sun\"],\"done_map\":{},\"id\":0,\"state\":\"Unchanged\",\"sync_id\":2130429732854137202}],\"server\":false},\"server\":false}";

        Mtd mtd = new Mtd(json);

        assertEquals("Todo", mtd.getItemBody(new MtdItem(0, MtdItem.Type.Todo)));
        assertEquals("task", mtd.getItemBody(new MtdItem(0, MtdItem.Type.Task)));

        assertEquals(json, mtd.toJson());
    }
}