package serverTicTac;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
 
public class Server extends java.lang.Thread {
 
    private boolean OutServer = false;
    private ServerSocket server;
    private final int ServerPort = 5678;// 指定的port
    private Socket connectSocket;
    private PrintWriter output;
    private BufferedReader input;
    private String inputLine;
    private String outputLine;
    private final int arrayMAX = 3;
    private final int player1 = 1;
    private final int player2 = 2;
    private int[][] chessBoard;
    private int steps;
    private boolean playWithCom;
    
    //定義操作指令
    private final String commandPlace = "place";
    private final String commandDisconnect = "disconnect";
    private final String commandWin = "win";
    private final String commandContinue = "continue";
    private final String commandRestart = "restart";
    private final String commandDraw = "draw";
    private final String commandPlayWithCom = "playWithCom";
    private final String commandComStep = "comStep";
    
    public Server() {
    	playWithCom = false;
    	steps = 0;
    	chessBoard = new int[arrayMAX][arrayMAX];
    	for(int i=0;i<arrayMAX;i++){
    		for(int j=0;j<arrayMAX;j++){
    			chessBoard[i][j]=0;
    		}
    	}
        try {
            server = new ServerSocket(ServerPort);
 
        } catch (java.io.IOException e) {
            System.out.println("Socket啟動有問題 !");
            System.out.println("IOException :" + e.toString());
        }
    }
 
    public void run() {
 
        System.out.println("伺服器已啟動 !");
        while (!OutServer) {
            try {
                synchronized (server) {
                	connectSocket = server.accept();
                }
                System.out.println("取得連線 : InetAddress = "
                        + connectSocket.getInetAddress());
 
                input = new BufferedReader(new InputStreamReader(connectSocket.getInputStream()));
                output = new PrintWriter(connectSocket.getOutputStream(),true);
                inputLine = new String();
                outputLine = new String();
                
                do{
                	inputLine = input.readLine();
                	
                	if(inputLine.equals(commandDisconnect)){
                		connectSocket.close();
                		System.out.println("disconnect from client.");
                	}else if(inputLine.equals(commandPlace)){
                		int x,y;
                		inputLine = input.readLine();
                		x = Integer.valueOf(inputLine);
                		inputLine = input.readLine();
                		y = Integer.valueOf(inputLine);
                		steps += 1;
                		if((steps%2) == 1){
                			chessBoard[x][y] = player1;
                		}else{
                			chessBoard[x][y] = player2;
                		}
                		
                		
                		if(!(isGameSet())){
                			if(playWithCom){
                				ArrayList<Integer> nextStep = calculateStep();
                				if(nextStep.size() > 0){
                					steps += 1;
                					x = nextStep.get(0);
                					y = nextStep.get(1);
                					if((steps%2) == 1){
                            			chessBoard[x][y] = player1;
                            		}else{
                            			chessBoard[x][y] = player2;
                            		}
                					output.println(commandComStep);
                					output.println(x);
                					output.println(y);
                					output.println(commandContinue);
                				}else{
                					System.out.println("no Next step!");
                				}
                			}else{
                				output.println(commandContinue);
                			}
                		}
                		
                	}else if(inputLine.equals(commandRestart)){
                		gameRestart();
                		System.out.println("Game restart!");
                	}else if(inputLine.equals(commandPlayWithCom)){
                		playWithCom = true;
                	}else{
                		System.out.println("不知名的命令：" + inputLine);
                	}
                }while(!(connectSocket.isClosed()));
 
            } catch (java.io.IOException e) {
                System.out.println("Socket連線有問題 !");
                System.out.println("IOException :" + e.toString());
            }
 
        }
    }
    
    private boolean isGameSet() throws IOException{
    	boolean result = false;
    	if(isPlayerWin(player1)){
    		output.println(commandWin);
    		output.println(player1);
    		System.out.println("Player 1 win.");
    		result = true;
    	}else if(isPlayerWin(player2)){
    		output.println(commandWin);
    		output.println(player2);
    		System.out.println("Player 2 win.");
    		result = true;
    	}else if(steps == 9){
    		output.println(commandDraw);
    		System.out.println("Game ended in Draw!");
    		result = true;
    	}
    	System.out.println("result is " + String.valueOf(result));
    	
    	return result;
    }
    
    private boolean isPlayerWin(int player){
    	boolean result = false;
    	for(int i=0;i<arrayMAX;i++){
    		for(int j=0;j<arrayMAX;j++){
    			if(chessBoard[i][j] == player){
    				result = true;
    				continue;
    			}else{
    				result = false;
    				break;
    			}
    		}
    		if(result){
    			return result;
    		}
    	}
    	
    	for(int i=0;i<arrayMAX;i++){
    		for(int j=0;j<arrayMAX;j++){
    			if(chessBoard[j][i] == player){
    				result = true;
    				continue;
    			}else{
    				result = false;
    				break;
    			}
    		}
    		if(result){
    			return result;
    		}
    	}
    	
    	for(int i=0;i<arrayMAX;i++){
    		if(chessBoard[i][i] == player){
    			result = true;
    			continue;
    		}else{
    			result = false;
    			break;
    		}
    	}
    	if(result){
    		return result;
    	}
    	
    	for(int i=0;i<arrayMAX;i++){
    		if(chessBoard[i][arrayMAX-i-1] == player){
    			result = true;
    			continue;
    		}else{
    			result = false;
    			break;
    		}
    	}
    	
    	return result;
    }
    
    private void gameRestart(){
    	for(int i=0;i<arrayMAX;i++){
    		for(int j=0;j<arrayMAX;j++){
    			chessBoard[i][j]=0;
    		}
    	}
    	steps = 0;
    	playWithCom = false;
    }
    
    private ArrayList<Integer> calculateStep(){
    	ArrayList<Integer> result = new ArrayList<Integer>();
    	
    	return result;
    }
 
    public static void main(String args[]) {
        (new Server()).start();
    }
 
}
