package org.activityinfo.test.capacity.scenario.coordination;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.activityinfo.test.capacity.action.CompositeAction;
import org.activityinfo.test.capacity.action.SyncOfflineWithApi;
import org.activityinfo.test.capacity.action.UserAction;
import org.activityinfo.test.capacity.model.UserRole;


public class ReportingUser implements UserRole {
    
    private final PartnerOrganization organization;
    private final String nickname;

    public ReportingUser(PartnerOrganization organization, String nickname) {
        this.organization = organization;
        this.nickname = nickname;
    }

    @Override
    public String getNickName() {
        return nickname;
    }

    @Override
    public Optional<UserAction> getTask(int dayNumber) {
        if(dayNumber > 1) {
            return Optional.of(SyncOfflineWithApi.INSTANCE);
        } else {
            return Optional.absent();
        }
    }

}
