package org.activityinfo.legacy.shared;
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

import java.io.Serializable;

/**
 * @author yuriyz on 1/27/14.
 */
public class Pair<A, B> implements Serializable {

    /**
     * A object
     */
    private A m_a;

    /**
     * B object
     */
    private B m_b;

    /**
     * Constructor
     */
    public Pair() {
    }

    /**
     * Constructor
     *
     * @param p_a a object
     * @param p_b b object
     */
    public Pair(A p_a, B p_b) {
        m_a = p_a;
        m_b = p_b;
    }

    /**
     * Gets A object.
     *
     * @return A object
     */
    public A getA() {
        return m_a;
    }

    /**
     * Gets B object.
     *
     * @return B object
     */
    public B getB() {
        return m_b;
    }

    /**
     * Sets A object.
     *
     * @param p_a A object
     */
    public void setA(A p_a) {
        m_a = p_a;
    }

    /**
     * Sets B object.
     *
     * @param p_b B object
     */
    public void setB(B p_b) {
        m_b = p_b;
    }

    /**
     * Equals implementation.
     *
     * @param o object to compare
     * @return whether objects are equal
     */
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> other = (Pair<?, ?>) o;
        return m_a.equals(other.m_a) && m_b.equals(other.m_b);
    }

    /**
     * Hash code function.
     *
     * @return hash code function
     */
    public int hashCode() {
        return m_a.hashCode() * 13 + m_b.hashCode() * 7;
    }
}