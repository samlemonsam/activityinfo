package org.activityinfo.model.util;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

/**
 * @author yuriyz on 07/03/2015.
 */
public final class Pair<S, T> {

    private final S m_first;
    private final T m_second;

    public Pair(S first, T second) {
        m_first = first;
        m_second = second;
    }

    public static <S, T> Pair<S, T> newPair(S first, T second) {
        return new Pair<>(first, second);
    }

    public S getFirst() {
        return m_first;
    }

    public T getSecond() {
        return m_second;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Pair<?, ?> other = (Pair<?, ?>) o;

        return !(m_first != null ? !m_first.equals(other.m_first) : other.m_first != null) && !(m_second != null ? !m_second.equals(other.m_second) : other.m_second != null);

    }

    public int hashCode() {
        int result;
        result = (m_first != null ? m_first.hashCode() : 0);
        result = 31 * result + (m_second != null ? m_second.hashCode() : 0);
        return result;
    }
}
