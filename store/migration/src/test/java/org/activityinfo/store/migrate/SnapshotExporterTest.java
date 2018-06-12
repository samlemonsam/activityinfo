package org.activityinfo.store.migrate;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class SnapshotExporterTest {

    @Test
    public void test() {
        long time = System.currentTimeMillis();
        for (int i = 0; i < 365; i++) {
            System.out.println(SnapshotExporter.TIME_FORMAT.print(time));
            time += TimeUnit.DAYS.toMillis(1);
        }
    }
}