package com.github.windore.mtd;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

// Due to the fact that these methods test the underlying rust libraries, these test count as
// instrumented tests.
@RunWith(AndroidJUnit4.class)
public class MtdInstrumentedTest {

    @Test(expected = IllegalArgumentException.class)
    public void invalidJsonThrowsException() {
        new Mtd("invalid json").close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullJsonThrowsException() {
        new Mtd(null).close();
    }
}