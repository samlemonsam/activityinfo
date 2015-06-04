package org.activityinfo.geoadmin.match;


import java.util.Arrays;

/* Copyright (c) 2012 Kevin L. Stern
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/

/**
* An implementation of the Hungarian algorithm for solving the assignment
* problem. An instance of the assignment problem consists of a number of
* targets along with a number of sources and a cost matrix which gives the cost of
* assigning the i'th target to the j'th source at position (i, j). The goal is to
* find an assignment of targets to sources so that no source is assigned more than
* one target and so that no target is assigned to more than one source in such a
* manner so as to minimize the total cost of completing the sources.
* <p>
*
* An assignment for a cost matrix that has more targets than sources will
* necessarily include unassigned targets, indicated by an assignment value of
* -1; in no other circumstance will there be unassigned targets. Similarly, an
* assignment for a cost matrix that has more sources than targets will necessarily
* include unassigned sources; in no other circumstance will there be unassigned
* sources. For completeness, an assignment for a square cost matrix will give
* exactly one unique target to each source.
* <p>
*
* This version of the Hungarian algorithm runs in time O(n^3), where n is the
* maximum among the number of targets and the number of sources.
*
* @author Kevin L. Stern
*/
public class HungarianAlgorithm {
    private final double[][] costMatrix;
    private final int rows, cols, dim;
    
    // target = target
    // source = source
    private final double[] labelByTarget, labelBySource;
    private final int[] minSlackTargetBySource;
    private final double[] minSlackValueBySource;
    private final int[] matchSourceByTarget, matchTargetBySource;
    private final int[] parentTargetByCommittedSource;
    private final boolean[] committedTargets;



    public static double[][] toArray(ScoreMatrix matrix) {
        double[][] array = new double[matrix.getRowCount()][matrix.getColumnCount()];
        for(int i=0;i<matrix.getRowCount();++i) {
            for(int j=0;j<matrix.getColumnCount();++j) {
                array[i][j] = matrix.distance(i, j);
            }
        }
        return array;
    }

    public HungarianAlgorithm(ScoreMatrix matrix) {
        this(toArray(matrix));
    }


    /**
     * Construct an instance of the algorithm.
     *
     * @param costMatrix
     *          the cost matrix, where matrix[i][j] holds the cost of assigning
     *          target i to source j, for all i, j. The cost matrix must not be
     *          irregular in the sense that all rows must be the same length.
     */
    public HungarianAlgorithm(double[][] costMatrix) {
        this.dim = Math.max(costMatrix.length, costMatrix[0].length);
        this.rows = costMatrix.length;
        this.cols = costMatrix[0].length;
        this.costMatrix = new double[this.dim][this.dim];
        for (int w = 0; w < this.dim; w++) {
            if (w < costMatrix.length) {
                if (costMatrix[w].length != this.cols) {
                    throw new IllegalArgumentException("Irregular cost matrix");
                }
                this.costMatrix[w] = Arrays.copyOf(costMatrix[w], this.dim);
            } else {
                this.costMatrix[w] = new double[this.dim];
            }
        }
        labelByTarget = new double[this.dim];
        labelBySource = new double[this.dim];
        minSlackTargetBySource = new int[this.dim];
        minSlackValueBySource = new double[this.dim];
        committedTargets = new boolean[this.dim];
        parentTargetByCommittedSource = new int[this.dim];
        matchSourceByTarget = new int[this.dim];
        Arrays.fill(matchSourceByTarget, -1);
        matchTargetBySource = new int[this.dim];
        Arrays.fill(matchTargetBySource, -1);
    }

    /**
     * Compute an initial feasible solution by assigning zero labels to the
     * targets and by assigning to each source a label equal to the minimum cost
     * among its incident edges.
     */
    protected void computeInitialFeasibleSolution() {
        for (int j = 0; j < dim; j++) {
            labelBySource[j] = Double.POSITIVE_INFINITY;
        }
        for (int w = 0; w < dim; w++) {
            for (int j = 0; j < dim; j++) {
                if (costMatrix[w][j] < labelBySource[j]) {
                    labelBySource[j] = costMatrix[w][j];
                }
            }
        }
    }

    /**
     * Execute the algorithm.
     *
     * @return the minimum cost matching of targets to sources based upon the
     *         provided cost matrix. A matching value of -1 indicates that the
     *         corresponding target is unassigned.
     */
    public int[] execute() {
    /*
     * Heuristics to improve performance: Reduce rows and columns by their
     * smallest element, compute an initial non-zero dual feasible solution and
     * create a greedy matching from targets to sources of the cost matrix.
     */
        reduce();
        computeInitialFeasibleSolution();
        greedyMatch();

        int w = fetchUnmatchedTarget();
        while (w < dim) {
            initializePhase(w);
            executePhase();
            w = fetchUnmatchedTarget();
        }
        int[] result = Arrays.copyOf(matchSourceByTarget, rows);
        for (w = 0; w < result.length; w++) {
            if (result[w] >= cols) {
                result[w] = -1;
            }
        }
        return result;
    }

