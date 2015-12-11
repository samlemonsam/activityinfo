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
