package org.activityinfo.store.query.shared.plan;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gwt.core.shared.GwtIncompatible;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QueryPlan {
    private final ProjectionNode rootNode;
    private final List<PlanNode> nodes = new ArrayList<>();

    public QueryPlan(ProjectionNode rootNode) {
        this.rootNode = rootNode;
        findNodes(rootNode);
    }

    private void findNodes(PlanNode parent) {
        nodes.add(parent);
        for (PlanNode input : parent.getInputs()) {
            findNodes(input);
        }
    }

    public List<PlanNode> getNodes() {
        return nodes;
    }


    /**
     * @return a dot file that can be used to plot with graphviz
     */
    public String toDot() {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");
        for (PlanNode node : nodes) {
            dot.append(node.getDebugId()).append(" [label=\"").append(node.getDebugLabel()).append("\"];");
        }
        for (PlanNode node : nodes) {
            for (PlanNode input : node.getInputs()) {
                dot.append(node.getDebugId()).append(" -> ").append(input.getDebugId()).append(";");
            }
        }
        dot.append("}");
        return dot.toString();
    }

    @GwtIncompatible
    @SuppressWarnings("NonJREEmulationClassesInClientCode")
    public void dumpGraph() throws IOException {
        File file = File.createTempFile("queryplan", ".dot");
        Files.asCharSink(file, Charsets.UTF_8).write(toDot());

        System.out.println("Query plan dumped to " + file.getAbsolutePath());
        System.out.println("dot -Tsvg " + file.getAbsolutePath() + " > /tmp/plan.svg");

    }
}
