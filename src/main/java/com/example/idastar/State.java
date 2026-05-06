package com.example.idastar;

import java.util.*;

public class State {
    public static final int HEURISTIC_COUNT = 6;
    public static int heuristic;
    public static int goal;
    public int[] value;
    private final int size;
    private final int length;
    private int blank;
    // Truyền vào kích thước của puzzle
    public State(int m) {
        this.size = m;
        this.length = size * size;
        this.value= new int[length];
        this.blank = 0;
    }
    // Truyền vào trạng thái và kích thước của puzzle
    public State(int[] v, int size) {
        this.value = v;
        this.size = size;
        this.length = size * size;
        this.blank = posBlank(this.value);
    }
    public void Init() {
        if (goal == 1) {
            for (int i = 0; i < length; i++) {
                value[i] = i;
            }
        } else {
            for (int i = 0; i < length - 1; i++) {
                value[i] = i + 1;
            }
            value[length - 1] = 0;
        }
    }
    // Tạo trang thái đích
    public int[] createGoalArray() {
        Init();
        return value;
    }
    // Tạo trạng thái ramdom
    public int[] createRandomArray() {
        Init();
        Random rand = new Random();
        int t = 20 * size;
        int count = 0;
        int a = 1, b;
        do {
            switch (a) {
                case 1 -> UP();
                case 2 -> RIGHT();
                case 3 -> DOWN();
                case 4 -> LEFT();
            }
            count++;
            while (true) {
                b = rand.nextInt(4) + 1;
                if (Math.abs(b - a) != 2) {
                    a = b;
                    break;
                }
            }
        } while (count != t);
        return value;
    }
    // Tìm vị trí trống
    public int posBlank(int[] val) {
        int pos = 0;
        for (int i = 0; i < val.length; i++) {
            if (val[i] == 0) {
                pos = i;
                break;
            }
        }
        return pos;
    }
    // Kiểm tra trạng thái có phải trạng thái đích không
    public boolean isGoal(State goalState) {
        int[] goalValue = goalState.value;
        boolean flag = true;
        for (int i = 0; i < length; i++) {
            if (value[i] != goalValue[i]) {
                flag = false;
                break;
            }
        }
        return flag;
    }
    // Tính ước lượng h(x)
    public int estimate(State goalState) {
        return switch (heuristic) {
            case 1 -> heuristic1(goalState);
            case 2 -> heuristic2(goalState);
            case 3 -> heuristic3(goalState);
            case 4 -> heuristic4(goalState);
            case 5 -> heuristic5(goalState);
            case 6 -> heuristic6(goalState);
            default -> heuristic2(goalState);
        };
    }
    public static String heuristicName(int heuristic) {
        return switch (heuristic) {
            case 1 -> "H1 - Số ô sai vị trí";
            case 2 -> "H2 - Manhattan";
            case 3 -> "H3 - Euclid";
            case 4 -> "H4 - Sai hàng/cột";
            case 5 -> "H5 - Manhattan + xung đột tuyến tính";
            case 6 -> "H6 - H5 + ô chặn đích";
            default -> "Không xác định";
        };
    }
    private int[] goalPositions(State goalState) {
        int[] positions = new int[length];
        int[] goalValue = goalState.value;
        for (int i = 0; i < length; i++) {
            positions[goalValue[i]] = i;
        }
        return positions;
    }
    // Heuristic 1 - Tổng số ô sai vị trí
    public int heuristic1(State goalState) {
        int[] goalValue = goalState.value;
        int distance = 0;
        for (int i = 0; i < length; i++) {
            if(value[i] != 0 && value[i] != goalValue[i]) distance++;
        }
        return distance;
    }
    // Heuristic 2 - Tổng khoảng cách để đưa các ô về đúng vị trí
    public int heuristic2(State goalState) {
        int[] goalPositions = goalPositions(goalState);
        int distance = 0;
        for (int i = 0; i < length; i++) {
            if (value[i] != 0) {
                int gi = goalPositions[value[i]];
                distance += Math.abs(gi / size - i / size) + Math.abs(gi % size - i % size);
            }
        }
        return distance;
    }
    // Heuristic 3 - Tổng khoảng cách Euclid của các ô với vị trí đích
    public int heuristic3(State goalState) {
        int[] goalPositions = goalPositions(goalState);
        int distance = 0;
        for (int i = 0; i < length; i++) {
            if (value[i] != 0) {
                int gi = goalPositions[value[i]];
                int width = Math.abs(gi % size - i % size);
                int height = Math.abs(gi / size - i / size);
                distance += (int) Math.sqrt(width * width + height * height);
            }
        }
        return distance;
    }
    // Heuristic 4 - Tổng số ô sai hàng và số ô sai cột
    public int heuristic4(State goalState) {
        int[] goalPositions = goalPositions(goalState);
        int distance = 0;
        for (int i = 0; i < length; i++) {
            if (value[i] != 0) {
                int gi = goalPositions[value[i]];
                if ((gi / size) != (i / size)) distance++;
                if ((gi % size) != (i % size)) distance++;
            }
        }
        return distance;
    }
    // Heuristic 5 - Tổng khoảng cách để đưa các ô về đúng vị trí + số ô xung đột tuyến tính
    public int heuristic5(State goalState) {
        int[] goalPositions = goalPositions(goalState);
        return heuristic2(goalState) + linearConflict(goalPositions);
    }

