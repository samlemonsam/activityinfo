package org.activityinfo.i18n.tools.po;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.representation.Form;
import org.activityinfo.i18n.tools.model.Term;
import org.activityinfo.i18n.tools.model.TranslationSet;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Restful client for poeditor.com
 * 
 * @see  <a href="https://poeditor.com/api_reference/">API Reference</a>
 */
public class PoEditorClient {

    private final Client client;
    private String token;
    private ObjectMapper objectMapper;

    /**
     *  
     * @param token PoEditor.com API Token
     */
    public PoEditorClient(String token) {
        this.token = token;
        this.objectMapper = new ObjectMapper();
        
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        
        client = Client.create(clientConfig);
    }

    private ObjectNode viewTerms(int projectId, String language) {

        Form form = new Form();
        if(language != null) {
            form.putSingle("language", language);
        }
        form.putSingle("id", Integer.toString(projectId));
        form.putSingle("action", "view_terms");
        form.putSingle("api_token", token);

        return client.resource("https://poeditor.com/api/").post(ObjectNode.class, form);
    }
    
    public Map<String, Term> getTerms(int projectId) throws IOException {
        ObjectNode response = viewTerms(projectId, null);
        PoTerm[] terms = objectMapper.readValue(response.path("list"), PoTerm[].class);

        Map<String, Term> map = new HashMap<>();
        for(PoTerm term : terms) {
            map.put(term.getKey(), term);
        }
        return map;
    }
    
    public void addTerms(int projectId, List<PoTerm> terms) {
        String data;
        try {
            data = objectMapper.writeValueAsString(terms);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        System.out.println(data);
        
        Form form = new Form();
        form.putSingle("id", Integer.toString(projectId));
        form.putSingle("action", "add_terms");
        form.putSingle("api_token", token);
        form.putSingle("data", data);
        
        String response = client.resource("https://poeditor.com/api/").post(String.class, form);
        
        System.out.println(response);

    }

    public TranslationSet getTranslations(int projectId, String language) throws IOException {
        ObjectNode response = viewTerms(projectId, language);

        PoTermTranslation[] terms = objectMapper.readValue(response.path("list"), PoTermTranslation[].class);

        return new TranslationSet(language, Arrays.asList(terms));
    }
}
