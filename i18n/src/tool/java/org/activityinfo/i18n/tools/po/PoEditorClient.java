package org.activityinfo.i18n.tools.po;

import com.google.common.base.Charsets;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.activityinfo.i18n.tools.model.TranslationSet;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;

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
       // client.addFilter(new LoggingFilter());
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
    
    public Map<String, PoTerm> getTerms(int projectId) throws IOException {
        ObjectNode response = viewTerms(projectId, null);
        System.out.println(response);
        PoTerm[] terms = objectMapper.readValue(response.path("list"), PoTerm[].class);

        Map<String, PoTerm> map = new HashMap<>();
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
    }


    /**
     * Uploads a list of terms along with English translations.
     *  @param projectId the PoEditor project id
     * @param terms the list of terms to update
     * @param sync
     */
    public PoUploadResponse upload(int projectId, List<PoTermUpdate> terms, boolean sync) throws IOException {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field("api_token", token);
        form.field("action", "upload");
        form.field("id", Integer.toString(projectId));
        form.field("updating", "terms_definitions");
        form.field("language", "en");
        form.field("overwrite", "1");
        form.field("sync_terms", sync ? "1" : "0");
        form.bodyPart(createUpload(terms));

        String responseText = client.resource("https://poeditor.com/api/")
                .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                .post(String.class, form);
        
        PoUploadResponse response = objectMapper.readValue(responseText, PoUploadResponse.class);
        if(response.getResponse().getCode() != 200) {
            System.err.println(responseText);
            throw new RuntimeException("Failed: " + response.getResponse().getMessage());
        }
        return response;
    }

    private FormDataBodyPart createUpload(List<PoTermUpdate> jsonFile) throws IOException {

        byte[] jsonText = objectMapper.writeValueAsString(jsonFile).getBytes(Charsets.UTF_8);
        
        FormDataContentDisposition disposition = 
                FormDataContentDisposition.name("file")
                .fileName("translations.json")
                .creationDate(new Date())
                .modificationDate(new Date())
                .size(jsonText.length)
                .build();
        
        return new FormDataBodyPart(disposition, jsonText, MediaType.APPLICATION_JSON_TYPE);
    }

    public TranslationSet getTranslations(int projectId, String language) throws IOException {
        ObjectNode response = viewTerms(projectId, language);

        PoTermTranslation[] terms = objectMapper.readValue(response.path("list"), PoTermTranslation[].class);

        return new TranslationSet(language, Arrays.asList(terms));
    }

}
