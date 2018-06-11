package org.activityinfo.store.migrate;

import org.junit.Test;

import java.util.Date;

public class SnapshotExporterTest {

    @Test
    public void test() {
        System.out.println(SnapshotExporter.TIME_FORMAT.format(new Date()));
    }
}