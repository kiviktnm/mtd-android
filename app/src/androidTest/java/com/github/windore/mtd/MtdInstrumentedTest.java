package com.github.windore.mtd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        mtd.addTask(null, new DayOfWeek[]{DayOfWeek.MONDAY});
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyTaskWeekdayListThrowsException() {
        Mtd mtd = new Mtd();
        mtd.addTask("Task", new DayOfWeek[]{});
    }

    @Test
    public void invalidItemIdReturnsNullBody() {
        Mtd mtd = new Mtd();
        assertNull(mtd.getItemBody(MtdItemRef.Type.Todo, 0));
        assertNull(mtd.getItemBody(MtdItemRef.Type.Task, 0));
    }

    @Test
    public void validItemIdReturnsCorrectBody() {
        Mtd mtd = new Mtd();
        mtd.addTodo("Todo 1", DayOfWeek.MONDAY);
        mtd.addTask("Task 1", new DayOfWeek[]{DayOfWeek.MONDAY});

        assertEquals("Todo 1", mtd.getItemBody(MtdItemRef.Type.Todo, 0));
        assertEquals("Task 1", mtd.getItemBody(MtdItemRef.Type.Task, 0));
    }

    @Test
    public void getItemsForWeekdayReturnsEmpty() {
        Mtd mtd = new Mtd();

        assertTrue(mtd.getItemsForWeekday(MtdItemRef.Type.Todo, DayOfWeek.MONDAY, true).isEmpty());
        assertTrue(mtd.getItemsForWeekday(MtdItemRef.Type.Todo, DayOfWeek.MONDAY, false).isEmpty());
        assertTrue(mtd.getItemsForWeekday(MtdItemRef.Type.Task, DayOfWeek.MONDAY, true).isEmpty());
        assertTrue(mtd.getItemsForWeekday(MtdItemRef.Type.Task, DayOfWeek.MONDAY, false).isEmpty());
    }

    @Test
    public void getTodosReturnsCorrect() {
        Mtd mtd = new Mtd();

        mtd.addTodo("Monday", DayOfWeek.MONDAY);
        mtd.addTodo("Monday", DayOfWeek.MONDAY);
        mtd.addTodo("Tuesday", DayOfWeek.TUESDAY);

        List<MtdItemRef> mondaysItems = mtd.getItemsForWeekday(MtdItemRef.Type.Todo, DayOfWeek.MONDAY, false);

        assertEquals(2, mondaysItems.size());
        for (MtdItemRef monday : mondaysItems) {
            assertEquals("Monday", mtd.getItemBody(monday.getType(), monday.getId()));
        }

        List<MtdItemRef> tuesdaysItems = mtd.getItemsForWeekday(MtdItemRef.Type.Todo, DayOfWeek.TUESDAY, false);

        assertEquals(1, tuesdaysItems.size());
        for (MtdItemRef tuesday : tuesdaysItems) {
            assertEquals("Tuesday", mtd.getItemBody(tuesday.getType(), tuesday.getId()));
        }
    }

    @Test
    public void getTasksReturnsCorrect() {
        Mtd mtd = new Mtd();

        mtd.addTask("Saturday", new DayOfWeek[]{DayOfWeek.SATURDAY});
        mtd.addTask("Saturday", new DayOfWeek[]{DayOfWeek.SATURDAY});
        mtd.addTask("Sunday", new DayOfWeek[]{DayOfWeek.SUNDAY});

        List<MtdItemRef> saturdayItems = mtd.getItemsForWeekday(MtdItemRef.Type.Task, DayOfWeek.SATURDAY, false);

        assertEquals(2, saturdayItems.size());
        for (MtdItemRef item : saturdayItems) {
            assertEquals("Saturday", mtd.getItemBody(item.getType(), item.getId()));
        }

        List<MtdItemRef> sundayItems = mtd.getItemsForWeekday(MtdItemRef.Type.Task, DayOfWeek.SUNDAY, false);

        assertEquals(1, sundayItems.size());
        for (MtdItemRef item : sundayItems) {
            assertEquals("Sunday", mtd.getItemBody(item.getType(), item.getId()));
        }
    }

    @Test
    public void removeItemRemovesItems() {
        Mtd mtd = new Mtd();

        mtd.addTodo("Todo", DayOfWeek.MONDAY);
        mtd.addTask("Task", new DayOfWeek[]{DayOfWeek.SUNDAY});

        mtd.removeItem(new MtdItemRef(0, MtdItemRef.Type.Todo));
        mtd.removeItem(new MtdItemRef(0, MtdItemRef.Type.Task));

        assertEquals(0, mtd.getItemsForWeekday(MtdItemRef.Type.Todo, DayOfWeek.MONDAY, false).size());
        assertEquals(0, mtd.getItemsForWeekday(MtdItemRef.Type.Task, DayOfWeek.SUNDAY, false).size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removingNonExistentThrowsException() {
        Mtd mtd = new Mtd();
        mtd.removeItem(new MtdItemRef(0, MtdItemRef.Type.Todo));
    }

    @Test
    public void settingTodosDoneAndUndoneWorks() {
        Mtd mtd = new Mtd();

        mtd.addTodo("Done", DayOfWeek.MONDAY);
        mtd.addTodo("Done", DayOfWeek.MONDAY);
        mtd.addTodo("Done", DayOfWeek.MONDAY);

        List<MtdItemRef> undoneAtStart = mtd.getItemsForWeekday(MtdItemRef.Type.Todo, DayOfWeek.MONDAY, false);

        assertEquals(3, undoneAtStart.size());

        mtd.modifyItemDoneState(new MtdItemRef(0, MtdItemRef.Type.Todo), true, DayOfWeek.MONDAY);
        mtd.modifyItemDoneState(new MtdItemRef(1, MtdItemRef.Type.Todo), true, DayOfWeek.MONDAY);
        mtd.modifyItemDoneState(new MtdItemRef(2, MtdItemRef.Type.Todo), true, DayOfWeek.MONDAY);

        List<MtdItemRef> allDoneInTheMiddle = mtd.getItemsForWeekday(MtdItemRef.Type.Todo, DayOfWeek.MONDAY, true);

        assertEquals(3, allDoneInTheMiddle.size());

        mtd.modifyItemDoneState(new MtdItemRef(1, MtdItemRef.Type.Todo), false, DayOfWeek.MONDAY);

        List<MtdItemRef> undoneItemsAtEnd = mtd.getItemsForWeekday(MtdItemRef.Type.Todo, DayOfWeek.MONDAY, false);
        List<MtdItemRef> doneItemsAtEnd = mtd.getItemsForWeekday(MtdItemRef.Type.Todo, DayOfWeek.MONDAY, true);

        assertEquals(1, undoneItemsAtEnd.size());
        assertEquals(2, doneItemsAtEnd.size());
    }

    @Test
    public void settingTasksDoneAndUndoneWorks() {
        Mtd mtd = new Mtd();

        mtd.addTask("Done", new DayOfWeek[]{DayOfWeek.SUNDAY});
        mtd.addTask("Done", new DayOfWeek[]{DayOfWeek.SUNDAY});
        mtd.addTask("Done", new DayOfWeek[]{DayOfWeek.SUNDAY});

        List<MtdItemRef> undoneAtStart = mtd.getItemsForWeekday(MtdItemRef.Type.Task, DayOfWeek.SUNDAY, false);

        assertEquals(3, undoneAtStart.size());

        mtd.modifyItemDoneState(new MtdItemRef(0, MtdItemRef.Type.Task), true, DayOfWeek.SUNDAY);
        mtd.modifyItemDoneState(new MtdItemRef(1, MtdItemRef.Type.Task), true, DayOfWeek.SUNDAY);
        mtd.modifyItemDoneState(new MtdItemRef(2, MtdItemRef.Type.Task), true, DayOfWeek.SUNDAY);

        List<MtdItemRef> allDoneInTheMiddle = mtd.getItemsForWeekday(MtdItemRef.Type.Task, DayOfWeek.SUNDAY, true);

        assertEquals(3, allDoneInTheMiddle.size());

        mtd.modifyItemDoneState(new MtdItemRef(1, MtdItemRef.Type.Task), false, DayOfWeek.SUNDAY);

        List<MtdItemRef> undoneItemsAtEnd = mtd.getItemsForWeekday(MtdItemRef.Type.Task, DayOfWeek.SUNDAY, false);
        List<MtdItemRef> doneItemsAtEnd = mtd.getItemsForWeekday(MtdItemRef.Type.Task, DayOfWeek.SUNDAY, true);

        assertEquals(1, undoneItemsAtEnd.size());
        assertEquals(2, doneItemsAtEnd.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void modifyingNonExistentThrowsException() {
        Mtd mtd = new Mtd();

        mtd.modifyItemDoneState(new MtdItemRef(0, MtdItemRef.Type.Task), false, DayOfWeek.FRIDAY);
    }

    @Test
    public void modifyItemStateModifiesState() {
        Mtd mtd = new Mtd();

        mtd.addTodo("Todo", DayOfWeek.MONDAY);
        mtd.addTask("Task", new DayOfWeek[]{DayOfWeek.MONDAY});

        assertFalse(mtd.isItemDone(new MtdItemRef(0, MtdItemRef.Type.Todo), DayOfWeek.MONDAY));
        assertFalse(mtd.isItemDone(new MtdItemRef(0, MtdItemRef.Type.Task), DayOfWeek.MONDAY));

        mtd.modifyItemDoneState(new MtdItemRef(0, MtdItemRef.Type.Todo), true, DayOfWeek.MONDAY);
        mtd.modifyItemDoneState(new MtdItemRef(0, MtdItemRef.Type.Task), true, DayOfWeek.MONDAY);

        assertTrue(mtd.isItemDone(new MtdItemRef(0, MtdItemRef.Type.Todo), DayOfWeek.MONDAY));
        assertTrue(mtd.isItemDone(new MtdItemRef(0, MtdItemRef.Type.Task), DayOfWeek.MONDAY));
    }

    @Test
    public void mtdToFromJsonWorks() {
        String json = "{\"todos\":{\"items\":[{\"body\":\"Todo\",\"date\":\"2022-07-17\",\"id\":0,\"done\":null,\"sync_id\":2346981044091297941,\"state\":\"Unchanged\"}],\"server\":false},\"tasks\":{\"items\":[{\"body\":\"task\",\"weekdays\":[\"Sun\"],\"done_map\":{},\"id\":0,\"state\":\"Unchanged\",\"sync_id\":2130429732854137202}],\"server\":false},\"server\":false}";

        Mtd mtd = new Mtd(json);

        assertEquals("Todo", mtd.getItemBody(new MtdItemRef(0, MtdItemRef.Type.Todo)));
        assertEquals("task", mtd.getItemBody(new MtdItemRef(0, MtdItemRef.Type.Task)));

        assertEquals(json, mtd.toJson());
    }

    @Test
    public void mtdSyncFailsWithInvalidSocketAddress() {
        Mtd mtd = new Mtd();

        assertEquals("Cannot parse 'notaddress' to a socket address.", mtd.sync("Super secure password", "notaddress"));
    }

    @Test
    public void mtdSyncFailsWithoutServer() {
        Mtd mtd = new Mtd();

        assertEquals("An error occurred while syncing: IO failure: Connection refused (os error 111)", mtd.sync("Super secure password", "127.0.0.1:1234"));
    }
}