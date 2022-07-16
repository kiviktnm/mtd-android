package com.github.windore.mtd;

import androidx.annotation.Nullable;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Mtd implements AutoCloseable {

    private final long tdListPtr;

    static {
        System.loadLibrary("mtda");
    }

    private static native long newTdList();
    private static native long newTdListFromJson(String json);
    private native long destroyTdList(long tdListPtr);

    private native long[] getItemsForWeekday(long tdListPtr, byte weekdayNum, short itemTypeNum, boolean are_done);
    private native String getItemBody(long tdListPtr, short itemTypeNum, long id);
    private native void addTodo(long tdListPtr, String body, byte weekdayNum);
    private native void addTask(long tdListPtr, String body, byte[] weekdayNums);
    private native int removeItem(long tdListPtr, short itemTypeNum, long id);
    private native int modifyItemDoneState(long tdListPtr, short itemTypeNum, long id, boolean isDone, byte weekdayNum);

    public Mtd() {
        tdListPtr = newTdList();
    }

    public Mtd(String json) throws IllegalArgumentException {
        tdListPtr = newTdListFromJson(json);
    }

    public List<MtdItem> getItemsForWeekday(MtdItem.Type itemType, DayOfWeek weekday, boolean are_done) {
        ArrayList<MtdItem> list = new ArrayList<>();
        for (long id : getItemsForWeekday(tdListPtr, (byte)weekday.getValue(), MtdItem.typeToNum(itemType), are_done)) {
            list.add(new MtdItem(id, itemType));
        }
        return list;
    }

    @Nullable
    public String getItemBody(MtdItem item) {
        return getItemBody(item.getType(), item.getId());
    }

    @Nullable
    public String getItemBody(MtdItem.Type itemType, long itemId) {
        return getItemBody(tdListPtr, MtdItem.typeToNum(itemType), itemId);
    }

    public void addTodo(String body, DayOfWeek weekday) {
        addTodo(tdListPtr, body, (byte)weekday.getValue());
    }

    public void addTask(String body, DayOfWeek[] weekdays) {
        Byte[] weekdayNumsB = Stream.of(weekdays).map(wd -> (byte)wd.getValue()).toArray(Byte[]::new);
        byte[] weekdayNums = new byte[weekdayNumsB.length];
        for (int i = 0; i < weekdayNumsB.length; i++) {
            weekdayNums[i] = weekdayNumsB[i];
        }
        addTask(tdListPtr, body, weekdayNums);
    }

    public void removeItem(MtdItem item) {
        if (removeItem(tdListPtr, MtdItem.typeToNum(item.getType()), item.getId()) != 0) {
            throw new IllegalArgumentException("No such item exists.");
        }
    }

    public void modifyItemDoneState(MtdItem item, boolean isDone, DayOfWeek weekdayWhenDone) {
        if (modifyItemDoneState(tdListPtr, MtdItem.typeToNum(item.getType()), item.getId(), isDone, (byte)weekdayWhenDone.getValue()) != 0) {
            throw new IllegalArgumentException("No such item exists.");
        }
    }

    @Override
    public void close() {
        destroyTdList(tdListPtr);
    }
}
