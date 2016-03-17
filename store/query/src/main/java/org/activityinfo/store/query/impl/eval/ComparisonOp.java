package org.activityinfo.store.query.impl.eval;

/**
 * Boolean-valued functions that operate on column views
 */
public enum ComparisonOp {

    EQUALS {
        @Override
        public boolean apply(int sign) {
            return sign == 0;
        }
    },
    NOT_EQUALS {
        @Override
        public boolean apply(int sign) {
            return sign != 0;
        }
    },
    LESS_THAN {
        @Override
        public boolean apply(int sign) {
            return sign < 0;
        }
    },
    GREATER_THAN {
        @Override
        public boolean apply(int sign) {
            return sign > 0;
        }
    },
    LESS_THAN_EQUAL_TO {
        @Override
        public boolean apply(int sign) {
            return sign <= 0;
        }
    },
    GREATER_THAN_EQUAL_TO {
        @Override
        public boolean apply(int sign) {
            return sign >= 0;
        }
    };


    public abstract boolean apply(int sign);


}
