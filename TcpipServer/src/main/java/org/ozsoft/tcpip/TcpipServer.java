package org.ozsoft.tcpip;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * Simple TCP/IP server application for sending and receiving text messages. <br />
 * <br />
 * 
 * Conveniently saves the most recent sent messages. <br />
 * <br />
 * 
 * Acts as server, handling only 1 client connection at any given time. Once a
 * client has connected, the server stops listening for incoming connections.
 * After a client has disconnected, the server starts listening again for a new
 * connection. <br />
 * <br />
 * 
 * The text messages (as <code>String</code>s must be delimited with either one
 * LINEFEED character (0x0a) or a pair of STX/ETX characters (0x02/0x03). <br />
 * <br />
 * 
 * Implemented with the JBoss Netty framework (http://www.jboss.org/netty/) as
 * event-driven abstraction for the multithreaded, multiplexed Java NIO socket
 * handling, for robustness, stability and performance.
 * 
 * @author nlost
 */
public class TcpipServer {
	
	private static final String TITLE = "TCP/IP Server";
	
	private static final int DEFAULT_PORT = 3000;
	
	private static final int LINEFEED = 0x0a;
	
	private static final int STX = 0x02;
	
	private static final int ETX = 0x03;
	
	private static final File MESSAGES_FILE = new File("TcpipServer_messages.txt");
	
	private static final int MAX_SAVED_MESSAGES = 20;
	
	private static final int APP_WIDTH = 900;  
	
	private static final int APP_HEIGHT = 700;  
	
	private static final Font CODE_FONT = new Font("Monospaced", Font.PLAIN, 12);
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
    /** Application state. */
	private enum State {
        
        /** Not running. */
        STOPPED,
        
        /** Running, but not connected. */
        ACCEPTING,
        
        /** Running and connected. */
        CONNECTED,
        
    };
    
	private int port;
	
	private ChannelFactory channelFactory;
	
	private ServerBootstrap bootstrap;
	
	private Channel serverChannel;

	private Channel clientChannel;

	private State state;
	
	private JFrame frame;
	
	private JTextField portText;
	
	private JRadioButton linefeedButton;
	
	private JRadioButton stxEtxButton;
	
	private JButton startStopButton;
	
	private JComboBox messagesComboBox;
	
	private DefaultComboBoxModel messages;
	
	private JButton sendButton;
	
	private JTextArea logText;
	
	public static void main(String[] args) {
		new TcpipServer();
	}

	public TcpipServer() {
		createUI();
		loadMessages();
		initTcpip();
		setState(State.STOPPED);
	}
	
	private void createUI() {
		frame = new JFrame(TITLE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
		frame.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JPanel serverPanel = new JPanel(new GridBagLayout());
		serverPanel.setBorder(new TitledBorder("Server"));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		frame.getContentPane().add(serverPanel, gbc);

		JLabel label = new JLabel("Port:");
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		serverPanel.add(label, gbc);

		portText = new JTextField(String.valueOf(DEFAULT_PORT));
		portText.setPreferredSize(new Dimension(75, 25));
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		serverPanel.add(portText, gbc);
		
		JPanel delimiterPanel = new JPanel();
		delimiterPanel.setBorder(new TitledBorder("Message Delimiter"));
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		serverPanel.add(delimiterPanel, gbc);

		linefeedButton = new JRadioButton("LineFeed");
		delimiterPanel.add(linefeedButton);
		
		stxEtxButton = new JRadioButton("STX/ETX");
		delimiterPanel.add(stxEtxButton, gbc);
		stxEtxButton.setSelected(true);
		
		ButtonGroup delimiterButtonGroup = new ButtonGroup();
		delimiterButtonGroup.add(linefeedButton);
		delimiterButtonGroup.add(stxEtxButton);
		
		startStopButton = new JButton("Start");
		startStopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startStop();
			}
		});
		gbc.gridx = 4;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		serverPanel.add(startStopButton, gbc);
		
		JPanel sendPanel = new JPanel(new GridBagLayout());
		sendPanel.setBorder(new TitledBorder("Send Message"));
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		frame.getContentPane().add(sendPanel, gbc);

		messages = new DefaultComboBoxModel();

		messagesComboBox = new JComboBox(messages);
		messagesComboBox.setFont(CODE_FONT);
		messagesComboBox.setEditable(true);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		sendPanel.add(messagesComboBox, gbc);

		sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendMessage();
			}
		});
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		sendPanel.add(sendButton, gbc);
		
		JPanel logPanel = new JPanel(new GridBagLayout());
		logPanel.setBorder(new TitledBorder("Message Log"));
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 4;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		frame.getContentPane().add(logPanel, gbc);

		logText = new JTextArea();
		logText.setEditable(false);
		logText.setFont(CODE_FONT);
		logText.setLineWrap(false);
		JScrollPane scrollPane = new JScrollPane(logText);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		logPanel.add(scrollPane, gbc);
		
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearLog();
			}
		});
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(5, 5, 5, 5);
		logPanel.add(clearButton, gbc);

		frame.setSize(APP_WIDTH, APP_HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private void setState(State state) {
		this.state = state;
		portText.setEditable(state == State.STOPPED);
		linefeedButton.setEnabled(state == State.STOPPED);
		stxEtxButton.setEnabled(state == State.STOPPED);
		if (state == State.STOPPED) {
			startStopButton.setText("Start");
		} else {
			startStopButton.setText("Stop");
		}
		messagesComboBox.setEditable(state == State.CONNECTED);
		sendButton.setEnabled(state == State.CONNECTED);
	}
	
	private void initTcpip() {
		// Create factory for server channels based on Java NIO.
		channelFactory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		
		// Create a server bootstrap.
		bootstrap = new ServerBootstrap(channelFactory);
        
        // Setup message handler for string messages.
        ChannelPipeline pipeline = bootstrap.getPipeline();
        pipeline.addLast("messageHandler", new MessageHandler());

        // Configure TCP/IP settings.
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
	}

	private void startStop() {
		if (state == State.STOPPED) {
		    // Start server.
			String portString = portText.getText();
			if (portString.isEmpty()) {
				log("[ERROR] Empty port");
			} else {
				try {
					int port = Integer.parseInt(portString);
					if (port >= 0 && port <= 65535) {
						this.port = port;
						accept();
				        log(String.format("Server started; listening on port %d", port));
					} else {
						log("[ERROR] Invalid port");
					}
				} catch (NumberFormatException e) {
					log("[ERROR] Invalid port");
				}
			}
		} else {
		    // Stop server.
			setState(State.STOPPED);
			disconnect();
			log("Server stopped");
		}
	}
	
	private void accept() {
		setState(State.ACCEPTING);
        serverChannel = bootstrap.bind(new InetSocketAddress(port));
	}
	
	private void disconnect() {
		// Stop listening for incoming connections.
		if (serverChannel != null) {
			serverChannel.close();
			serverChannel = null;
		}

		// Close client connection (if any).
		if (clientChannel != null) {
			clientChannel.close();
			clientChannel = null;
		}
	}
	
	private void sendMessage() {
		if (state == State.CONNECTED) {
			sendButton.setEnabled(false);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// Save message at the first position in the list.
					String message = (String) messages.getSelectedItem();
					messages.removeElement(message);
					messages.insertElementAt(message, 0);
					if (messages.getSize() > MAX_SAVED_MESSAGES) {
						messages.removeElementAt(MAX_SAVED_MESSAGES);
					}
					messages.setSelectedItem(message);
					
					// Send message.
					ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
					if (stxEtxButton.isSelected()) {
						buffer.writeByte((byte) STX);
					}
					try {
						buffer.writeBytes(message.getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						log("[ERROR] " + e.getMessage());
					}
					if (stxEtxButton.isSelected()) {
						buffer.writeByte((byte) ETX);
					} else {
						buffer.writeByte((byte) LINEFEED);
					}
					clientChannel.write(buffer);
					log(String.format("Sent:     '%s'", message));
					
					sendButton.setEnabled(true);
				}
			});
		}
	}
	
	private void log(String message) {
		String timestamp = DATE_FORMAT.format(new Date());
		
		logText.append(String.format("%s %s", timestamp, message));
		logText.append("\n");
		
		// Force JTextArea to scroll down to last line.
		logText.setCaretPosition(logText.getText().length());
	}
	
	private void clearLog() {
		logText.setText(null);
	}
	
	private void loadMessages() {
		messages.removeAllElements();
		if (MESSAGES_FILE.isFile()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(MESSAGES_FILE));
				String line = null;
				while ((line = reader.readLine()) != null) {
					messages.addElement(line);
				}
				reader.close();
			} catch (IOException e) {
				log("[ERROR] Could not read history file: " + e.getMessage());
			}
		}
	}

	private void saveMessages() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(MESSAGES_FILE));
			int count = messages.getSize();
			for (int i = 0; i < count; i++) {
				String text = (String) messages.getElementAt(i);
				writer.write(text);
				writer.write(LINEFEED);
			}
			writer.close();
		} catch (IOException e) {
			log("[ERROR] Could not write history file: " + e.getMessage());
		}
	}
	
	private void close() {
		disconnect();
		saveMessages();
	}
	

	/**
	 * ChannelHandler decoding incoming TCP/IP frames based on the selected
	 * message delimiter.
	 * 
	 * @author nlost
	 */
	private class MessageHandler extends FrameDecoder {
		
		private final ChannelBuffer messageBuffer = ChannelBuffers.dynamicBuffer();
		
		private boolean inMessage = false;

		@Override
		public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
			if (state == State.ACCEPTING && clientChannel == null) {
				clientChannel = ctx.getChannel();
				String client = clientChannel.getRemoteAddress().toString();
				log(String.format("Connected to client '%s'", client));
				
				// Stop listening for more connections.
				serverChannel.close();
				serverChannel = null;
				
				setState(State.CONNECTED);
			}
		}
		
		@Override
		public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
			log("Disconnected from client");
			
			if (state == State.CONNECTED) {
				// Restart server, listening for a new connection.
				disconnect();
				accept();
			}
		}
		
		@Override
		protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) {
			String message = null;
			while (buffer.readableBytes() > 0) {
				byte b = buffer.readByte();
				if (linefeedButton.isSelected()) {
					// LineFeed delimited messages.
					if (b == LINEFEED) {
						receiveMessage(messageBuffer.toString("UTF-8"));
					} else { 
						messageBuffer.writeByte(b);
					}
				} else {
					// ETX/STX delimited messages.
					if (b == STX) {
						if (!inMessage) {
							inMessage = true;
						} else {
							log("[WARN ] Unexpected STX received!");
						}
					} else if (b == ETX) {
						if (inMessage) {
							receiveMessage(messageBuffer.toString("UTF-8"));
							inMessage = false;
						} else {
							log("[WARN ] Unexpected ETX received!");
						}
					} else {
						if (inMessage) {
							messageBuffer.writeByte(b);
						}
					}
				}
			}
			return message;
		}
		
		private void receiveMessage(String message) {
			log(String.format("Received: '%s'", message));
			messageBuffer.clear();
		}
		
	}

}
