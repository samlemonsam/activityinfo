package org.activityinfo.store.hrd.columns;

import org.junit.Test;

public class Base62Test {

    @Test
    public void maxLength() {
        String longest = null;

        long i = Long.MAX_VALUE;
        while(true) {
            String encoded = Base62.encode(i);
            if(longest == null || encoded.length() > longest.length()) {
                longest = encoded;
                System.out.println(i + " => " + encoded + " (" + encoded.length() + ")");
            }
            if(i == Long.MAX_VALUE) {
                break;
            }
            i++;
        }

        System.out.println("LONGEST = " + longest + " (" + longest.length() + ")");
    }


}