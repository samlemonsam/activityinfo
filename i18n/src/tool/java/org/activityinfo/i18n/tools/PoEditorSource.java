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
package org.activityinfo.i18n.tools;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.i18n.tools.model.ResourceClassTerm;
import org.activityinfo.i18n.tools.model.TranslationSet;
import org.activityinfo.i18n.tools.model.TranslationSource;
import org.activityinfo.i18n.tools.po.PoEditorClient;
import org.activityinfo.i18n.tools.po.PoTerm;
import org.activityinfo.i18n.tools.po.PoTermUpdate;
import org.activityinfo.i18n.tools.po.PoUploadResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines the source of the translations as a PoEditor.com project
 */
public class PoEditorSource implements TranslationSource {
    private int projectId;
    private String apiToken;

    public PoEditorSource(int projectId, String apiToken) {
        this.projectId = projectId;
        this.apiToken = apiToken;
        if(Strings.isNullOrEmpty(this.apiToken)) {
            throw new IllegalArgumentException("API Token is missing.");
        }
    }

    public int getProjectId() {
        return projectId;
    }

    public String getApiToken() {
        return apiToken;
    }

    @Override
    public Map<String, PoTerm> fetchTerms() throws IOException {
        PoEditorClient client = new PoEditorClient(apiToken);
        return client.getTerms(projectId);
    }

    @Override
    public TranslationSet fetchTranslations(String language) throws IOException {
        PoEditorClient client = new PoEditorClient(apiToken);
        return client.getTranslations(projectId, language);
    }


    /**
     * Updates the translation source, adding any missing terms and their default
     * translations.
     * 
     * @param terms
     */
    public void updateTerms(List<ResourceClassTerm> terms, boolean sync) throws IOException {
        
        List<PoTermUpdate> updates = Lists.newArrayList();
        for (ResourceClassTerm term : terms) {
            updates.add(new PoTermUpdate(term.getKey(), term.getDefaultTranslation()));
        }

        PoEditorClient client = new PoEditorClient(apiToken);
        PoUploadResponse response = client.upload(projectId, updates, sync);

        PoUploadResponse.Details details = response.getDetails();
        System.out.println(String.format("Terms:       %5d  Added: %5d  Deleted: %d",
                details.getTerms().getParsed(),
                details.getTerms().getAdded(),
                details.getTerms().getDeleted()));

        System.out.println(String.format("Definitions: %5d  Added: %5d  Updated: %d",
                details.getDefinitions().getParsed(),
                details.getDefinitions().getAdded(),
                details.getDefinitions().getUpdated()));
    }

    @Override
    public String toString() {
        return "PoEditor.com[projectId=" + projectId + "]";
    }


    public void dumpNewTerms(List<ResourceClassTerm> terms) throws IOException {
        Set<String> existingTerms = fetchTerms().keySet();
        for (ResourceClassTerm term : terms) {
            if(!existingTerms.contains(term.getKey())) {
                System.out.println("New term: " + term.getKey());
            }
        }
    }
}
