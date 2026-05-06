module com.example.idastar {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.idastar to javafx.fxml;
    exports com.example.idastar;
}
