package org.activityinfo.io.xls;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class SheetNamerTest {

    private static final String DATABASE_NAME = "1. LCRP-R Protection";
    
    private static final String[] ACTIVITY_NAMES = new String[] {
            "PRT-1.1.1. Presence on border (Quarterly)",
            "PRT-1.1.2. Direct interven on border",
            "PRT-1.1.3. Train border official (Quarterly)",
            "PRT-1.1.4. Border Inst. Support (Quarterly)",
            "PRT-1.1.5. Advocacy interventions (Quarterly)",
            "PRT-1.2.1. Legal counselling (Access to Doc.)",
            "PRT-1.2.2. Legal counseling (Other than Doc.)",
            "PRT-1.2.3. Legal Representation",
            "PRT-1.2.4. Legal awareness /information",
            "PRT-1.2.5. Tech support on Doc (Quarterly)",
            "PRT-1.3.1. Assess legal issues (Quarterly)",
            "PRT-1.3.2. C.Build legal actors (Quarterly)",
            "PRT-1.3.3. Mediation",
            "PRT-1.3.4. Strategic litigation (Quarterly)",
            "PRT-2.1.1. Cmty. awareness/outreach(services)",
            "PRT-2.1.2. Activities(Cmty Dev. Centres)",
            "PRT-2.1.3. Support MoSA SDC (Quarterly)",
            "PRT-2.2.1. Train Cmty-based protection (Cada)",
            "PRT-2.2.1. Train Cmty-based protection (Site)",
            "PRT-2.2.2. Est. Cmty self-mgnt Struc(Country)",
            "PRT-2.2.2. Est. Cmty self-mgnt Struc(Cada)",
            "PRT-2.2.2. Est. Cmty self-mgnt Struc(Sites)",
            "PRT-2.2.3. Cmty self-mgnt Struc identify risk",
            "PRT-3.1.1. PRM Refer Respond(excl. Detention)",
            "PRT-3.1.2. Interventions in detention centres",
            "PRT-3.1.3. C.Build on PRT issues (Quarterly)",
            "PRT-3.1.4. Advocacy on PRT issues (Quarterly)",
            "PRT-3.1.5. Mine Action",
            "PRT-3.1.6. Inst support Law enfor (Quarterly)",
            "PRT-3.2.1. PWSN Case Mgnt(excl. Child &SGBV)",
            "PRT-3.2.2. Specialized services for PWD",
            "PRT-3.2.3. Emergency cash assistance for PWSN",
            "PRT-3.2.4. Train on ID,Refer,Case (Quarterly)",
            "PRT-3.3.1. Registration of refugees",
            "PRT-3.3.2. Verification/Renewal of refugees",
            "PRT-3.3.3. Transport to registration centres",
            "PRT-3.3.4. Recording for PRS",
            "PRT-3.3.5. Profiling of Lebanese Returnees",
            "PRT-3.4.1. RST Interview/assessment/counsel",
            "PRT-3.4.2. Preparing for RST/HAP departures",
            "PRT-3.4.3. Training on RST/HAP"
    };
    
    @Test
    public void test() {
        
        SheetNamer namer = new SheetNamer();
        Set<String> uniqueNames = new HashSet<>();

        for (String activityName : ACTIVITY_NAMES) {
            String uniqueName = namer.name(DATABASE_NAME + " - " + activityName);
            System.out.println(uniqueName);
            if(uniqueNames.contains(uniqueName)) {
                throw new AssertionError("Duplicate name produced: " + uniqueName);
            }
            uniqueNames.add(uniqueName);
        }
        
    }
    
}