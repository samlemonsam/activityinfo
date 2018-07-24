package org.activityinfo.store.testing;

import org.junit.Test;

public class AddressGeneratorTest {

    @Test
    public void test() {
        AddressGenerator generator = new AddressGenerator();
        System.out.println(generator.get());
        System.out.println(generator.get());

    }

}