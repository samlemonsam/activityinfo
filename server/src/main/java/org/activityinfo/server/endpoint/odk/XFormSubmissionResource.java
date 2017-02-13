package org.activityinfo.server.endpoint.odk;

import com.google.api.client.util.Maps;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.CreateLocation;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.endpoint.odk.xform.XFormInstance;
import org.activityinfo.server.endpoint.odk.xform.XFormInstanceImpl;
import org.activityinfo.service.blob.BlobId;
import org.activityinfo.service.blob.GcsBlobFieldStorageService;
import org.activityinfo.service.lookup.ReferenceChoice;
import org.w3c.dom.Element;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static javax.ws.rs.core.Response.Status.*;
import static org.activityinfo.model.legacy.CuidAdapter.*;
import static org.activityinfo.server.endpoint.odk.OdkFieldValueParserFactory.fromFieldType;
import static org.activityinfo.server.endpoint.odk.OdkHelper.isLocation;

@Path("/submission")
public class XFormSubmissionResource {

    private static final Logger LOGGER = Logger.getLogger(XFormSubmissionResource.class.getName());

    final private DispatcherSync dispatcher;
    final private ResourceLocatorSync locator;
    final private AuthenticationTokenService authenticationTokenService;
    final private GcsBlobFieldStorageService blobFieldStorageService;
    final private InstanceIdService instanceIdService;
    final private SubmissionArchiver submissionArchiver;

    @Inject
    public XFormSubmissionResource(DispatcherSync dispatcher,
                                   ResourceLocatorSync locator,
                                   AuthenticationTokenService authenticationTokenService,
                                   GcsBlobFieldStorageService blobFieldStorageService,
                                   InstanceIdService instanceIdService,
                                   SubmissionArchiver submissionArchiver) {
        this.dispatcher = dispatcher;
        this.locator = locator;
        this.authenticationTokenService = authenticationTokenService;
        this.blobFieldStorageService = blobFieldStorageService;
        this.instanceIdService = instanceIdService;
        this.submissionArchiver = submissionArchiver;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_XML)
    public Response submit(byte bytes[]) {

        XFormInstance instance = new XFormInstanceImpl(bytes);
        AuthenticatedUser user = authenticationTokenService.authenticate(instance.getAuthenticationToken());
        FormClass formClass = locator.getFormClass(instance.getFormClassId());


        ResourceId formId = newLegacyFormInstanceId(formClass.getId());
        FormInstance formInstance = new FormInstance(formId, formClass.getId());
        String instanceId = instance.getId();

        LOGGER.log(Level.INFO, "Saving XForm " + instance.getId() + " as " + formId);

        for (FormField formField : formClass.getFields()) {
            Optional<Element> element = instance.getFieldContent(formField.getId());
            if (element.isPresent()) {
                formInstance.set(formField.getId(), tryParse(formInstance, formField, element.get()));
            } else if (isLocation(formClass, formField)) {
                FieldType fieldType = formField.getType();
                Optional<Element> gpsField = instance.getFieldContent(field(formClass.getId(), GPS_FIELD));
                Optional<Element> nameField = instance.getFieldContent(field(formClass.getId(), LOCATION_NAME_FIELD));

                if (fieldType instanceof ReferenceType && gpsField.isPresent() && nameField.isPresent()) {

                    ResourceId locationFieldId = field(formClass.getId(), LOCATION_FIELD);
                    int newLocationId = new KeyGenerator().generateInt();
                    ReferenceType locationRefType = (ReferenceType) fieldType;
                    if(locationRefType.getRange().isEmpty()) {
                        throw new IllegalStateException("Location field has empty range");
                    }
                    ResourceId locationFormId = locationRefType.getRange().iterator().next();
                    int locationTypeId = getLegacyIdFromCuid(locationFormId);
                    FieldValue fieldValue = new ReferenceValue(new RecordRef(locationFormId, locationInstanceId(newLocationId)));
                    String name = OdkHelper.extractText(nameField.get());

                    if (Strings.isNullOrEmpty(name)) {
                        throw new WebApplicationException(
                                Response.status(BAD_REQUEST).
                                        entity("Name value for location field is blank. ").
                                        build());
                    }

                    Optional<GeoPoint> geoPoint = parseLocation(gpsField);

                    formInstance.set(locationFieldId, fieldValue);
                    createLocation(newLocationId, locationTypeId, name, geoPoint);
                }
            }
        }
    
        ensurePartnerIsSet(formClass, formInstance);

        if (!instanceIdService.exists(instanceId)) {
            for (FieldValue fieldValue : formInstance.getFieldValueMap().values()) {
                if (fieldValue instanceof AttachmentValue) {
                    persist(user, instance, (AttachmentValue) fieldValue);
                }
            }

            locator.persist(formInstance);
            instanceIdService.submit(instanceId);
        }

        // Backup the original XForm in case something went wrong with processing
        submissionArchiver.backup(formClass.getId(), formId, ByteSource.wrap(bytes));

        return Response.status(CREATED).build();
    }

