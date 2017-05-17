package application.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.Node;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import application.Main;

public class MyClientController {
	@FXML
	private Button refreshButton;
	@FXML
	private Button closeAppButton;
	@FXML
	private Button connectButton;
	@FXML
	private Button disconnectButton;
	@FXML
	private GridPane chessBoard;
	@FXML
	private RadioButton localHost;
	@FXML
	private RadioButton remoteHost;
	@FXML
	private ToggleGroup toggleGroup;
	@FXML
	private TextField textIP;
	@FXML
	private ToggleGroup playGroup;
	@FXML
	private RadioButton playerFirst;
	@FXML
	private RadioButton playerSecond;
	@FXML
	private CheckBox playWithCom;
	
	private int steps;
	private String address;
    private final int port = 5678;//port
    private Socket connectSocket;
    private PrintWriter output;
    private BufferedReader input;
    private String inputLine;
    private String outputLine;
    private final String player1 = "×";
    private final String player2 = "○";
    
    //定義操作指令
    private final String commandDisconnect = "disconnect";
    private final String commandPlace = "place";
    private final String commandWin = "win";
    private final String commandContinue = "continue";
    private final String commandRestart = "restart";
    private final String commandDraw = "draw";
    private final String commandPlayWithCom = "playWithCom";
    private final String commandComStep = "comStep";
	// Reference to the main application.
    private Main main;
    
    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public MyClientController() {
    	address = "127.0.0.1";
    	steps = 0;
    }
    
    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    	textIP.setDisable(true);
        localHost.selectedProperty().addListener(new ChangeListener<Boolean>(){
        	@Override
        	public void changed(ObservableValue<? extends Boolean> obs, Boolean wasPreviouslySelected, Boolean isNowSelected){
        		if(isNowSelected){
        			textIP.setDisable(true);
        		}
        	}
        });
        
        remoteHost.selectedProperty().addListener(new ChangeListener<Boolean>(){
        	@Override
        	public void changed(ObservableValue<? extends Boolean> obs, Boolean wasPreviouslySelected, Boolean isNowSelected){
        		if(isNowSelected){
        			textIP.setDisable(false);
        		}
        	}
        });
        