    /**
     * Execute a single phase of the algorithm. A phase of the Hungarian algorithm
     * consists of building a set of committed targets and a set of committed sources
     * from a root unmatched target by following alternating unmatched/matched
     * zero-slack edges. If an unmatched source is encountered, then an augmenting
     * path has been found and the matching is grown. If the connected zero-slack
     * edges have been exhausted, the labels of committed targets are increased by
     * the minimum slack among committed targets and non-committed sources to create
     * more zero-slack edges (the labels of committed sources are simultaneously
     * decreased by the same amount in order to maintain a feasible labeling).
     * <p>
     *
     * The runtime of a single phase of the algorithm is O(n^2), where n is the
     * dimension of the internal square cost matrix, since each edge is visited at
     * most once and since increasing the labeling is accomplished in time O(n) by
     * maintaining the minimum slack values among non-committed sources. When a phase
     * completes, the matching will have increased in size.
     */
    protected void executePhase() {
        while (true) {
            int minSlackTarget = -1, minSlackSource = -1;
            double minSlackValue = Double.POSITIVE_INFINITY;
            for (int j = 0; j < dim; j++) {
                if (parentTargetByCommittedSource[j] == -1) {
                    if (minSlackValueBySource[j] < minSlackValue) {
                        minSlackValue = minSlackValueBySource[j];
                        minSlackTarget = minSlackTargetBySource[j];
                        minSlackSource = j;
                    }
                }
            }
            if (minSlackValue > 0) {
                updateLabeling(minSlackValue);
            }
            parentTargetByCommittedSource[minSlackSource] = minSlackTarget;
            if (matchTargetBySource[minSlackSource] == -1) {
        /*
         * An augmenting path has been found.
         */
                int committedSource = minSlackSource;
                int parentTarget = parentTargetByCommittedSource[committedSource];
                while (true) {
                    int temp = matchSourceByTarget[parentTarget];
                    match(parentTarget, committedSource);
                    committedSource = temp;
                    if (committedSource == -1) {
                        break;
                    }
                    parentTarget = parentTargetByCommittedSource[committedSource];
                }
                return;
            } else {
        /*
         * Update slack values since we increased the size of the committed
         * targets set.
         */
                int target = matchTargetBySource[minSlackSource];
                committedTargets[target] = true;
                for (int j = 0; j < dim; j++) {
                    if (parentTargetByCommittedSource[j] == -1) {
                        double slack = costMatrix[target][j] - labelByTarget[target]
                                - labelBySource[j];
                        if (minSlackValueBySource[j] > slack) {
                            minSlackValueBySource[j] = slack;
                            minSlackTargetBySource[j] = target;
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @return the first unmatched target or {@link #dim} if none.
     */
    protected int fetchUnmatchedTarget() {
        int t;
        for (t = 0; t < dim; t++) {
            if (matchSourceByTarget[t] == -1) {
                break;
            }
        }
        return t;
    }

    /**
     * Find a valid matching by greedily selecting among zero-cost matchings. This
     * is a heuristic to jump-start the augmentation algorithm.
     */
    protected void greedyMatch() {
        for (int t = 0; t < dim; t++) {
            for (int j = 0; j < dim; j++) {
                if (matchSourceByTarget[t] == -1 && matchTargetBySource[j] == -1
                        && costMatrix[t][j] - labelByTarget[t] - labelBySource[j] == 0) {
                    match(t, j);
                }
            }
        }
    }

    /**
     * Initialize the next phase of the algorithm by clearing the committed
     * targets and source sets and by initializing the slack arrays to the values
     * corresponding to the specified root target.
     *
     * @param t
     *          the target at which to root the next phase.
     */
    protected void initializePhase(int t) {
        Arrays.fill(committedTargets, false);
        Arrays.fill(parentTargetByCommittedSource, -1);
        committedTargets[t] = true;
        for (int j = 0; j < dim; j++) {
            minSlackValueBySource[j] = costMatrix[t][j] - labelByTarget[t]
                    - labelBySource[j];
            minSlackTargetBySource[j] = t;
        }
    }

    /**
     * Helper method to record a matching between target t and source j.
     */
    protected void match(int t, int j) {
        matchSourceByTarget[t] = j;
        matchTargetBySource[j] = t;
    }

    /**
     * Reduce the cost matrix by subtracting the smallest element of each row from
     * all elements of the row as well as the smallest element of each column from
     * all elements of the column. Note that an optimal assignment for a reduced
     * cost matrix is optimal for the original cost matrix.
     */
    protected void reduce() {
        for (int t = 0; t < dim; t++) {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < dim; j++) {
                if (costMatrix[t][j] < min) {
                    min = costMatrix[t][j];
                }
            }
            for (int j = 0; j < dim; j++) {
                costMatrix[t][j] -= min;
            }
        }
        double[] min = new double[dim];
        for (int j = 0; j < dim; j++) {
            min[j] = Double.POSITIVE_INFINITY;
        }
        for (int t = 0; t < dim; t++) {
            for (int j = 0; j < dim; j++) {
                if (costMatrix[t][j] < min[j]) {
                    min[j] = costMatrix[t][j];
                }
            }
        }
        for (int t = 0; t < dim; t++) {
            for (int j = 0; j < dim; j++) {
                costMatrix[t][j] -= min[j];
            }
        }
    }

    /**
     * Update labels with the specified slack by adding the slack value for
     * committed targets and by subtracting the slack value for committed sources. In
     * addition, update the minimum slack values appropriately.
     */
    protected void updateLabeling(double slack) {
        for (int t = 0; t < dim; t++) {
            if (committedTargets[t]) {
                labelByTarget[t] += slack;
            }
        }
        for (int j = 0; j < dim; j++) {
            if (parentTargetByCommittedSource[j] != -1) {
                labelBySource[j] -= slack;
            } else {
                minSlackValueBySource[j] -= slack;
            }
        }
    }
}