    private void ensurePartnerIsSet(FormClass formClass, FormInstance formInstance) {

        ResourceId partnerFieldId = CuidAdapter.field(formClass.getId(), CuidAdapter.PARTNER_FIELD);
        if(formInstance.get(partnerFieldId) != null) {
            return;
        }
        
        // Otherwise, find the default partner
        FormField partnerField = formClass.getField(partnerFieldId);
        ReferenceType partnerFieldType = (ReferenceType) partnerField.getType();

        List<ReferenceChoice> choices = locator.getReferenceChoices(partnerFieldType.getRange());
        if(choices.size() != 1) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
            .entity("No partner selected").build());
        }

        formInstance.set(partnerFieldId, new ReferenceValue(choices.get(0).getRef()));
    }

    private FieldValue tryParse(FormInstance formInstance, FormField formField, Element element) {
        try {
            OdkFieldValueParser odkFieldValueParser = fromFieldType(formField.getType());
            return odkFieldValueParser.parse(element);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse value for field " + formField.getId() +
                     " in form " + formInstance.getFormId() +
                     " from xml: " + element, e);
        }
        return null;
    }

    private Optional<GeoPoint> parseLocation(Optional<Element> element) {
        Preconditions.checkNotNull(element);
        if (element.isPresent()) {
            try {
                OdkFieldValueParser odkFieldValueParser = fromFieldType(GeoPointType.INSTANCE);
                return fromNullable((GeoPoint) odkFieldValueParser.parse(element.get()));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Can't parse form submission location data", e);
            }
        }

        return absent();
    }

    private void persist(AuthenticatedUser user, XFormInstance instance, AttachmentValue fieldValue) {
        Attachment attachment = fieldValue.getValues().get(0);
        if (attachment.getFilename() != null) {
            try {
                BodyPart bodyPart = ((XFormInstanceImpl) instance).findBodyPartByFilename(attachment.getFilename());

                String mimeType = bodyPart.getContentType();
                attachment.setMimeType(mimeType);

                blobFieldStorageService.put(user, bodyPart.getDisposition(), mimeType,
                        new BlobId(attachment.getBlobId()), instance.getFormClassId(),
                        bodyPart.getInputStream());

            } catch (MessagingException messagingException) {
                LOGGER.log(Level.SEVERE, "Unable to parse input", messagingException);
                throw new WebApplicationException(Response.status(BAD_REQUEST).build());
            } catch (IOException ioException) {
                LOGGER.log(Level.SEVERE, "Could not write attachment to GCS", ioException);
                throw new WebApplicationException(Response.status(SERVICE_UNAVAILABLE).build());
            }
        }
    }

    private VoidResult createLocation(int id, int locationTypeId, String name, Optional<GeoPoint> geoPoint) {
        Preconditions.checkNotNull(name, geoPoint);
        Map<String, Object> properties = Maps.newHashMap();

        properties.put("id", id);
        properties.put("locationTypeId", locationTypeId);
        properties.put("name", name);

        if (geoPoint.isPresent()) {
            properties.put("latitude", geoPoint.get().getLatitude());
            properties.put("longitude", geoPoint.get().getLongitude());
        }

        return dispatcher.execute(new CreateLocation(properties));
    }
}
