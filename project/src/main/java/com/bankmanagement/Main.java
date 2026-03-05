// package com.bankmanagement;

// import java.sql.Connection;

// import com.bankmanagement.config.dbConnection;

// public class Main {
//     public static void main(String[] args) {
//         dbConnection a = new dbConnection();
//         Connection connect = a.getConnection();
//     }
// }

package com.bankmanagement;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/UpdateInfo.fxml"));

        Scene scene = new Scene(root, 480, 600);
        stage.setTitle("BankManagement");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}