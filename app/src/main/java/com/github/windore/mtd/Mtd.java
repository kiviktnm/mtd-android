package com.github.windore.mtd;

import androidx.annotation.Nullable;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.stream.Stream;

public class Mtd extends Observable {

    static {
        System.loadLibrary("mtda");
    }

    private final long tdListPtr;

    public Mtd() {
        tdListPtr = newTdList();
    }

    public Mtd(String json) throws IllegalArgumentException {
        tdListPtr = newTdListFromJson(json);
    }

    private static native long newTdList();

    private static native long newTdListFromJson(String json);

    private native String toJson(long tdListPtr);

    private native long destroyTdList(long tdListPtr);

    private native long[] getItemsForWeekday(long tdListPtr, byte weekdayNum, short itemTypeNum, boolean are_done);

    private native String getItemBody(long tdListPtr, short itemTypeNum, long id);

    private native void addTodo(long tdListPtr, String body, byte weekdayNum);

    private native void addTask(long tdListPtr, String body, byte[] weekdayNums);

    private native int removeItem(long tdListPtr, short itemTypeNum, long id);

    private native int modifyItemDoneState(long tdListPtr, short itemTypeNum, long id, boolean isDone, byte weekdayNum);

    public String toJson() {
        return toJson(tdListPtr);
    }

    public List<MtdItemRef> getItemsForWeekday(MtdItemRef.Type itemType, DayOfWeek weekday, boolean are_done) {
        ArrayList<MtdItemRef> list = new ArrayList<>();
        for (long id : getItemsForWeekday(tdListPtr, (byte) weekday.getValue(), MtdItemRef.typeToNum(itemType), are_done)) {
            list.add(new MtdItemRef(id, itemType));
        }
        return list;
    }

    @Nullable
    public String getItemBody(MtdItemRef item) {
        return getItemBody(item.getType(), item.getId());
    }

    @Nullable
    public String getItemBody(MtdItemRef.Type itemType, long itemId) {
        return getItemBody(tdListPtr, MtdItemRef.typeToNum(itemType), itemId);
    }

    public void addTodo(String body, DayOfWeek weekday) {
        addTodo(tdListPtr, body, (byte) weekday.getValue());

        setChanged();
        notifyObservers();
    }

    public void addTask(String body, DayOfWeek[] weekdays) {
        Byte[] weekdayNumsB = Stream.of(weekdays).map(wd -> (byte) wd.getValue()).toArray(Byte[]::new);
        byte[] weekdayNums = new byte[weekdayNumsB.length];
        for (int i = 0; i < weekdayNumsB.length; i++) {
            weekdayNums[i] = weekdayNumsB[i];
        }
        addTask(tdListPtr, body, weekdayNums);

        setChanged();
        notifyObservers();
    }

    public void removeItem(MtdItemRef item) {
        if (removeItem(tdListPtr, MtdItemRef.typeToNum(item.getType()), item.getId()) != 0) {
            throw new IllegalArgumentException("No such item exists.");
        }

        setChanged();
        notifyObservers();
    }

    public void modifyItemDoneState(MtdItemRef item, boolean isDone, DayOfWeek weekdayWhenDone) {
        if (modifyItemDoneState(tdListPtr, MtdItemRef.typeToNum(item.getType()), item.getId(), isDone, (byte) weekdayWhenDone.getValue()) != 0) {
            throw new IllegalArgumentException("No such item exists.");
        }

        setChanged();
        notifyObservers();
    }

    @Override
    protected void finalize() {
        destroyTdList(tdListPtr);
    }
}
