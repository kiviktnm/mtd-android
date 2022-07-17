package com.github.windore.mtd;

public class MtdItemRef {
    private final long id;
    private final Type type;
    MtdItemRef(long id, Type type) {
        this.id = id;
        this.type = type;
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

    public long getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        Todo,
        Task,
    }
}
