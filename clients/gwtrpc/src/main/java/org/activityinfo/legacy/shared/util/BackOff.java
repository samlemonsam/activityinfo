/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.legacy.shared.util;
/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.IOException;

/**
 * Back-off policy when retrying an operation.
 *
 * @author Ravi Mistry
 * @since 1.15
 */
public interface BackOff {

    /**
     * Indicates that no more retries should be made for use in {@link #nextBackOffMillis()}.
     */
    static final long STOP = -1L;

    /**
     * Reset to initial state.
     */
    void reset() throws IOException;

    /**
     * Gets the number of milliseconds to wait before retrying the operation or {@link #STOP} to
     * indicate that no retries should be made.
     * <p/>
     * <p>
     * Example usage:
     * </p>
     * <p/>
     * <pre>
     * long backOffMillis = backoff.nextBackOffMillis();
     * if (backOffMillis == Backoff.STOP) {
     * // do not retry operation
     * } else {
     * // sleep for backOffMillis milliseconds and retry operation
     * }
     * </pre>
     */
    long nextBackOffMillis();

    /**
     * Fixed back-off policy whose back-off time is always zero, meaning that the operation is retried
     * immediately without waiting.
     */
    BackOff ZERO_BACKOFF = new BackOff() {

        @Override
        public void reset() throws IOException {
        }

        @Override
        public long nextBackOffMillis() {
            return 0;
        }
    };

    /**
     * Fixed back-off policy that always returns {@code #STOP} for {@link #nextBackOffMillis()},
     * meaning that the operation should not be retried.
     */
    BackOff STOP_BACKOFF = new BackOff() {

        @Override
        public void reset() throws IOException {
        }

        @Override
        public long nextBackOffMillis() {
            return STOP;
        }
    };
}
