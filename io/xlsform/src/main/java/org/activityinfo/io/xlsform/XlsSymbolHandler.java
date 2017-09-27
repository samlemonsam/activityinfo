package org.activityinfo.io.xlsform;

import com.google.common.base.Preconditions;
import org.activityinfo.io.xform.xpath.XSymbolException;
import org.activityinfo.io.xform.xpath.XSymbolHandler;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XlsSymbolHandler implements XSymbolHandler {

    private Map<String,String> symbolMap;

    public XlsSymbolHandler(List<FormField> fields) {
        symbolMap = new HashMap<>();
        for (FormField field : fields) {
            if (field.getType() instanceof EnumType) {
                addEnumItems((EnumType) field.getType());
            }
            symbolMap.put(field.getId().asString(), fieldRef(field.getCode()));
        }
    }

    private void addEnumItems(EnumType enumType) {
        for(EnumItem item : enumType.getValues()) {
            symbolMap.put(item.getId().asString(), quote(item.getId().asString()));
        }
    }

    private String fieldRef(String field) {
        return "${" + field + "}";
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
