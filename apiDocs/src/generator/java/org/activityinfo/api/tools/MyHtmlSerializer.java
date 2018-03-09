/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.api.tools;

import org.pegdown.LinkRenderer;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.TextNode;

public class MyHtmlSerializer extends ToHtmlSerializer {

    public MyHtmlSerializer() {
        super(new LinkRenderer());
    }

    @Override
    public void visit(HeaderNode node) {
        String id = computeId(node);
        String tag = "h" + node.getLevel();
        printer.print('<').print(tag);
        printer.print(" id=\"").print(id).print("\">");
        visitChildren(node);
        printer.print('<').print('/').print(tag).print('>');
    }
    
    private String computeId(HeaderNode node) {
        StringBuilder sb = new StringBuilder();
        collectText(sb, node);
        
        String headerText = sb.toString();
        String simpleText = headerText.replaceAll("[^A-Za-z0-9- ]", "");
        return simpleText.toLowerCase().replace(' ', '-');
    }
    
    private void collectText(StringBuilder sb, Node node) {
        if(node instanceof TextNode) {
            sb.append(((TextNode) node).getText());
        } else {
            for (Node child : node.getChildren()) {
                collectText(sb, child);
            }
        }
    }
}

