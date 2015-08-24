package org.activityinfo.ui.rebind;

import com.google.common.collect.Iterables;
import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Generates per-permutation manifests
 */
@LinkerOrder(LinkerOrder.Order.POST)
public class AppCacheLinker extends AbstractLinker {
    @Override
    public String getDescription() {
        return "AppCache Manifest Emitter";
    }


    @Override
    public ArtifactSet link(TreeLogger logger, LinkerContext linkerContext, ArtifactSet artifacts) throws UnableToCompleteException {

        for (Artifact<?> artifact : artifacts) {
            logger.log(TreeLogger.Type.ERROR, artifact.toString());
        }
        
        return super.link(logger, linkerContext, artifacts);
    }
}
