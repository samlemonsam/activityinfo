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
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.Activity;
import org.activityinfo.server.database.hibernate.entity.HasFormClassJson;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

public class GetFormClassHandler implements CommandHandler<GetFormClass> {

    private Provider<EntityManager> entityManager;
    private DispatcherSync dispatcherSync;

    @Inject
    public GetFormClassHandler(Provider<EntityManager> entityManager, DispatcherSync dispatcherSync) {
        this.entityManager = entityManager;
        this.dispatcherSync = dispatcherSync;
    }

    @Override
    public CommandResult execute(GetFormClass cmd, User user) throws CommandException {
        String json = fetchJson(cmd, user);
        return new FormClassResult(json);
    }

    private String fetchJson(GetFormClass cmd, User user) {
        char domain = ResourceId.valueOf(cmd.getResourceId()).getDomain();
        if (domain == CuidAdapter.ACTIVITY_DOMAIN) {
            Activity activity = entityManager.get().find(Activity.class, CuidAdapter.getLegacyIdFromCuid(cmd.getResourceId()));
            String json = readJson(activity);
            if (json == null) {
                return constructFromLegacy(activity.getId());
            }
            return json;
        } else {
            org.activityinfo.server.database.hibernate.entity.FormClass hibernateFormClass =
                    entityManager.get().find(org.activityinfo.server.database.hibernate.entity.FormClass.class, cmd.getResourceId());
            return readJson(hibernateFormClass);
        }
    }

    private String readJson(HasFormClassJson hasFormClassJson) {
        if (hasFormClassJson.getGzFormClass() != null) {
            try (Reader reader = new InputStreamReader(
                    new GZIPInputStream(
                            new ByteArrayInputStream(hasFormClassJson.getGzFormClass())), Charsets.UTF_8)) {

                return CharStreams.toString(reader);

            } catch (IOException e) {
                throw new UnexpectedCommandException(e);
            }

        } else if (hasFormClassJson.getFormClass() != null) {
            return hasFormClassJson.getFormClass();

        } else {
            return null;
        }
    }


    private String constructFromLegacy(final int activityId) {
        ActivityFormDTO activityDTO = dispatcherSync.execute(new GetActivityForm(activityId));
        ActivityFormClassBuilder builder = new ActivityFormClassBuilder(activityDTO);
        FormClass formClass = builder.build();
        return Resources.toJson(formClass.asResource());
    }
}