    private int linearConflict(int[] goalPositions) {
        int distance = 0;
        for (int row = 0; row < size; row++) {
            ArrayList<Integer> goalColumns = new ArrayList<>();
            for (int col = 0; col < size; col++) {
                int tile = value[row * size + col];
                if (tile != 0) {
                    int gi = goalPositions[tile];
                    if (gi / size == row) {
                        goalColumns.add(gi % size);
                    }
                }
            }
            distance += linearConflictPenalty(goalColumns);
        }
        for (int col = 0; col < size; col++) {
            ArrayList<Integer> goalRows = new ArrayList<>();
            for (int row = 0; row < size; row++) {
                int tile = value[row * size + col];
                if (tile != 0) {
                    int gi = goalPositions[tile];
                    if (gi % size == col) {
                        goalRows.add(gi / size);
                    }
                }
            }
            distance += linearConflictPenalty(goalRows);
        }
        return distance;
    }

    private int linearConflictPenalty(ArrayList<Integer> goalOrders) {
        if (goalOrders.size() < 2) {
            return 0;
        }
        int[] tails = new int[goalOrders.size()];
        int lisLength = 0;
        for (int order : goalOrders) {
            int left = 0;
            int right = lisLength;
            while (left < right) {
                int mid = (left + right) / 2;
                if (tails[mid] < order) {
                    left = mid + 1;
                } else {
                    right = mid;
                }
            }
            tails[left] = order;
            if (left == lisLength) {
                lisLength++;
            }
        }
        return (goalOrders.size() - lisLength) * 2;
    }

    // Heuristic 6 - Heuristic 5 + số ô không thể về đích
    public int heuristic6(State goalState) {
        int[] goalValue = goalState.value;
        int[] goalPositions = goalPositions(goalState);
        int distance = 0;
        distance += heuristic5(goalState);
        for (int i = 0; i < length; i++) {
            if (value[i] != 0) {
                int c = value[i];
                int gi = goalPositions[c];
                int block = 0; // Số ô vuông đúng vị trí xung quanh
                int count = 0; // Số ô vuông xung quanh
                if (i != gi) {
                    if (gi / size != 0) {
                        count++;
                        block += value[gi - size] == goalValue[gi - size] ? 1 : 0;
                    }
                    if (gi / size != size - 1) {
                        count++;
                        block += value[gi + size] == goalValue[gi + size] ? 1 : 0;
                    }
                    if (gi  % size != 0) {
                        count++;
                        block += value[gi - 1] == goalValue[gi - 1] ? 1 : 0;
                    }
                    if (gi % size != size - 1) {
                        count++;
                        block += value[gi + 1] == goalValue[gi + 1] ? 1 : 0;
                    }
                }
                if (count >= 2 && count == block) distance++;
            }
        }
        return distance;
    }
    // Vector các trạng thái con
    public Vector<State> successors() {
        Vector<State> states = new Vector<>();
        int blank = posBlank(value);
        if (blank / size > 0) {
            addSuccessor(blank, blank - size, states, value);
        }
        if (blank / size < size - 1) {
            addSuccessor(blank, blank + size, states, value);
        }
        if (blank % size > 0) {
            addSuccessor(blank, blank - 1, states, value);
        }
        if (blank % size < size - 1) {
            addSuccessor(blank, blank + 1, states, value);
        }
        return states;
    }
    // Add trạng thái con
    public void addSuccessor(int oldBlank, int newBlank, Vector<State> states, int[] oldVal) {
        int[] newVal = oldVal.clone();
        newVal[oldBlank] = newVal[newBlank];
        newVal[newBlank] = 0;
        states.add(new State(newVal, size));
    }
    // Di chuyển ô trống lên trên
    public void UP() {
        blank = posBlank(value);
        int temp;
        if(blank >= size) {
            temp = value[blank];
            value[blank] = value[blank - size];
            value[blank - size] = temp;
            blank -= size;
        }
    }
    // Di chuyển ô trống sang phải
    public void RIGHT() {
        blank = posBlank(value);
        int temp;
        if(blank % size != size-1) {
            temp = value[blank];
            value[blank] = value[blank + 1];
            value[blank + 1] = temp;
            blank += 1;
        }
    }
    // Di chuyển ô trống xuống dưới
    public void DOWN() {
        blank = posBlank(value);
        int temp;
        if(blank < length - size) {
            temp = value[blank];
            value[blank] = value[blank + size];
            value[blank + size] = temp;
            blank += size;
        }
    }
    // Di chuyển ô blank sang trái
    public void LEFT() {
        blank = posBlank(value);
        int temp;
        if(blank % size != 0 ) {
            temp = value[blank];
            value[blank] = value[blank - 1];
            value[blank - 1] = temp;
            blank -= 1;
        }
    }
}
