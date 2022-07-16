package com.github.windore.mtd;

public class MtdItem {
    public enum Type {
        Todo,
        Task,
    }

    private final long id;
    private final Type type;

    MtdItem(long id, Type type) {
        this.id = id;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public static short typeToNum(Type type) {
        switch (type) {
            case Todo:
                return 1;
            case Task:
                return 2;
            default:
                return -1;
        }
    }
}
