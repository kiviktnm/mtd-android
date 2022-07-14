package com.github.windore.mtd;

public class Mtd implements AutoCloseable {

    private final long tdListPtr;

    static {
        System.loadLibrary("mtda");
    }

    private static native long newTdList();
    private static native long newTdListFromJson(String json);
    private static native long destroyTdList(long tdListPtr);

    public Mtd() {
        tdListPtr = newTdList();
    }

    public Mtd(String json) throws IllegalArgumentException {
        tdListPtr = newTdListFromJson(json);
    }

    @Override
    public void close() {
        // Memory must be freed
        destroyTdList(tdListPtr);
    }
}
