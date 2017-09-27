package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Preconditions;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.io.xform.xpath.XSymbolException;
import org.activityinfo.io.xform.xpath.XSymbolHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestSymbolHandler implements XSymbolHandler {

    private Map<String,String> symbolMap = new HashMap<>();

    public TestSymbolHandler(List<ResourceId> fieldIds) {
        Iterator<ResourceId> it = fieldIds.listIterator();
        while (it.hasNext()) {
            ResourceId id = it.next();
            Character domain = id.getDomain();
            if (domain.equals(CuidAdapter.ATTRIBUTE_DOMAIN)) {
                symbolMap.put(id.asString(), quote(id.asString()));
            } else {
                symbolMap.put(id.asString(), id.asString());
            }
        }
    }

    @Override
    public String resolveSymbol(String symbol) throws XSymbolException {
        Preconditions.checkArgument(symbol != null, "Symbol cannot be null.");
        String resolved = symbolMap.get(symbol);
        if (resolved == null) {
            throw new XSymbolException(symbol);
        }
        return resolved;
    }

    private String quote(String string) {
        return "'" + string + "'";
    }

}
