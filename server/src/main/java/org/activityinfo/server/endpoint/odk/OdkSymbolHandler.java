package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Preconditions;
import org.activityinfo.io.xform.xpath.XSymbolException;
import org.activityinfo.io.xform.xpath.XSymbolHandler;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OdkSymbolHandler implements XSymbolHandler {

    private final Map<String, String> symbolMap = new HashMap<>();

    public OdkSymbolHandler(List<OdkField> fields) {
        for(OdkField field : fields) {
            symbolMap.put(field.getModel().getId().asString(), field.getAbsoluteFieldName());
            if(field.getModel().getType() instanceof EnumType) {
                addEnumItems((EnumType) field.getModel().getType());
            }
        }
    }

    private void addEnumItems(EnumType enumType) {
        for (EnumItem item : enumType.getValues()) {
            symbolMap.put(item.getId().asString(), quote(item.getId().asString()));
        }
    }

    private String quote(String value) {
        return "'" + value + "'";
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

}
