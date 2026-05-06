package com.example.idastar;

import java.util.Locale;

public class Result {
    public final String heuristic;
    public final String description;
    public final double approved;
    public final double total;
    public final double time;
    public final double step;
    public final double iterations;
    public final double bound;
    public final String error;

    public Result(String heuristic, int approved, int total, int step, double time, String error) {
        this(heuristic, "", approved, total, step, time, error, 0, 0);
    }

    public Result(String heuristic, String description, double approved, double total, double step, double time,
                  String error, double iterations, double bound) {
        this.heuristic = heuristic;
        this.description = description;
        this.approved = approved;
        this.total = total;
        this.step = step;
        this.time = time;
        this.error = error;
        this.iterations = iterations;
        this.bound = bound;
    }

    public boolean isSolved() {
        return error == null;
    }

    public String getHeuristic() {
        return heuristic;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return isSolved() ? "Đạt" : "Lỗi";
    }

    public String getApprovedText() {
        return formatNumber(approved);
    }

    public String getTotalText() {
        return formatNumber(total);
    }

    public String getStepText() {
        return isSolved() ? formatMetric(step) : "-";
    }

    public String getTimeText() {
        return formatTime(time);
    }

    public String getIterationsText() {
        return formatMetric(iterations);
    }

    public String getBoundText() {
        return formatMetric(bound);
    }

    public String getEfficiencyText() {
        if (!isSolved() || step <= 0) {
            return "-";
        }
        return String.format(Locale.US, "%.1f", (double) approved / step);
    }

    public String showResult() {
        String rs = "Thuật toán sử dụng Heuristic: " + this.heuristic + "\n";
        if (this.error == null) {
            rs += "Số node đã duyệt: " + formatMetric(this.approved) + "\n"
                    + "Tổng số node đã sinh: " + formatMetric(this.total) + "\n"
                    + "Số bước đi đến đích: " + formatMetric(this.step) + "\n"
                    + "Số vòng lặp IDA*: " + formatMetric(this.iterations) + "\n"
                    + "Ngưỡng cuối: " + formatMetric(this.bound) + "\n"
                    + "Thời gian tìm kiếm: " + formatTime(this.time) + "\n";
        } else {
            rs += "Không tìm được lời giải\nNguyên nhân:\n" + this.error + "\n";
        }
        return rs;
    }

    private String formatNumber(double value) {
        if (value == Integer.MAX_VALUE) {
            return "Quá hạn";
        }
        return formatMetric(value);
    }

    public static String formatTime(double milliseconds) {
        return String.format(Locale.US, "%.3f ms", milliseconds);
    }

    private String formatMetric(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format(Locale.US, "%.2f", value);
    }
}
