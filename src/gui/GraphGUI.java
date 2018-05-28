package gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GraphGUI extends Application {
	
	public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage mainStage) throws Exception {
        GraphModel model = new GraphModel();
        GraphView view = new GraphView(model);
        view.addMainStage(mainStage);
        new GraphController(model, view).addStage(mainStage);
        
        mainStage.setScene(view.getScene());
        mainStage.setTitle("GraphGUI");
        mainStage.setResizable(false);
        mainStage.getIcons().add(new Image("file:images\\icon.png"));
        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				event.consume();
				if (view.modified) {
					view.showCloseConfirmationDialog("Are you sure you want to close the graph?");
				} else {
					System.exit(0);
				}
			}
		});
        mainStage.show();
        
        Stage propertiesStage = new Stage();
        view.addPropertiesStage(propertiesStage);
        propertiesStage.setTitle("Properties");
        propertiesStage.setResizable(false);
    }
}