        playWithCom.selectedProperty().addListener(new ChangeListener<Boolean>(){

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue){
					if(steps == 0){
						playerFirst.setDisable(false);
						playerSecond.setDisable(false);
					}else{
						comInMiddle();
					}
				}else{
					playerFirst.setDisable(true);
					playerSecond.setDisable(true);
				}
			}
        });
        
        playerSecond.selectedProperty().addListener(new ChangeListener<Boolean>(){

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue&&playWithCom.isSelected()){
					if(connectSocket!=null&&(!connectSocket.isClosed())){
						comFirst();
					}
				}
			}

        });
    }
    
    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param Main
     */
    public void setMainApp(Main mainApp) {
        this.main = mainApp;
    }
    
    @FXML
    private void chessBoardClicked(MouseEvent e) throws IOException{
    	if(connectSocket == null || connectSocket.isClosed()){
    		Alert alert = new Alert(AlertType.ERROR);
    		alert.setTitle("連線錯誤");
    		alert.setContentText("尚未連線至伺服，請先連線!");
    		alert.show();
    		return;
    	}
    	
    	if((!playWithCom.isDisable())&&playWithCom.isSelected()){
    		if(!(playerFirst.isSelected()||playerSecond.isSelected())){
    			Alert alert = new Alert(AlertType.CONFIRMATION);
        		alert.setTitle("請先選擇");
        		alert.setContentText("已勾選與電腦對戰，請先選擇先手或後手!");
        		alert.show();
        		return;
    		}else{
    			playerFirst.setDisable(true);
    			playerSecond.setDisable(true);
    			playWithCom.setDisable(true);
    		}
    	}
    	if(steps == 0){
    		refreshButton.setDisable(false);
    	}
    	
    	Node source = (Node)e.getSource();
    	Integer colIndex = GridPane.getColumnIndex(source);
    	Integer rowIndex = GridPane.getRowIndex(source);
    	Label label = (Label)source;
    	if(label.getText().toString().trim().length() == 0){
    		steps += 1;
    		if((steps%2) == 1){
    			label.setText("×");
    			label.setTextFill(Color.RED);
    		}else{
    			label.setText("○");
    		}
    		output.println(commandPlace);
    		output.println(colIndex);
    		output.println(rowIndex);
    		
    		inputLine = input.readLine();
			if(inputLine.equals(commandWin)){
				inputLine = input.readLine();
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("遊戲結束!");
				outputLine = (Integer.valueOf(inputLine) == 1) ? player1 : player2 ;
				outputLine = outputLine + " 方獲勝!";
				alert.setContentText(outputLine);
				alert.show();
			}else if(inputLine.equals(commandContinue)){
				System.out.println(inputLine);
			}else if(inputLine.endsWith(commandDraw)){
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("遊戲結束!");
				alert.setContentText("不分勝負的平手!");
				alert.show();
			}else if(inputLine.equals(commandComStep)){
				comStep();
			}
			
    	}
    }
    @FXML
    private void refreshButtonClicked(){
    	Label label;
    	for(int i=0;i<3;i++){
    		for(int j=0;j<3;j++){
    			label = getNodeByRowColumnIndex(i,j,chessBoard);
    			label.setText("");
    		}
    	}
    	output.println(commandRestart);
    	outputLine = new String();
    	steps = 0;
    	refreshButton.setDisable(true);
    }
    
    public Label getNodeByRowColumnIndex (final int row, final int column, GridPane gridPane) {
        Label result = null;
        ObservableList<Node> childrens = gridPane.getChildren();

        for (Node node : childrens) {
            if(GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = (Label)node;
                break;
            }
        }

        return result;
    }
    
    @FXML
    private void connectButtonClicked(){
    	if(localHost.isSelected()){
    		address = "127.0.0.1";
    	}else{
    		address = textIP.getText();
    	}
    	
    	connectSocket = new Socket();
    	
        InetSocketAddress isa = new InetSocketAddress(this.address, this.port);
        try {
            connectSocket.connect(isa);
            input = new BufferedReader(new InputStreamReader(connectSocket.getInputStream()));
            output = new PrintWriter(connectSocket.getOutputStream(),true);
            inputLine = new String();
            outputLine = new String();
            
            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
            if(steps == 0){
            	if(playWithCom.isSelected()&&playerSecond.isSelected()){
            		comFirst();
            		refreshButton.setDisable(true);
            	}
            }else{
            	refreshButton.setDisable(false);
            }
 
        } catch (java.io.IOException e) {
            System.out.println("Socket連線有問題 !");
            System.out.println("IOException :" + e.toString());
        }
    }
    
    @FXML
    private void disconnectButtonClicked(){
    	if(connectSocket != null){
    		try {
    			output.println(commandDisconnect);
    			input.close();
    			output.close();
    			inputLine = null;
    			outputLine = null;
				connectSocket.close();
				disconnectButton.setDisable(true);
				connectButton.setDisable(false);
				refreshButton.setDisable(true);
			} catch (IOException e) {
				// TODO 自動產生的 catch 區塊
				e.printStackTrace();
			}
    	}
    }
    
    @FXML
    private void closeAppButtonClicked() throws IOException{
    	if((connectSocket!=null)&&(!connectSocket.isClosed())){
    		output.println(commandDisconnect);
    		input.close();
    		output.close();
    		connectSocket.close();
    	}
    	System.exit(0);
    }
    
    private void comInMiddle(){
    	String comChess;
    	if((steps+1)%2 == 1){
    		comChess = player1;
    	}else{
    		comChess = player2;
    	}
    	Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("電腦對戰");
		alert.setContentText("選擇與電腦對戰，下一手"+comChess+"將由電腦下!");
		alert.show();
		output.println(commandPlayWithCom);
		comStep();
    }
    
    private void comFirst(){
    	Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("遊戲開始");
		alert.setContentText("選擇後手且與電腦對戰，遊戲即將開始!");
		alert.show();
		output.println(commandPlayWithCom);
		comStep();
    }
    
    private void comStep(){
    	int x,y;
    	try {
			inputLine = input.readLine();
			x = Integer.valueOf(inputLine);
	    	inputLine = input.readLine();
	    	y = Integer.valueOf(inputLine);
	    	setChessBoardEnable(false);
	    	Label label = getNodeByRowColumnIndex(x,y,chessBoard);
	    	if(label.getText().toString().trim().length() == 0){
	    		steps += 1;
	    		if((steps%2) == 1){
	    			label.setText("×");
	    			label.setTextFill(Color.RED);
	    		}else{
	    			label.setText("○");
	    		}
	    		setChessBoardEnable(true);
	    	}else{
	    		System.out.println("unknown error!!");
	    	}
		} catch (IOException e) {
			// TODO 自動產生的 catch 區塊
			e.printStackTrace();
		}
    }
    
    private void setChessBoardEnable(boolean value){
    	value = !value;
    	Label label;
    	for(int i=0;i<3;i++){
    		for(int j=0;j<3;j++){
    			label = getNodeByRowColumnIndex(i,j,chessBoard);
    			label.setDisable(value);
    		}
    	}
    }
}
