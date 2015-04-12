package il.ac.mta.bi.dmd.common;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * Server wrapper for serving client queries. Wraps the client handle function
 */
public class ServerWrapper {
	
	private static Logger logger = Logger.getLogger(ServerWrapper.class);
	private boolean serverRunning;
	private Integer listeningPort;
	private String serverDescription;
	private IClientHandler clientHandler;
	
	public ServerWrapper(Integer listeningPort, IClientHandler clientHandler) {
		this.listeningPort = listeningPort;
		this.clientHandler = clientHandler;
	}

	public void start() {
		if(serverRunning == false) {
			logger.info("starting up server on port=" + listeningPort + " for service: " + serverDescription);
			
			try {
				ServerSocket serverSocket = new ServerSocket(listeningPort);
				
				/* start server loop */
				Thread server = new Thread(new ServerLoop(serverSocket));
				server.start();
				
				serverRunning = true;
			} catch (IOException e) {
				logger.error("caught exception ",  e);
			} 
			
			System.out.println("listening on port "+ listeningPort + " for service: " + serverDescription);
		}
	}
	
	/* server loop thread */
	private class ServerLoop implements Runnable {
		private ServerSocket serverSocket;
		
		public ServerLoop(ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		}

		@Override
		public void run() {
			logger.info("starting up server loop");
			while(true) {
				try {
					Socket clientSocket = serverSocket.accept();
					logger.info("accepted connection from: " + clientSocket.getInetAddress() + " port: " + clientSocket.getPort());
					
					/* start server worker for handling the connection */
					Thread serverWorker = new Thread(new ServerWorker(clientSocket));
					serverWorker.start();
					
				} catch (IOException e) {
					logger.error("failed to create client socket");
				}
			}
		}
	}
	
	/* server worker thread */
	private class ServerWorker implements Runnable {
		private Socket clientSocket;
		
		public ServerWorker(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			try{
		    	BufferedReader in = new BufferedReader(new InputStreamReader(
		    			clientSocket.getInputStream()));

		    	PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		    	
		    	clientHandler.handle(in, out);
		    	
		    	out.close();
		    	in.close();
		    	
		    	logger.info("closed connection from: " + clientSocket.getInetAddress() + " port: " + clientSocket.getPort());
		    	
			} catch (Exception e) {
			    logger.error("caught exception ", e);
		    }
		}
	}

	public String getServerDescription() {
		return serverDescription;
	}

	public void setServerDescription(String serverDescription) {
		this.serverDescription = serverDescription;
	}

	public boolean isServerRunning() {
		return serverRunning;
	}

	public Integer getListeningPort() {
		return listeningPort;
	}
}
