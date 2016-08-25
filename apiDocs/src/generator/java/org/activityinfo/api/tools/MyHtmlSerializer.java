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

