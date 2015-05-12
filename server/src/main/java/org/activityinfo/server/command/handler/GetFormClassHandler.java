package org.activityinfo.server.command.handler;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.adapter.ActivityFormClassBuilder;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetFormClass;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.FormClassResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.UnexpectedCommandException;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.Activity;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class GetFormClassHandler implements CommandHandler<GetFormClass> {

    private PermissionOracle permissionOracle;
    private Provider<EntityManager> entityManager;
    private DispatcherSync dispatcherSync;

    @Inject
    public GetFormClassHandler(PermissionOracle permissionOracle, Provider<EntityManager> entityManager, DispatcherSync dispatcherSync) {
        this.permissionOracle = permissionOracle;
        this.entityManager = entityManager;
        this.dispatcherSync = dispatcherSync;
    }

    @Override
    public CommandResult execute(GetFormClass cmd, User user) throws CommandException {

        Activity activity = entityManager.get().find(Activity.class, CuidAdapter.getLegacyIdFromCuid(cmd.getResourceId()));

        String json = readJson(activity);
        return new FormClassResult(json);
    }

    private String readJson(Activity activity)  {
        if(activity.getGzFormClass() != null) {
            try(Reader reader = new InputStreamReader(
                                    new GZIPInputStream(
                                        new ByteArrayInputStream(activity.getGzFormClass())), Charsets.UTF_8)) {

                return CharStreams.toString(reader);

            } catch (IOException e) {
                throw new UnexpectedCommandException(e);
            }

        } else if(activity.getFormClass() != null) {
            return activity.getFormClass();

        } else {
            return constructFromLegacy(activity.getId());
        }
    }


    private String constructFromLegacy(final int activityId) {
        ActivityFormDTO activityDTO = dispatcherSync.execute(new GetActivityForm(activityId));
        ActivityFormClassBuilder builder = new ActivityFormClassBuilder(activityDTO);
        FormClass formClass = builder.build();
        fixIfNeeded(formClass, activityDTO);
        return Resources.toJson(formClass.asResource());
    }

    // AI-1057 - Fix forms corrupted during cloning, partner and project range must reference to db id instead of partner id.
    private void fixIfNeeded(FormClass formClass, ActivityFormDTO activityDTO) {
        for (FormField formField : formClass.getFields()) {
            if (formField.getType() instanceof ReferenceType) {
                ReferenceType sourceType = (ReferenceType) formField.getType();

                Set<ResourceId> sourceRange = sourceType.getRange();
                ReferenceType targetType = new ReferenceType()
                        .setCardinality(sourceType.getCardinality());

                switch (sourceRange.iterator().next().getDomain()) {
                    case CuidAdapter.PARTNER_FORM_CLASS_DOMAIN:
                        targetType.setRange(CuidAdapter.partnerFormClass(activityDTO.getDatabaseId()));
                        formField.setType(targetType);
                        break;
                    case CuidAdapter.PROJECT_CLASS_DOMAIN:
                        targetType.setRange(CuidAdapter.projectFormClass(activityDTO.getDatabaseId()));
                        formField.setType(targetType);
                        break;
                }

            }
        }
    }
}
