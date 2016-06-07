
package org.activityinfo.store.query.impl.join;

/**
 * This utility heapsorts two arrays in tandem.
 *
 * @author Adriano Caloiaro
 * @date 12/6/14
 */
final class HeapsortTandem {

    /**
     * Sorts two arrays descending in tandem using heapsort.
     * <p>
     * a1's values are the values of interest, while a2 is sorted in tandem. This is useful when a2 is an identity array.
     *
     * @param a1 The array to be sorted
     * @param a2 The array to sort in tandem
     * @param n  The size of the arrays ( should be equal )
     */
    public static void heapsortAscending(int[] a1, double[] a2, int n) {
        int end = n - 1;
        heapify(a1, a2, n);

        while (end > 0) {
            swap(a1, end, 0);
            end--;

            // Restore the heap property in the range 0:end
            siftDown(a1, a2, 0, end);
        }
    }


    /**
     * Heapifies an array with the largest value at the root
     */
    private static void heapify(int[] a1, double[] a2, int count) {
        // Choose the last parent node in the array as the start position
        int start = (int) Math.floor((count - 2) / 2);

        while (start >= 0) {
            siftDown(a1, a2, start, count - 1);
            start--;
        }
    }

    /**
     * Orders the nodes between startNode and endNode in descending heap order
     */
    private static void siftDown(int[] a1, double[] a2, int startNode, int endNode) {
        int root = startNode;
        int swapNode, leftChild, rightChild;

        // While the root node has at least a single child
        leftChild = root * 2 + 1;
        while (leftChild <= endNode) {
            leftChild = root * 2 + 1;
            rightChild = leftChild + 1;
            swapNode = root;

            // If the left child is great, choose it
            if (a1[swapNode] < a1[leftChild]) {
                swapNode = leftChild;
            }

            // If the right child exists and is greater than the left child, choose it
            if (rightChild <= endNode && a1[swapNode] < a1[rightChild]) {
                swapNode = rightChild;
            }

            // We're done if we've chosen to swap the root
            if (swapNode == root) {
                return;
            }// Else do the swap since we're not done
            else {
                swap(a1, root, swapNode);
                swap(a2, root, swapNode);
                root = swapNode;
                leftChild = root * 2 + 1;
            }
        }
    }


    /**
     * Swaps the values of a double array
     */
    private static void swap(double[] array, int i, int j) {
        double dtmp = array[i];
        array[i] = array[j];
        array[j] = dtmp;
    }

    /**
     * Swaps the values of an integer array
     */
    private static void swap(int[] array, int i, int j) {
        int itmp = array[i];
        array[i] = array[j];
        array[j] = itmp;
    }

}