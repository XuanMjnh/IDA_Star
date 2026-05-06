package com.example.idastar;

import java.util.Comparator;
import java.util.Vector;

public class IDAStar {
    private static final int FOUND = -1;
    private static final int STOPPED = -2;
    private static final int TIMEOUT = -3;
    private static final int INF = Integer.MAX_VALUE;
    private static final long TIME_LIMIT_MS = 60000;

    public Node startNode;
    public Node goalNode;
    public Node currentNode;
    public Vector<int[]> RESULT;
    protected int approvedNodes;
    protected int totalNodes;
    protected int iterations;
    protected int finalBound;
    protected double time;
    protected static boolean stop = false;
    protected String error;

    public IDAStar() {
        RESULT = new Vector<>();
    }

    public void solve() {
        RESULT.clear();
        error = null;
        approvedNodes = 0;
        totalNodes = 1;
        iterations = 0;
        finalBound = 0;

        long startTime = System.nanoTime();
        startNode.parent = null;
        startNode.g = 0;
        startNode.h = startNode.estimate(goalNode.state);
        startNode.f = startNode.h;

        int bound = startNode.f;
        while (true) {
            if (isTimedOut(startTime)) {
                timeout(startTime);
                return;
            }
            if (stop) {
                time = elapsedMs(startTime);
                return;
            }

            iterations++;
            finalBound = bound;
            int searchResult = search(startNode, bound, startTime);
            if (searchResult == FOUND) {
                time = elapsedMs(startTime);
                addResult(currentNode);
                return;
            }
            if (searchResult == TIMEOUT) {
                timeout(startTime);
                return;
            }
            if (searchResult == STOPPED) {
                time = elapsedMs(startTime);
                return;
            }
            if (searchResult == INF) {
                time = elapsedMs(startTime);
                error = "Không tìm được lời giải!";
                return;
            }
            bound = searchResult;
        }
    }

    private int search(Node node, int bound, long startTime) {
        if (isTimedOut(startTime)) {
            return TIMEOUT;
        }
        if (stop) {
            return STOPPED;
        }

        node.h = node.estimate(goalNode.state);
        node.f = node.g + node.h;
        if (node.f > bound) {
            return node.f;
        }
        if (node.equals(goalNode)) {
            currentNode = node;
            return FOUND;
        }

        approvedNodes++;
        Vector<Node> children = node.successors();
        children.sort(Comparator.comparingInt(child -> node.g + child.cost + child.estimate(goalNode.state)));

        int min = INF;
        for (Node child : children) {
            if (isInPath(child, node)) {
                continue;
            }
            child.parent = node;
            child.g = node.g + child.cost;
            child.h = child.estimate(goalNode.state);
            child.f = child.g + child.h;
            totalNodes++;

            int searchResult = search(child, bound, startTime);
            if (searchResult == FOUND || searchResult == TIMEOUT || searchResult == STOPPED) {
                return searchResult;
            }
            if (searchResult < min) {
                min = searchResult;
            }
        }
        return min;
    }

    private boolean isInPath(Node child, Node node) {
        Node cursor = node;
        while (cursor != null) {
            if (child.equals(cursor)) {
                return true;
            }
            cursor = cursor.parent;
        }
        return false;
    }

    private boolean isTimedOut(long startTime) {
        return elapsedMs(startTime) > TIME_LIMIT_MS;
    }

    private void timeout(long startTime) {
        error = "Thuật toán quá tốn thời gian!";
        approvedNodes = Integer.MAX_VALUE;
        time = elapsedMs(startTime);
    }

    private double elapsedMs(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000.0;
    }

    public void addResult(Node n) {
        if (n.parent != null) {
            addResult(n.parent);
        }
        RESULT.add(n.state.value);
    }
}
