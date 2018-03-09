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
package org.activityinfo.i18n.tools.po;

/**
 * Response from upload
 */
public class PoUploadResponse {
    
    public static class Counts {
        private int parsed;
        private int added;
        private int deleted;
        private int updated;

        public int getParsed() {
            return parsed;
        }

        public int getAdded() {
            return added;
        }

        public int getDeleted() {
            return deleted;
        }

        public int getUpdated() {
            return updated;
        }
    }
    
    public static class Details {
        private Counts terms = new Counts();
        private Counts definitions = new Counts();

        public Counts getTerms() {
            return terms;
        }

        public Counts getDefinitions() {
            return definitions;
        }
    }
 
    private PoResponse response = new PoResponse();
    private Details details = new Details();

    public PoResponse getResponse() {
        return response;
    }

    public Details getDetails() {
        return details;
    }
}
