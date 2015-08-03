package org.activityinfo.i18n.tools;

import com.google.common.collect.Lists;
import org.activityinfo.i18n.tools.model.ResourceClassTerm;
import org.activityinfo.i18n.tools.model.Term;
import org.activityinfo.i18n.tools.model.TranslationSet;
import org.activityinfo.i18n.tools.model.TranslationSource;
import org.activityinfo.i18n.tools.po.PoEditorClient;
import org.activityinfo.i18n.tools.po.PoTerm;
import org.activityinfo.i18n.tools.po.PoTermTranslation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Defines the source of the translations as a PoEditor.com project
 */
public class PoEditorSource implements TranslationSource {
    private int projectId;
    private String apiToken;

    public int getProjectId() {
        return projectId;
    }

    public String getApiToken() {
        return apiToken;
    }

    @Override
    public Map<String, Term> fetchTerms() throws IOException {
        PoEditorClient client = new PoEditorClient(apiToken);
        return client.getTerms(projectId);
    }

    @Override
    public TranslationSet fetchTranslations(String language) throws IOException {
        PoEditorClient client = new PoEditorClient(apiToken);
        return client.getTranslations(projectId, language);
    }

    @Override
    public void addTerms(List<ResourceClassTerm> terms) {
        List<PoTerm> newTerms = Lists.newArrayList();
        for(ResourceClassTerm term : terms) {
            PoTermTranslation newTerm = new PoTermTranslation(term);
            newTerms.add(newTerm);
        }
        
        PoEditorClient client = new PoEditorClient(apiToken);
        client.addTerms(projectId, newTerms);
    }

    @Override
    public String toString() {
        return "PoEditor.com[projectId=" + projectId + "]";
    }
}
