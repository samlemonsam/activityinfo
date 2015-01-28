package cucumber.runtime.junit;

import java.io.Serializable;

/**
* Created by alex on 28-1-15.
*/
class UniqueId implements Serializable {
    private Serializable profileId;
    private Serializable executionUnit;

    public UniqueId(Serializable profileId, Serializable executionUnit) {
        this.profileId = profileId;
        this.executionUnit = executionUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UniqueId uniqueId = (UniqueId) o;

        if (!executionUnit.equals(uniqueId.executionUnit)) return false;
        if (!profileId.equals(uniqueId.profileId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = profileId.hashCode();
        result = 31 * result + executionUnit.hashCode();
        return result;
    }
}
