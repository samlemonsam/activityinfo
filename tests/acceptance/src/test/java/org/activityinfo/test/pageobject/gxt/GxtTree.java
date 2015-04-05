package org.activityinfo.test.pageobject.gxt;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.XPathBuilder;
import org.activityinfo.test.pageobject.gxt.tree.CheckingVisitor;
import org.activityinfo.test.pageobject.gxt.tree.GxtTreeVisitor;
import org.activityinfo.test.pageobject.gxt.tree.NavigatingVisitor;
import org.activityinfo.test.pageobject.gxt.tree.SearchingVisitor;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withRole;

public class GxtTree {
    private FluentElement container;

    public GxtTree(FluentElement container) {
        this.container = container;
    }

    public void select(String... labels) {
        findNode(labels).select();
    }


    /**
     * Finds a specific node, following the given path
     * @param path the path of nodes from root node, to child, to child, etc.
     * @return the GxtNode
     */
    public GxtNode findNode(String... path) {
        NavigatingVisitor visitor = new NavigatingVisitor(Arrays.asList(path));
        accept(visitor);
        
        return visitor.get();
    }
    
    /**
     * Searches, breadth-first, through the tree for a node with the given label.
     *
     */
    public Optional<GxtNode> search(String label) {

        Optional<GxtNode> selected = findSelected();
        if(selected.isPresent() && selected.get().getLabel().equals(label)) {
            return selected;
        }

        SearchingVisitor visitor = SearchingVisitor.byLabel(label);
        accept(visitor);
        
        return visitor.getMatch();
    }

    public void accept(GxtTreeVisitor visitor) {
        Optional<GxtNode> node = firstRootNode();
        if(node.isPresent()) {
            node.get().select();
        }
        while(node.isPresent()) {
            GxtTreeVisitor.Action action = visitor.visit(node.get());
            if(action == GxtTreeVisitor.Action.ABORT) {
                break;
            }
            node = next(node.get());
        }
    }

    public void waitUntilLoaded() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        while(isEmpty()) {
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                throw new AssertionError("Interrupted while waiting for nodes to load...");
            }
            if(stopwatch.elapsed(TimeUnit.SECONDS) > 10) {
                throw new AssertionError("Timed out while waiting for nodes to load...");
            }
        }
    }
    
    /**
     * Advances to the next node in the tree using the Keyboard
     * so that we don't have problems with scrolling
     */
    private Optional<GxtNode> next(GxtNode node) {
        String currentId = node.getId();
        container.sendKeys(Keys.DOWN);
        Optional<GxtNode> selected = findSelected();
        if(!selected.isPresent()) {
            return Optional.absent();
        }
        
        if(selected.get().getId().equals(currentId)) {
            return Optional.absent();
        }
        
        return selected;
    }

    
    public Optional<GxtNode> findSelected() {
        Optional<FluentElement> element = container.find().div(withClass("x-ftree2-selected")).parent().div().firstIfPresent();
        if(element.isPresent()) {
            return Optional.of(new GxtNode(element.get()));
        } else {
            return Optional.absent();            
        }
    }

    public void setChecked(Iterable<String> labels) {
        CheckingVisitor visitor = new CheckingVisitor(labels);
        accept(visitor);
        visitor.validate();
    }


    private FluentIterable<GxtNode> findRootNodes() {
        return container.findElements(By.xpath("table/tbody/tr/td/div[@role = 'presentation']")).as(GxtNode.class);
    }
    
    private Optional<GxtNode> firstRootNode() {
        return container.findElements(By.xpath("table/tbody/tr/td/div[@role = 'presentation'][1]")).as(GxtNode.class).first();
    }
    
    public boolean isEmpty() {
        return findRootNodes().isEmpty();
    }


    private AssertionError assertionError(String message, Object... args) {
        return new AssertionError(String.format(message, args) + dumpTree());
    }
    
    private String dumpTree() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nTree:\n");
        dumpTree(sb, "", findRootNodes());
        return sb.toString();
    }

    private void dumpTree(StringBuilder sb, String indent, FluentIterable<GxtNode> nodes) {
        for(GxtNode node : nodes) {
            sb.append(node.getLabel()).append("\n");
            dumpTree(sb, indent + "  ",  node.children());
        }
    }


    public static class GxtNode {
        private FluentElement element;

        /**
         * Cached label of the node, for use in debugging.
         */
        private String debugLabel;

        public GxtNode(FluentElement element) {
            this.element = element;
        }
        
        private XPathBuilder treeItem() {
            return element.find().child().div(withRole("treeitem"));
        }
        
        private XPathBuilder joint() {
            return treeItem().descendants().img(withClass("x-tree3-node-joint"));
        }
        
        public boolean isLeaf() {
            Optional<FluentElement> joint = joint().firstIfPresent();
            if(!joint.isPresent()) {
                return false;
            }
            try {
                String style = joint.get().attribute("style");
                boolean leaf = !style.contains("background");
                System.out.println(getLabel() + ".leaf = " + leaf);
                return leaf;
            } catch(StaleElementReferenceException e) {
                return isLeaf();
            }
        }
        
        private FluentIterable<GxtNode> children() {
            return childContainer()
                    .child().div(withRole("presentation"))
                    .asList().as(GxtNode.class);
        }

        private XPathBuilder childContainer() {
            return element.find()
                    .child().div(withRole("group"));
        }
        
        public boolean isExpanded() {
            Optional<FluentElement> container = childContainer().firstIfPresent();
            return container.isPresent() && container.get().isDisplayed();
        }
        
        public String getLabel() {
            FluentElement treeItem = treeItem().first();
            debugLabel = treeItem.text();
            return treeItem.text();
        }

        public void ensureExpanded() {

            Stopwatch stopwatch = Stopwatch.createStarted();
            while(stopwatch.elapsed(TimeUnit.SECONDS) < 90) {
                if(isExpanded()) {
                    break;
                }
                if(isLeaf()) {
                    break;
                }
                tryExpand();

                int checksRemaining = 5;
                while(checksRemaining > 0) {
                    if(isExpanded() || isLeaf()) {
                        break;
                    }
                    sleep(150);
                    checksRemaining --;
                }
            }
        }

        private void sleep(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while waiting to expand node");
            }
        }

        private void tryExpand() {
            try {
                joint().first().click();
            } catch (WebDriverException ignore) {
                
            }
        }

        public GxtNode search(String label) {
            if(getLabel().equals(label)) {
                return this;
            } else if(!isLeaf()) {
                ensureExpanded();
                for (GxtNode child : children()) {
                    GxtNode match = child.search(label);
                    if(match != null) {
                        return match;
                    }
                }
            }
            return null;
        }

        
        private FluentElement checkbox() {
            return treeItem().img(withClass("x-tree3-node-check")).first();
        }
        
        private boolean isChecked(FluentElement checkbox) {
            // Because of the image spriting that GWT does, it's difficult to know which image is being displayed
            // It's unclear how stable the value below is
            return checkbox.attribute("style").contains("-670px");
        }

        public void setChecked(boolean checked) {
            FluentElement check = checkbox();
            if(isChecked(check) != checked) {
                check.click();
            }
        }

        public void select() {
            treeItem().span(withClass("x-tree3-node-text")).first().click();
        }

        @Override
        public String toString() {
            return "GxtNode{" + debugLabel + "}";
        }

        public String getId() {
            return element.attribute("id");
        }
    }

}
