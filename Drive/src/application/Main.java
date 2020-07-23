package application;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.app.googledrive.GGDriveUtils;
import com.google.api.services.drive.Drive;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.geometry.Insets;


public class Main extends Application {

	private Desktop desktop = Desktop.getDesktop();

	@Override
	public void start(Stage primaryStage) throws Exception {

		//menu
		final FileChooser fileChooser = new FileChooser();
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		TextArea textArea = new TextArea();
		TextField textDriveLink = new TextField();
		textDriveLink.setPromptText("Enter Google Drive Link");
		textDriveLink.setMinWidth(120);
		textArea.setMinHeight(50);
		Button button1 = new Button("Select One File and Open");
		Button buttonM = new Button("Select Multi Files");
		Button buttonUpdload = new Button("Upload To Google Drive");
		Button buttonDownload = new Button("Download from Google Drive");
		MenuBar menubar = new MenuBar();  
		Menu fileMenu = new Menu("File");  
		MenuItem filemenu1=new MenuItem("new");  
		MenuItem filemenu2=new MenuItem("Save");  
		MenuItem filemenu3=new MenuItem("Exit");  
		Menu editMenu=new Menu("Edit");  
		MenuItem EditMenu1=new MenuItem("Cut");  
		MenuItem EditMenu2=new MenuItem("Copy");  
		MenuItem EditMenu3=new MenuItem("Paste");  
		editMenu.getItems().addAll(EditMenu1,EditMenu2,EditMenu3);  
		fileMenu.getItems().addAll(filemenu1,filemenu2,filemenu3);  
		menubar.getMenus().addAll(fileMenu,editMenu);  


		//set event for 
		button1.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				textArea.clear();
				File file = fileChooser.showOpenDialog(primaryStage);
				if (file != null) {
					openFile(file);
					List<File> files = Arrays.asList(file);
					printLog(textArea, files);
				}

			}
		});
		//set event for filechooser button
		buttonM.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				textArea.clear();
				List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);

				printLog(textArea, files);
			}
		});

		buttonUpdload.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				
				if(textDriveLink.getText().isEmpty()) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Ting Ting");
					alert.setContentText("Please Enter a Link Fist!");
					alert.showAndWait();
					return;						
				}

				String[] folderIdTemp = textDriveLink.getText().split("/");
				GGDriveUtils utils=new GGDriveUtils();
				textArea.clear();
				//                List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
				String folderId=folderIdTemp[folderIdTemp.length-1];
				File dir = directoryChooser.showDialog(primaryStage);
				textArea.setText(dir.getAbsolutePath());
				utils.uploadFile(folderId, dir.getAbsolutePath());
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Ting Ting");
				alert.setContentText("Upload Successfully!");
				alert.showAndWait();
			}
		});
		buttonDownload.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {


				try {
					if(textDriveLink.getText().isEmpty()) {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("Ting Ting");
						alert.setContentText("Please Enter a Link ");
						alert.showAndWait();
						return;						
					}

					String[] folderId = textDriveLink.getText().split("/");
					GGDriveUtils utils=new GGDriveUtils();
					Drive driveService=GGDriveUtils.getDriveService();
					com.google.api.services.drive.model.File file = driveService.files().get(folderId[folderId.length-1]).execute();
					utils.getFilesToDownload("D:\\Downloads", file);
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Ting Ting");
					alert.setContentText("Download Successfully! \n "
							+ "Data Location In:"
							+ "D:\\Downloads\\"+file.getName());
					alert.showAndWait();

				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}



			}
		});
		VBox root = new VBox();
		root.setPadding(new Insets(10));
		root.setSpacing(5);
		root.getChildren().addAll(menubar,textArea,textDriveLink,buttonUpdload,buttonDownload);
		Scene scene = new Scene(root, 400, 200);
		primaryStage.setTitle("JavaFX FileChooser");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void printLog(TextArea textArea, List<File> files) {
		if (files == null || files.isEmpty()) {
			return;
		}
		for (File file : files) {
			textArea.appendText(file.getAbsolutePath() + "\n");
		}
	}

	private void openFile(File file) {
		try {
			this.desktop.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}
