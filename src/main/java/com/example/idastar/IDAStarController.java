package com.example.idastar;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.net.URL;
import java.util.*;

import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class IDAStarController implements Initializable, Runnable {
    private static final double IMAGE_CORNER_RADIUS = 12;

    @FXML
    private ToggleGroup difficultyToggle;
    @FXML
    private ToggleGroup algorithmToggle;
    @FXML
    private Canvas imgCanvas;
    @FXML
    private ImageView imgView;
    @FXML
    private Button solveBtn;
    @FXML
    private Button playBtn;
    @FXML
    private Button jumbleBtn;
    @FXML
    private Button addImage;
    @FXML
    private Button changeImage;
    @FXML
    private Button benchmarkBtn;
    @FXML
    private Button compareBtn;
    @FXML
    private SplitMenuButton sizeMenu;
    @FXML
    private SplitMenuButton algorithmMenu;
    @FXML
    private TextField stepField;
    @FXML
    private AnchorPane displayPane;
    @FXML
    private AnchorPane statsPane;
    @FXML
    private TableView<Result> statsTable;
    @FXML
    private TableColumn<Result, String> heuristicColumn;
    @FXML
    private TableColumn<Result, String> descriptionColumn;
    @FXML
    private TableColumn<Result, String> statusColumn;
    @FXML
    private TableColumn<Result, String> stepColumn;
    @FXML
    private TableColumn<Result, String> approvedColumn;
    @FXML
    private TableColumn<Result, String> totalColumn;
    @FXML
    private TableColumn<Result, String> timeColumn;
    @FXML
    private TableColumn<Result, String> iterationColumn;
    @FXML
    private TableColumn<Result, String> boundColumn;
    @FXML
    private Label statsSummaryLabel;
    @FXML
    private Label statsTitleLabel;
    @FXML
    private VBox statsChartBox;
    @FXML
    private ComboBox<String> benchmarkSizeBox;
    @FXML
    private TextField benchmarkTestField;
    @FXML
    private Button runBenchmarkBtn;

    public IDAStar idaStar;
    public Image image;
    public HandleImage handledImage;
    private int size;
    private State state;
    private State goalState;
    private int[] value;
    private Vector<int[]> result;
    private int countStep = 0;
    private boolean isSolve = false;
    private boolean isPlay = false;
    private int approvedNodes;
    private int totalNodes;
    private int searchIterations;
    private int finalBound;
    private double solveTime;
    private long startTime;
    private String error;
    private final Vector<Result> compareResults = new Vector<>();
    private final ObservableList<Result> statisticsData = FXCollections.observableArrayList();

    @Override
    // Trạng thái khởi tạo ban đầu
    public void initialize(URL url, ResourceBundle resourceBundle) {
        State.heuristic = 1;
        State.goal = 2;
        size = 3;
        state = new State(size);
        value = state.createGoalArray();
        goalState = new State(size);
        goalState.createGoalArray();
        clipPreviewImage();
        clipPuzzleCanvas();
        loadSampleImage();
        displayImage(image);
        initBenchmarkControls();
        initStatisticsTable();
    }
    // Luồng chạy lời giải
    public void run() {
        int totalStep = result.size() - 1;
        for (int i = 0; i <= totalStep; i++) {
            value = result.get(i);
            state.value = value;
            displayImage(image);
            String step = i + "/" + totalStep;
            Platform.runLater(() -> stepField.setText(step));
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Platform.runLater(this::notSolve);
    }

    @FXML
    // Chọn size bảng
    public void onChangeImageSize() {
        hideStatsPage();
        RadioMenuItem selectedDiff = (RadioMenuItem) difficultyToggle.getSelectedToggle();
        switch (selectedDiff.getId()) {
            case "medium" -> size = 4;
            case "hard" -> size = 5;
            default -> size = 3;
        }
        sizeMenu.setText(selectedDiff.getText());
        state = new State(size);
        value = state.createGoalArray();
        goalState = new State(size);
        goalState.createGoalArray();
        displayImage(image);
    }
    // Chọn thuật toán
    public void onChangeAlgorithm() {
        hideStatsPage();
        RadioMenuItem selectedAlgorithm = (RadioMenuItem) algorithmToggle.getSelectedToggle();
        switch (selectedAlgorithm.getId()) {
            case "heuristic1" -> {
                State.heuristic = 1;
            }
            case "heuristic2" -> {
                State.heuristic = 2;
            }
            case "heuristic3" -> {
                State.heuristic = 3;
            }
            case "heuristic4" -> {
                State.heuristic = 4;
            }
            case "heuristic5" -> {
                State.heuristic = 5;
            }
            case "heuristic6" -> {
                State.heuristic = 6;
            }
            default -> State.heuristic = 1;
        }
        algorithmMenu.setText(selectedAlgorithm.getText());
    }
    private void loadSampleImage() {
        image = new Image(Objects.requireNonNull(IDAStarApplication.class.getResourceAsStream("img/monalisa.jpg")));
        setPreviewImage(image);
    }

    private void setPreviewImage(Image previewImage) {
        double squareSize = Math.min(previewImage.getWidth(), previewImage.getHeight());
        double sourceX = (previewImage.getWidth() - squareSize) / 2;
        double sourceY = (previewImage.getHeight() - squareSize) / 2;
        imgView.setViewport(new Rectangle2D(sourceX, sourceY, squareSize, squareSize));
        imgView.setPreserveRatio(false);
        imgView.setImage(previewImage);
    }

    private void clipPreviewImage() {
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(imgView.getFitWidth(), imgView.getFitHeight());
        clip.setArcWidth(IMAGE_CORNER_RADIUS);
        clip.setArcHeight(IMAGE_CORNER_RADIUS);
        imgView.setClip(clip);
    }

    private void clipPuzzleCanvas() {
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(imgCanvas.getWidth(), imgCanvas.getHeight());
        clip.setArcWidth(IMAGE_CORNER_RADIUS);
        clip.setArcHeight(IMAGE_CORNER_RADIUS);
        imgCanvas.setClip(clip);
    }

    private void resetCurrentPuzzle() {
        countStep = 0;
        stepField.setText("0");
        value = state.createGoalArray();
        displayImage(image);
    }

    // Button ảnh mẫu
    public void onAddImgBtnClick() {
        hideStatsPage();
        loadSampleImage();
        resetCurrentPuzzle();
    }

    // Button đổi ảnh
    public void onChangeImgBtnClick() {
        hideStatsPage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        Stage stage = (Stage) imgView.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        Image selectedImage = new Image(selectedFile.toURI().toString());
        if (selectedImage.isError() || selectedImage.getWidth() <= 0 || selectedImage.getHeight() <= 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Thông báo");
            alert.setHeaderText("Không thể mở ảnh đã chọn");
            alert.setContentText("Vui lòng chọn file ảnh hợp lệ.");
            alert.showAndWait();
            return;
        }

        image = selectedImage;
        setPreviewImage(image);
        resetCurrentPuzzle();
    }
    // Button trộn ảnh
    public void onJumbleBtnClick() {
        hideStatsPage();
        stepField.setText("0");
        countStep = 0;
        value = state.createRandomArray();
        displayImage(image);
    }
    // Button tìm kết quả
    public void onSolveBtnClick() {
        hideStatsPage();
        countStep = 0;
        if (!isSolve) {
            IDAStar.stop = false;
            solveThread().start();
            solving();
        } else {
            IDAStar.stop = true;
            notSolve();
        }
    }
    // Button so sánh Heuristic
    public void onCompareBtnClick() {
        hideStatsPage();
        IDAStar.stop = false;
        compareThread().start();
    }
    // Button đánh giá Heuristic trên nhiều bộ test
    public void onBenchmarkBtnClick() {
        hideStatsPage();
        showBenchmarkControls(true);
        if (statsTitleLabel != null) {
            statsTitleLabel.setText("Đánh giá Heuristic");
        }
        statisticsData.clear();
        if (statsSummaryLabel != null) {
            statsSummaryLabel.setText("Chọn kích thước, nhập số bộ test rồi bấm Chạy đánh giá.");
        }
        if (statsChartBox != null) {
            statsChartBox.getChildren().clear();
        }
        showStatsPage();
    }

    @FXML
    public void onRunBenchmarkClick() {
        int benchmarkSize = selectedBenchmarkSize();
        int testCount;
        try {
            testCount = Integer.parseInt(benchmarkTestField.getText().trim());
        } catch (NumberFormatException e) {
            showBenchmarkInputError("Số bộ test phải là số nguyên dương.");
            return;
        }
        if (testCount <= 0) {
            showBenchmarkInputError("Số bộ test phải lớn hơn 0.");
            return;
        }
        if (testCount > 100) {
            showBenchmarkInputError("Số bộ test tối đa là 100 để tránh chạy quá lâu.");
            return;
        }

        IDAStar.stop = false;
        benchmarkThread(benchmarkSize, testCount).start();
    }
    // Button chơi
    public void onPlayBtnClick() {
        if (!isPlay) {
            playing();
            startTime = System.currentTimeMillis();
        } else {
            countStep = 0;
            notPlay();
        }
    }
    // Sự kiện từ bàn phím
    public void onKeyPressed(KeyEvent ke) {
        if (isPlay) {
            countStep++;
            int[] tmpValue = Arrays.copyOf(value, size * size);
            switch (ke.getCode()) {
                case W -> state.UP();
                case A -> state.LEFT();
                case S -> state.DOWN();
                case D -> state.RIGHT();
                default -> value = tmpValue;            }
            if (Arrays.equals(tmpValue, value)) {
                countStep--;
            }
            if (Arrays.equals(value, goalState.value)) {
                if (countStep != 0) {
                    showResult();
                    countStep = 0;
                }
            }
            stepField.setText(String.valueOf(countStep));
            displayImage(image);
        }
    }
    // Sự kiện click chuột
    public void onMouseClicked(MouseEvent me) {
        if (isPlay) {
            int blank = state.posBlank(state.value);
            int x = blank % size;
            int y = blank / size;
            int mx = (int) (me.getX() / imgCanvas.getWidth() * size);
            int my = (int) (me.getY() / imgCanvas.getHeight() * size);
            countStep++;
            if (mx == x && my == y - 1) {
                state.UP();
            } else if (mx == x && my == y + 1) {
                state.DOWN();
            } else if (mx == x - 1 && my == y) {
                state.LEFT();
            } else if (mx == x + 1 && my == y) {
                state.RIGHT();
            } else {
                countStep--;
            }
            if (Arrays.equals(value, goalState.value)) {
                if (countStep != 0) {
                    showResult();
                    countStep = 0;
                }
            }
            stepField.setText(String.valueOf(countStep));
            displayImage(image);
        }
    }
    // Giải quyết bài toán bằng thuật toán IDA*
    public void solveIDAStar() {
        idaStar = new IDAStar();
        idaStar.startNode = new Node(state, 0);
        idaStar.goalNode = new Node(goalState, 1);
        idaStar.solve();
        result = idaStar.RESULT;
        approvedNodes = idaStar.approvedNodes;
        totalNodes = idaStar.totalNodes;
        searchIterations = idaStar.iterations;
        finalBound = idaStar.finalBound;
        solveTime = idaStar.time;
        error = idaStar.error;
    }
    // Luồng tìm kiếm lời giải
    public Thread solveThread() {
        return new Thread(() -> {
            solveIDAStar();
            // Nếu tìm được lời giải
            if (result.size() > 1) {
                Platform.runLater(this::showAlert);
            }
            // Nếu không tìm được lời giải
            else if(result.size() == 0 && error != null) {
                Platform.runLater(this::showWarning);
            }
            // Người chơi chọn dừng tìm kiếm hoặc trạng thái ban đầu là trạng thái đích
            else {
                Platform.runLater(this::notSolve);
            }
        });
    }
    // Luồng so sánh Heuristic
    public Thread compareThread() {
        return new Thread(() -> {
            int tmp = State.heuristic;
            compareResults.clear();
            goalState.createGoalArray();
            Platform.runLater(this::solving);
            // Giải bài toán bằng lần lượt các Heuristic
            for (int i = 1; i <= State.HEURISTIC_COUNT; i++) {
                if (IDAStar.stop) {
                    break;
                }
                State.heuristic = i;
                solveIDAStar();
                int steps = result.isEmpty() ? -1 : result.size() - 1;
                Result result1 = new Result("H" + i, State.heuristicName(i), approvedNodes, totalNodes,
                        steps, solveTime, error, searchIterations, finalBound);
                compareResults.add(result1); // Lưu kết quả
            }
            // Nếu dừng so sánh
            if (IDAStar.stop) {
                compareResults.clear();
                Platform.runLater(this::notSolve);
            } else {
                Platform.runLater(this::showCompare); // Show bảng so sánh
            }
            State.heuristic = tmp;
        });
    }

    public Thread benchmarkThread(int benchmarkSize, int testCount) {
        return new Thread(() -> {
            int tmpHeuristic = State.heuristic;
            Vector<Result> benchmarkResults = new Vector<>();
            double[] approvedTotals = new double[State.HEURISTIC_COUNT + 1];
            double[] generatedTotals = new double[State.HEURISTIC_COUNT + 1];
            double[] stepTotals = new double[State.HEURISTIC_COUNT + 1];
            double[] timeTotals = new double[State.HEURISTIC_COUNT + 1];
            double[] iterationTotals = new double[State.HEURISTIC_COUNT + 1];
            double[] boundTotals = new double[State.HEURISTIC_COUNT + 1];
            int[] solvedCounts = new int[State.HEURISTIC_COUNT + 1];

            Platform.runLater(() -> {
                solving();
                setBenchmarkControlsDisable(true);
                statsSummaryLabel.setText("Đang chạy đánh giá " + testCount + " bộ test...");
            });

            for (int test = 0; test < testCount; test++) {
                if (IDAStar.stop) {
                    break;
                }
                State randomState = new State(benchmarkSize);
                int[] testValue = randomState.createRandomArray().clone();
                for (int heuristic = 1; heuristic <= State.HEURISTIC_COUNT; heuristic++) {
                    if (IDAStar.stop) {
                        break;
                    }
                    State.heuristic = heuristic;
                    Result rs = solveIDAStarFor(testValue, benchmarkSize);
                    approvedTotals[heuristic] += rs.approved == Integer.MAX_VALUE ? 0 : rs.approved;
                    generatedTotals[heuristic] += rs.total == Integer.MAX_VALUE ? 0 : rs.total;
                    stepTotals[heuristic] += rs.step < 0 ? 0 : rs.step;
                    timeTotals[heuristic] += rs.time;
                    iterationTotals[heuristic] += rs.iterations;
                    boundTotals[heuristic] += rs.bound;
                    if (rs.isSolved()) {
                        solvedCounts[heuristic]++;
                    }
                }
            }

            if (!IDAStar.stop) {
                for (int heuristic = 1; heuristic <= State.HEURISTIC_COUNT; heuristic++) {
                    String description = State.heuristicName(heuristic) + " | Đạt "
                            + solvedCounts[heuristic] + "/" + testCount;
                    benchmarkResults.add(new Result(
                            "H" + heuristic,
                            description,
                            approvedTotals[heuristic] / testCount,
                            generatedTotals[heuristic] / testCount,
                            stepTotals[heuristic] / testCount,
                            timeTotals[heuristic] / testCount,
                            null,
                            iterationTotals[heuristic] / testCount,
                            boundTotals[heuristic] / testCount
                    ));
                }
            }

            State.heuristic = tmpHeuristic;
            if (IDAStar.stop) {
                Platform.runLater(() -> {
                    setBenchmarkControlsDisable(false);
                    notSolve();
                });
            } else {
                Platform.runLater(() -> showBenchmarkResults(benchmarkResults, benchmarkSize, testCount));
            }
        });
    }

    private Result solveIDAStarFor(int[] startValue, int benchmarkSize) {
        State startState = new State(startValue.clone(), benchmarkSize);
        State benchmarkGoal = new State(benchmarkSize);
        benchmarkGoal.createGoalArray();

        IDAStar solver = new IDAStar();
        solver.startNode = new Node(startState, 0);
        solver.goalNode = new Node(benchmarkGoal, 1);
        solver.solve();
        int steps = solver.RESULT.isEmpty() ? -1 : solver.RESULT.size() - 1;
        return new Result("H" + State.heuristic, State.heuristicName(State.heuristic), solver.approvedNodes,
                solver.totalNodes, steps, solver.time, solver.error, solver.iterations, solver.finalBound);
    }
    // Trạng thái đang tìm kiếm
    public void solving() {
        isSolve = true;
        solveBtn.setText("Dừng");
        playBtn.setDisable(true);
        setDisable();
    }
    // Trạng thái không tìm kiếm
    public void notSolve() {
        isSolve = false;
        solveBtn.setText("Giải tự động");
        playBtn.setDisable(false);
        setEnable();
    }
    // Trạng thái người chơi
    public void playing() {
        isPlay = true;
        playBtn.setText("Dừng");
        solveBtn.setDisable(true);
        setDisable();
    }
    // Trạng thái không chơi
    public void notPlay() {
        isPlay = false;
        playBtn.setText("Chơi");
        solveBtn.setDisable(false);
        setEnable();
    }
    // Enable các nút
    private void setEnable() {
        solveBtn.setDisable(false);
        jumbleBtn.setDisable(false);
        addImage.setDisable(false);
        changeImage.setDisable(false);
        benchmarkBtn.setDisable(false);
        compareBtn.setDisable(false);
        sizeMenu.setDisable(false);
        algorithmMenu.setDisable(false);
        setBenchmarkControlsDisable(false);
    }
    // Disable các nút
    private void setDisable() {
        jumbleBtn.setDisable(true);
        addImage.setDisable(true);
        changeImage.setDisable(true);
        benchmarkBtn.setDisable(true);
        compareBtn.setDisable(true);
        sizeMenu.setDisable(true);
        algorithmMenu.setDisable(true);
        setBenchmarkControlsDisable(true);
    }
    // Bảng thông báo không tìm được lời giải
    public void showWarning() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        ButtonType closeTypeBtn = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(closeTypeBtn);
        alert.setTitle("Thông báo");
        alert.setHeaderText("Không tìm được lời giải!");
        alert.setContentText("Nguyên nhân: \n" + error);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(IDAStarApplication.class.getResourceAsStream("img/monalisa.jpg"))));
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        alert.showAndWait().ifPresent(res -> notSolve());
    }
    // Bảng thông báo kết quả tìm kiếm
    public void showAlert() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        ButtonType runTypeBtn = new ButtonType("Chạy", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeTypeBtn = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.setTitle("Thông báo");
        alert.getButtonTypes().setAll(runTypeBtn, closeTypeBtn);
        alert.setHeaderText("Lời giải: ");
        // Kết quả tìm kiếm được
        String idaStats = "Số vòng lặp IDA*: " + searchIterations + "\n"
                + "Ngưỡng cuối: " + finalBound + "\n";
        alert.setContentText("Thuật toán sử dụng: IDA* với Heuristic " + State.heuristic + "\n"
            + "Số node đã duyệt: " + approvedNodes + "\n"
            + "Tổng số node đã sinh: " + totalNodes + "\n"
            + idaStats
            + "Tổng số bước: " + (result.size() - 1) + "\n"
            + "Thời gian tìm kiếm: " + Result.formatTime(solveTime) + "\n"
            + "Bạn có muốn chạy lời giải?"
        );
        alertStyle(alert, closeTypeBtn);
        // Hiển thị kết quả và đợi phải hồi
        alert.showAndWait().ifPresent(res -> {
            if (res == runTypeBtn) {
                solveBtn.setDisable(true);
                Thread runResult = new Thread(this);
                runResult.start();
            } else {
                notSolve();
            }
        });
    }
    // Hiển thị bảng so sánh Heuristic
    private void initBenchmarkControls() {
        if (benchmarkSizeBox == null) {
            return;
        }
        benchmarkSizeBox.setItems(FXCollections.observableArrayList("3*3", "4*4", "5*5"));
        benchmarkSizeBox.getSelectionModel().select("3*3");
        benchmarkTestField.setText("10");
        showBenchmarkControls(false);
    }

    private void initStatisticsTable() {
        if (statsTable == null) {
            return;
        }
        heuristicColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getHeuristic()));
        descriptionColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDescription()));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getStatus()));
        stepColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getStepText()));
        approvedColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getApprovedText()));
        totalColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getTotalText()));
        timeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getTimeText()));
        iterationColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getIterationsText()));
        boundColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getBoundText()));
        statsTable.setItems(statisticsData);
        hideStatsPage();
    }

    private void showBenchmarkControls(boolean visible) {
        if (benchmarkSizeBox == null || benchmarkTestField == null || runBenchmarkBtn == null) {
            return;
        }
        benchmarkSizeBox.setVisible(visible);
        benchmarkSizeBox.setManaged(visible);
        benchmarkTestField.setVisible(visible);
        benchmarkTestField.setManaged(visible);
        runBenchmarkBtn.setVisible(visible);
        runBenchmarkBtn.setManaged(visible);
    }

    private void setBenchmarkControlsDisable(boolean disabled) {
        if (benchmarkSizeBox == null || benchmarkTestField == null || runBenchmarkBtn == null) {
            return;
        }
        benchmarkSizeBox.setDisable(disabled);
        benchmarkTestField.setDisable(disabled);
        runBenchmarkBtn.setDisable(disabled);
    }

    private int selectedBenchmarkSize() {
        String selected = benchmarkSizeBox.getSelectionModel().getSelectedItem();
        if ("4*4".equals(selected)) {
            return 4;
        }
        if ("5*5".equals(selected)) {
            return 5;
        }
        return 3;
    }

    private void showBenchmarkInputError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo");
        alert.setHeaderText("Thông tin đánh giá chưa hợp lệ");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String buildStatisticsSummary(Vector<Result> results) {
        Vector<Result> solvedResults = new Vector<>();
        for (Result rs : results) {
            if (rs.isSolved()) {
                solvedResults.add(rs);
            }
        }
        if (solvedResults.isEmpty()) {
            return "Không có heuristic tìm được lời giải trong giới hạn.";
        }

        Result fastest = Collections.min(solvedResults, Comparator.comparingDouble(rs -> rs.time));
        Result leastExpanded = Collections.min(solvedResults, Comparator.comparingDouble(rs -> rs.approved));
        Result leastGenerated = Collections.min(solvedResults, Comparator.comparingDouble(rs -> rs.total));
        Result shortest = Collections.min(solvedResults, Comparator.comparingDouble(rs -> rs.step));
        return "Nhanh nhất: " + fastest.heuristic + " (" + Result.formatTime(fastest.time) + ")"
                + " | Ít node duyệt nhất: " + leastExpanded.heuristic + " (" + formatMetricValue(leastExpanded.approved) + ")"
                + " | Ít node sinh nhất: " + leastGenerated.heuristic + " (" + formatMetricValue(leastGenerated.total) + ")"
                + " | Đường đi ngắn nhất: " + shortest.heuristic + " (" + formatMetricValue(shortest.step) + " bước)";
    }

    private void showStatsPage() {
        statsPane.setManaged(true);
        statsPane.setVisible(true);
    }

    private void hideStatsPage() {
        if (statsPane != null) {
            statsPane.setVisible(false);
            statsPane.setManaged(false);
        }
    }

    @FXML
    public void onCloseStatsClick() {
        hideStatsPage();
    }

    public void showCompare() {
        if (statsTable != null && statsSummaryLabel != null && statsPane != null) {
            showBenchmarkControls(false);
            if (statsTitleLabel != null) {
                statsTitleLabel.setText("Đánh giá các lời giải");
            }
            statisticsData.setAll(compareResults);
            statsSummaryLabel.setText(buildStatisticsSummary(compareResults));
            buildStatisticsCharts(compareResults);
            showStatsPage();
            notSolve();
            compareResults.clear();
            return;
        }
        Alert compare = new Alert(Alert.AlertType.CONFIRMATION);
        ButtonType runTypeBtn = new ButtonType("Chạy", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeTypeBtn = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        compare.setTitle("Thông báo");
        compare.setHeaderText("So sánh: ");
        // Hiển thị kết quả
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(15);
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        // Hiển thị kết quả của từng Heuristic
        for (int i = 0; i < compareResults.size(); i++) {
            Result rs = compareResults.get(i);
            Label rsLabel = new Label(rs.showResult());
            GridPane.setConstraints(rsLabel, i % 3, i / 3);
            gridPane.getChildren().add(rsLabel);
        }
        // Sắp xếp và hiển thị kết quả so sánh
        compareResults.sort(Comparator.comparingDouble(o -> o.approved));
        Collections.reverse(compareResults);
        Label cpLabel = new Label("Kết luận : ");
        boolean flag = false;
        for (Result rs : compareResults) {
            if (rs.error == null) {
                cpLabel.setText(cpLabel.getText() + rs.heuristic + (rs == compareResults.lastElement() ? " " : " < "));
                flag = true;
            }
        }
        // Flag kiểm tra xem có tìm được kết quả hay không
        if (flag) {
            cpLabel.setText(cpLabel.getText() + ". Bạn có muốn chạy lời giải?");
            compare.getButtonTypes().setAll(runTypeBtn, closeTypeBtn);
        } else {
            cpLabel.setText(cpLabel.getText() + "Không tìm được lời giải!");
            compare.getButtonTypes().setAll(closeTypeBtn);
        }
        alertStyle(compare, closeTypeBtn);
        vBox.getChildren().addAll(gridPane, cpLabel);
        compare.getDialogPane().setContent(vBox);
        // Chờ phải hồi
        compare.showAndWait().ifPresent(res -> {
            if (res == runTypeBtn) {
                solveBtn.setDisable(true);
                Thread runResult = new Thread(this);
                runResult.start();
            } else {
                notSolve();
            }
        });
        // Clear vector kết quả
        compareResults.clear();
    }

    public void showBenchmarkResults(Vector<Result> benchmarkResults, int benchmarkSize, int testCount) {
        statisticsData.setAll(benchmarkResults);
        statsSummaryLabel.setText("Kích thước: " + benchmarkSize + "*" + benchmarkSize
                + " | Số bộ test: " + testCount
                + " | Các chỉ số trong bảng và biểu đồ là giá trị trung bình trên toàn bộ test.");
        buildStatisticsCharts(benchmarkResults);
        setBenchmarkControlsDisable(false);
        showStatsPage();
        notSolve();
    }

    private void buildStatisticsCharts(Vector<Result> results) {
        if (statsChartBox == null) {
            return;
        }
        statsChartBox.getChildren().clear();
        statsChartBox.getChildren().add(createChart("Số bước giải", "Bước", results, rs -> rs.isSolved() ? rs.step : -1));
        statsChartBox.getChildren().add(createChart("Số node đã duyệt", "Node", results, rs -> rs.approved));
        statsChartBox.getChildren().add(createChart("Số node đã sinh", "Node", results, rs -> rs.total));
        statsChartBox.getChildren().add(createChart("Thời gian tìm kiếm", "ms", results, rs -> rs.time));
        statsChartBox.getChildren().add(createChart("Số vòng lặp IDA*", "Lần lặp", results, rs -> rs.iterations));
        statsChartBox.getChildren().add(createChart("Ngưỡng f cuối", "Ngưỡng", results, rs -> rs.bound));
    }

    private BarChart<String, Number> createChart(String title, String yLabel, Vector<Result> results,
                                                 MetricProvider metricProvider) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Hàm heuristic");
        yAxis.setLabel(yLabel);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefSize(660, 310);
        chart.setMinHeight(310);
        chart.setCategoryGap(14);
        chart.setBarGap(4);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        double maxMetric = 0;
        for (Result rs : results) {
            double metric = metricProvider.value(rs);
            if (metric < 0 || metric == Integer.MAX_VALUE) {
                metric = 0;
            }
            maxMetric = Math.max(maxMetric, metric);
            XYChart.Data<String, Number> data = new XYChart.Data<>(rs.heuristic, metric);
            data.nodeProperty().addListener((observable, oldNode, node) -> {
                if (node instanceof StackPane stackPane) {
                    Label valueLabel = new Label(formatMetricValue(data.getYValue().doubleValue()));
                    valueLabel.getStyleClass().add("chart-value-label");
                    valueLabel.setMouseTransparent(true);
                    StackPane.setAlignment(valueLabel, Pos.TOP_CENTER);
                    stackPane.getChildren().add(valueLabel);
                }
            });
            series.getData().add(data);
        }
        configureAxisRange(yAxis, maxMetric);
        chart.getData().add(series);
        return chart;
    }

    private void configureAxisRange(NumberAxis yAxis, double maxMetric) {
        double upperBound = maxMetric <= 0 ? 1 : maxMetric * 1.25;
        if (upperBound >= 10) {
            upperBound = Math.ceil(upperBound);
        }
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(upperBound);
        yAxis.setTickUnit(upperBound >= 5 ? Math.ceil(upperBound / 5) : upperBound / 5);
    }

    private String formatMetricValue(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format(Locale.US, "%.3f", value);
    }

    private interface MetricProvider {
        double value(Result result);
    }
    // Show kết quả người chơi
    public void showResult() {
        long time = (System.currentTimeMillis() - startTime) / 1000;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText("Bạn đã hoàn thành trò chơi!");
        alert.setContentText("Số bước giải: " + countStep + "\n"
            + "Thời gian giải: " + (time >= 60 ? time / 60 + ":" + time % 60 : time) + "s"
        );
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(IDAStarApplication.class.getResourceAsStream("img/monalisa.jpg"))));
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        alert.showAndWait().ifPresent(res -> notPlay());
    }
    // Thêm icon và style cho bảng lời giải và bảng so sánh
    public void alertStyle(Alert alert, ButtonType closeTypeBtn) {
        // Thêm icon
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(IDAStarApplication.class.getResourceAsStream("img/monalisa.jpg"))));
        DialogPane dialogPane = alert.getDialogPane();
        // Thêm css
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        javafx.scene.Node closeBtn = alert.getDialogPane().lookupButton(closeTypeBtn);
        closeBtn.setId("close-btn");
    }
    // Hiển thị ra màn hình
    public void displayImage(Image img) {
        if (img == null) {
            displayPane.setStyle("-fx-background-radius: 20px; -fx-background-color: #703838");
        } else {
            displayPane.setStyle("");
        }
        handledImage = new HandleImage(img ,size, value);
        if (state.isGoal(goalState)) {
            handledImage.win = true;
        }
        GraphicsContext gc = imgCanvas.getGraphicsContext2D();
        handledImage.paint(gc);
    }
}
