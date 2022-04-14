package app_kvECS;

import java.io.*;
import java.util.*;
import java.net.Socket;

import java.util.Map;
import java.util.Collection;
import java.math.BigInteger;
import ecs.IECSNode;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ecs.ECSNode;
import shared.HashFunc;
import shared.Serializer;
import shared.SocketLib;
import shared.messages.KVAdminMessage;

import logger.LogSetup;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import testing.TestUtility;

public class ECSClient implements IECSClient {
	private boolean verbose = false;
	private static Logger logger = Logger.getRootLogger();
	private ZooKeeper zk;

	private int servers_online = 0; // TODO: DEPRECATE, not used in any meaningful functions

	private Stack<IECSNode> availablePorts;

	private static final String zk_address = "0.0.0.0:2181";
	private static final int zk_timeout = 2000;
	private final String zk_activeNodePath = "/activeNodes";
	private final String zk_crashedNodePath = "/crashedNodes";

	private CountDownLatch latch;

	public TreeMap<String, IECSNode> nodes;
	private TreeMap<String, SocketLib> sockets;

	private boolean network_shutdown = false;
	private BufferedReader stdin;

	private BigInteger zero = new BigInteger("0");
	private BigInteger one = new BigInteger("1");

	public static final String PROMPT = "ECSClient> ";

	// for testing
	private BufferedReader serverProcessInput = null;
	private BufferedReader serverProcessError = null;

	public boolean blockCrashDetection = false;

	public ECSClient(boolean startFailureDetection) {
		this.nodes = new TreeMap<String, IECSNode>();
		this.sockets = new TreeMap<String, SocketLib>();

		try {
			parseECSConfig();

			ProcessBuilder pd = new ProcessBuilder("zookeeper-3.4.11/bin/zkServer.sh", "stop");
			Process p = pd.start();

			ProcessBuilder pd2 = new ProcessBuilder("zookeeper-3.4.11/bin/zkServer.sh", "start");
			Process p2 = pd2.start();

			latch = new CountDownLatch(1);

			zk = new ZooKeeper(zk_address, zk_timeout, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
						latch.countDown();
					}
				}
			});
			latch.await();

			resetNodes();

			// create activeNode
			try {
				if (zk.exists(zk_activeNodePath, true) == null) {
					byte[] data = new byte[0];
					// TODO: may have to change data
					zk.create(zk_activeNodePath, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				if (zk.exists(zk_crashedNodePath, true) == null) {
					IECSNode dummy = new ECSNode("localhost", 0);
					// TODO: may have to change data
					zk.create(zk_crashedNodePath, Serializer.nodeToBytes(dummy), ZooDefs.Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
				}
			} catch (KeeperException e) {
				logger.error("Zookeeper error setting up ActiveNode: " + e);
			}

			if (startFailureDetection)
				new CrashDetection(this).start();
			logger.info("zookeeper connected at: " + zk_address);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void resetNodes() {
		try {
			if (zk.exists(zk_crashedNodePath, true) != null) {
				zk.delete(zk_crashedNodePath, -1);
			}
			for (IECSNode node : availablePorts) {
				try {
					if (zk.exists(node.getNodePath(), true) != null) {
						zk.delete(node.getNodePath(), -1);
					}
				} catch (KeeperException ke) {
					System.out.println("Unable to delete node: " + node.getNodeName());
					ke.printStackTrace();
				}

			}
		} catch (KeeperException ke) {
			System.out.println("Handle Server Crash Error: Keeper Exception");
			ke.printStackTrace();
		} catch (InterruptedException ie) {
			System.out.println("Handle Server Crash Error: Interrupted Exception");
			ie.printStackTrace();
		}
	}

	public void handleServerCrash(String serverHash) {
		// known that serverHash has crashed
		blockCrashDetection = true;
		System.out.println(nodes.get(serverHash).getNodePort() + " HAS CRASHED!");

		IECSNode crashedNode = nodes.get(serverHash);
		removeNodesSocket(crashedNode.getHashedName());
		updateAllNodeMetadata();

		// don't need to do anything, since all nodes have all data
		if (servers_online >= 3) {
			String c = crashedNode.getHashedName();
			String n = HashFunc.findNextLargest(c, nodes);
			String nn = HashFunc.findNextLargest(n, nodes);
			String nnn = HashFunc.findNextLargest(nn, nodes);
			String p = HashFunc.findPrev(c, nodes);
			String pp = HashFunc.findPrev(p, nodes);

			// same as remove node operations
			sendMoveMessage(pp, n, pp, false, false);
			sendMoveMessage(p, nn, p, false, false);
			// to continue replicating the data lost on the node that went down
			// this operation is performed automatically on moveData for removeNode
			sendMoveMessage(n, nnn, n, false, false);

		}
		try {
			zk.delete(crashedNode.getNodePath(), -1);
		} catch (KeeperException ke) {
			System.out.println("Handle Server Crash Error: Keeper Exception");
			ke.printStackTrace();
		} catch (InterruptedException ie) {
			System.out.println("Handle Server Crash Error: Interrupted Exception");
			ie.printStackTrace();
		}
		// restart a new node
		addNode("FIFO", 123);
		blockCrashDetection = false;
	}

	public void handleServerCrash() {
		try {
			if (zk.exists(zk_crashedNodePath, true) != null) {
				byte[] data = zk.getData(zk_crashedNodePath, new Watcher() {
					@Override
					public void process(WatchedEvent we) {
						handleServerCrash();
					}
				}, null);
				/*
				 * Callback function of type Watcher. The ZooKeeper ensemble will notify through
				 * the
				 * Watcher callback when the data of the specified znode changes. This is
				 * one-time notification.
				 * 
				 * 
				 */

				IECSNode crashedNode = Serializer.bytesToNode(data);
				// assume port is no longer usable
				if (crashedNode != null) {
					System.out.println("+++++++++++++++++++++++++");
					System.out.println("A NODE HAS CRASHED: " + crashedNode.getNodePort());
					// removeNodesSocket(crashedNode.getHashedName());
					// updateAllNodeMetadata();
					//
					// String c = crashedNode.getHashedName();
					// String n = HashFunc.findNextLargest(c, nodes);
					// String nn = HashFunc.findNextLargest(n,nodes);
					// String nnn = HashFunc.findNextLargest(n, nodes);
					// String p = HashFunc.findPrev(c, nodes);
					// String pp = HashFunc.findPrev(c, nodes);
					//
					// //remove node operations
					// sendMoveMessage(p, nn, p, false, false);
					// sendMoveMessage(pp,n,pp, false, false);
					// //to continue replicating the data lost on the node that went down
					// sendMoveMessage(n,nnn,c,false,false);
					// zk.delete(crashedNode.getNodePath(), -1);
					//
					//
					// //restart a new node
					// addNode("FIFO", 123);

				}
			}
		} catch (KeeperException ke) {
			System.out.println("Handle Server Crash Error: Keeper Exception");
			ke.printStackTrace();
		} catch (InterruptedException ie) {
			System.out.println("Handle Server Crash Error: Interrupted Exception");
			ie.printStackTrace();
		}
	}

	public void parseECSConfig() {
		try {
			Scanner scanner = new Scanner(new File("ecs.config"));
			availablePorts = new Stack<IECSNode>();
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] configLine = line.split(" ");
				availablePorts.push(new ECSNode("localhost", Integer.parseInt(configLine[2])));
			}
			if (verbose) {
				System.out.println("Available ports size: " + availablePorts.size());
				for (IECSNode e : availablePorts) {
					System.out.println(e.getNodePort());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean start() {
		if (nodes.size() == 0) {
			System.out.println(PROMPT + "No nodes to start...");
			return false;
		}
		for (Map.Entry<String, IECSNode> entry : nodes.entrySet()) {
			sendMessage(entry.getKey(), "START");

		}
		return true;
	}

	@Override
	public boolean stop() {
		if (nodes.size() == 0) {
			System.out.println(PROMPT + "No nodes to stop...");
			return false;
		}
		for (Map.Entry<String, IECSNode> entry : nodes.entrySet()) {
			sendMessage(entry.getKey(), "STOP");
		}
		return true;
	}

	@Override
	public boolean shutdown() {
		for (Map.Entry<String, IECSNode> entry : nodes.entrySet()) {
			sendMessage(entry.getKey(), "SHUT_DOWN");
			this.servers_online--;
		}

		sockets.clear();
		nodes.clear();

		network_shutdown = true;
		System.out.println(PROMPT + "Successfully shutdown");
		return true;
	}

	private void printServerSystemOut() {
		try {
			// Read the output from the command
			System.out.println("Server logs: \n");
			String s = null;
			while ((s = serverProcessInput.readLine()) != null) {
				System.out.println(s);
			}

			// Read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = serverProcessError.readLine()) != null) {
				System.out.println(s);
			}

		} catch (Exception e) {
			System.out.println("Cant read server System.out lines");
		}

	}

	/**
	 * This starts a new KVServer instance for the node to connect to
	 * 
	 * @param node_host
	 * @param node_port
	 * @param cacheStrategy
	 * @param cacheSize
	 */
	private void runSSH(String node_host, int node_port, String cacheStrategy, int cacheSize) {
		File f = new File("m2-server.jar");
		String path = System.getProperty("user.dir");

		String script = "nohup java -jar " +
				path + "/m2-server.jar " +
				node_port + " " + cacheSize + " " + cacheStrategy + " &";

		// String script = "ssh -n " + node_host + " nohup java -jar " +
		// path+"/m2-server.jar " +
		// node_port + " " + cacheSize + " " + cacheStrategy + " &";
		try {
			if (verbose)
				System.out.println("runSSH RUNNING SCRIPT-------");
			// System.out.println(script);

			Runtime run = Runtime.getRuntime();
			Process p = run.exec(script);

			p.waitFor(1, TimeUnit.SECONDS);
			if (serverProcessInput == null) {
				serverProcessInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				// String line;
				// while ((line = serverProcessInput.readLine()) != null) {
				// System.out.println(line);
				// }
				serverProcessError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				// while ((line = serverProcessError.readLine()) != null) {
				// System.out.println("ERR==>" + line);
				// }
			}

			if (verbose)
				System.out.println("runSSH FINISHED RUNNING SCRIPT-------");

			if (verbose)
				System.out.println("runSSh start server with: " + node_host + " " + node_port);

		} catch (Exception e) {
			logger.error(e);
		}
	}

	public SocketLib connectNewSocket(IECSNode node) {
		try {
			Socket clientSocket = new Socket(node.getNodeHost(), node.getNodePort());
			ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
			return new SocketLib(clientSocket, input, output);
		} catch (Exception e) {
			System.out.println("Connect new socket error ECSClient");
			e.printStackTrace();
		}
		return null;
	}

	private void addNodesSocket(String k, IECSNode node) {
		nodes.put(k, node);
		sockets.put(k, connectNewSocket(node));
		servers_online += 1;
	}

	private void removeNodesSocket(String k) {
		availablePorts.push(nodes.remove(k));
		sockets.remove(k);
		servers_online -= 1;
	}

	/**
	 *
	 * @param curr key of node added
	 */
	private void transferNodeDataOnAdd(String curr) {
		// get next largest node
		String n = HashFunc.findNextLargest(curr, nodes);
		if (servers_online == 1)
			return;

		// first clear all data that is in the node to ensure against failures or
		// anything
		sendDeleteMessage(curr, "");

		// transfer all the data from next Largest to current nodeK
		/*
		 * Need to send in message:
		 * -> new nodeK for knowing which one to connect to
		 * -> nodeK of node in which data is transfered from
		 * -> data moved from nextLargestK to nodeK
		 */

		sendMoveMessage(n, curr, curr, true, true);
		TestUtility.printPortFromHashName(curr, nodes);
		TestUtility.printPortFromHashName(n, nodes);
		if (servers_online == 2) {
			System.out.println("Triggered");
			sendMoveMessage(n, curr, n, false, false);
		} else if (servers_online == 3) {
			String nn = HashFunc.findNextLargest(n, nodes);
			sendMoveMessage(n, curr, n, false, false);
			sendMoveMessage(nn, curr, nn, false, false);
		} else if (servers_online >= 4) {
			String p = HashFunc.findPrev(curr, nodes);
			String pp = HashFunc.findPrev(p, nodes);
			String nn = HashFunc.findNextLargest(n, nodes);
			String nnn = HashFunc.findNextLargest(nn, nodes);

			// new node need to be updated with previous replicas
			sendMoveMessage(p, curr, p, false, false);
			sendMoveMessage(pp, curr, pp, false, false);
			// nodes ahead need previous node data deleted
			sendDeleteMessage(n, pp);
			sendDeleteMessage(nn, p);
			sendDeleteMessage(nnn, curr);
		}
	}

	/**
	 *
	 * @param curr key of node removed
	 */
	private void transferNodeDataOnRemove(String curr) {
		// get next largest node
		String n = HashFunc.findNextLargest(curr, nodes);
		if (n == null)
			return;
		// transfer all the data from nodeK to nextLargestK
		if (servers_online == 1) {
			// indicates only one node remaining, and should just keep data on that node
			return;
		}
		sendMoveMessage(curr, n, curr, true, true);
		if (servers_online >= 4) {
			// metadata is updated after, at this moment server has not been removed
			String p = HashFunc.findPrev(curr, nodes);
			String pp = HashFunc.findPrev(p, nodes);
			String nn = HashFunc.findNextLargest(n, nodes);

			sendMoveMessage(p, nn, p, false, false);
			sendMoveMessage(pp, n, pp, false, false);

		}
	}

	@Override
	public IECSNode addNode(String cacheStrategy, int cacheSize) {
		blockCrashDetection = true;
		try {

			// get node from available pool of nodes
			IECSNode node = availablePorts.pop();
			// return null if no nodes remaining
			if (node == null)
				throw new Exception("Unable to add new node: Ran out of ports");
			// run server
			runSSH(node.getNodeHost(), node.getNodePort(), cacheStrategy, cacheSize);
			// add node socket (updates metadata)
			addNodesSocket(node.getHashedName(), node);
			// transfer data
			updateAllNodeMetadata();
			transferNodeDataOnAdd(node.getHashedName());

			printNodes();
			// currently zookeeper is not used for anything
			// TODO: Check the zookeeper error; currently has no effect to adding/removing
			// node
			try {
				zk.create(node.getNodePath(), "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			} catch (Exception e) {
				System.out.println("Zookeeper has some exception");
				e.printStackTrace();
			}

			logger.info("new node at" + node.getNodePath());
			blockCrashDetection = false;
			return node;

		} catch (Exception e) {
			logger.error("Error adding new node!");
			e.printStackTrace();
			blockCrashDetection = false;
			return null;
		}
	}

	@Override
	public Collection<IECSNode> addNodes(int count, String cacheStrategy, int cacheSize) {
		System.out.println("addNode: " + count + " " + "cacheSize: " + cacheSize + " cacheStrat: " + cacheStrategy);
		ArrayList<IECSNode> new_nodes = new ArrayList<IECSNode>();
		while (count-- > 0) {
			if (availablePorts.empty()) {
				System.out.println("No more available server:ports to be added");
				return null;
			}
			new_nodes.add(addNode(cacheStrategy, cacheSize));
			// entries will be null when we run out of nodes to add
		}
		updateAllNodeMetadata(); // update metadata on the server to reflect changed
		// metadata here
		return new_nodes;
	}

	/**
	 * @param count   number of nodes to wait for
	 * @param timeout the timeout in milliseconds
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean awaitNodes(int count, int timeout) throws Exception {
		// TODO: the strat is actually to do nothing here, becuase actually awaiting is
		// going to add a painful amount of code
		long start_time = System.currentTimeMillis();

		System.out.println("awaitNode zk children: " + zk.getChildren(zk_activeNodePath, true));

		System.out.println("awaitNodes end");
		while (System.currentTimeMillis() - start_time < timeout)
			if (zk.getChildren(zk_activeNodePath, true).size() == servers_online + count)
				return true;
		return false;
	}

	@Override
	public boolean removeNodes(Collection<String> nodeNames) {
		blockCrashDetection = true;
		boolean success = true;
		Iterator<String> i = nodeNames.iterator();

		// while loop
		while (i.hasNext()) {
			String k = HashFunc.hashString(i.next());
			try {
				if (nodes.containsKey(k)) {
					// need to find the next largest to move all the data from teh current node to
					// the next node
					transferNodeDataOnRemove(k);
					// shut down the node
					sendMessage(k, "SHUT_DOWN");
					// need to update metadata AFTER data has been removed
					removeNodesSocket(k);
				} else
					success = false;
			} catch (Exception e) {
				System.out.println("Unable to Remove Node k: " + k);
				e.printStackTrace();
			}

			/*
			 * things that need to be done:
			 * 1) Move data from current node to the next largest node
			 * 2) Remove node from nodes and sockets
			 * 
			 * Corner case to handle:
			 * 1) Only one node remaining, what happens if we delete that node?
			 */

		}
		// update metadata of all nodes
		updateAllNodeMetadata();
		blockCrashDetection = false;
		return success;
	}

	public void updateAllNodeMetadata() {
		for (Map.Entry<String, SocketLib> entry : sockets.entrySet()) {
			sendMessage(entry.getKey(), "UPDATE");
			// System.out.println("SENT");
		}
	}

	@Override
	public Map<String, IECSNode> getNodes() {
		return nodes;
	}

	@Override
	public IECSNode getNodeByKey(String Key) {
		return nodes.get(Key);
	}

	private void closeSockets() {
		for (Map.Entry<String, SocketLib> entry : sockets.entrySet()) {
			tearDownConnection(entry.getKey());
		}
	}

	private void tearDownConnection(String k) {
		System.out.println("tearing down the connection ...");
		SocketLib sl = this.sockets.get(k);
		Socket clientSocket = sl.socket;
		ObjectInputStream input = sl.input;
		ObjectOutputStream output = sl.output;
		try {
			if (clientSocket != null) {
				input.close();
				output.close();
				clientSocket.close();
				this.sockets.put(k, null);
			}
		} catch (Exception e) {
			System.out.println("Failed to close connection");
		}
	}

	private void receiveMessage(String k, SocketLib nodeSocket) {
		KVAdminMessage kvAdminMessage = null;
		try {
			kvAdminMessage = (KVAdminMessage) nodeSocket.input.readObject();
		} catch (EOFException e) {
		} catch (IOException e) {
			System.out.println("Connection Lost");
			tearDownConnection(k);
		} catch (ClassNotFoundException e) {
			System.out.println("Class not KVAdminMessage");
		}

		System.out.print("Received Message from ECSNode: ");
		if (kvAdminMessage != null) {
			kvAdminMessage.printKVMessage();
		} else
			System.out.println("null");
	}

	private void sendMessage(String k, KVAdminMessage kvAdminMessage) {
		SocketLib nodeSocket = sockets.get(k);
		try {
			System.out.print(
					"Sending Message: " + kvAdminMessage.message + " to port " + nodes.get(k).getNodePort() + " ");
			nodeSocket.output.writeObject(kvAdminMessage);
			nodeSocket.output.flush();
			System.out.print("... Success \n");

		} catch (IOException e) {
			System.out.println("IOException sending message");
			e.printStackTrace();
			return;
		} catch (Exception e) {
			System.out.println("Other error has occured");
			e.printStackTrace();
			return;
		}
		receiveMessage(k, nodeSocket);
		return;
	}

	private void sendMoveMessage(String srcServerKey, String dstServerKey, String nextUpperBound,
			boolean delete, boolean replicate) {
		sendMessage(srcServerKey, new KVAdminMessage(
				"Move data from " + nodes.get(srcServerKey).getNodePort() + " to "
						+ nodes.get(dstServerKey).getNodePort(),
				KVAdminMessage.StatusType.MOVE, dstServerKey, nextUpperBound,
				new TreeMap<>(nodes), delete, replicate));
	}

	private void sendDeleteMessage(String srcServerKey, String nextUpperBound) {
		sendMessage(srcServerKey, new KVAdminMessage("Delete", KVAdminMessage.StatusType.DELETE,
				null, nextUpperBound, new TreeMap<>(nodes), false, false));
	}

	/**
	 *
	 * @param k       hashName of server to send the message to
	 *                for move data: hashName of server to move data from
	 * @param message
	 */
	private void sendMessage(String k, String message) {
		sendMessage(k, new KVAdminMessage(message,
				KVAdminMessage.StatusType.valueOf(message),
				new TreeMap<>(nodes)));
	}

	public static void main(String[] args) {
		try {
			new LogSetup("ecs.log", Level.INFO);
			ECSClient client = new ECSClient(true);
			client.run();
		} catch (IOException e) {
			System.out.println("Error! Unable to initialize logger!");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void run() {
		while (!network_shutdown) {
			stdin = new BufferedReader(new InputStreamReader(System.in));
			System.out.print(PROMPT);

			try {
				String cmdLine = stdin.readLine();
				this.handleCommand(cmdLine);
			} catch (IOException e) {
				network_shutdown = true;
				System.out.print("Fatal Error, Shutting Down...");
			}
		}
	}

	private void quit() {
		for (Map.Entry<String, IECSNode> entry : nodes.entrySet()) {
			IECSNode node = entry.getValue();
			System.out.println("Node Quit: " + node.getNodePath());
			try {
				sendMessage(entry.getKey(), "SHUT_DOWN");
				zk.delete(node.getNodePath(), -1);
				System.out.println("Node Quit Success: " + node.getNodePath());
			} catch (Exception e) {
				System.out.println("Node Quit Fail: " + node.getNodePath());
				e.printStackTrace();
			}
		}
		closeSockets();
		this.sockets = new TreeMap<String, SocketLib>();
		this.nodes = new TreeMap<String, IECSNode>();
	}

	private void printNodes() {
		System.out.println("++++++++++++++Printing Node Information++++++++++++++");
		System.out.println("Total Number of Nodes: " + nodes.size());
		System.out.println("Total Number of Sockets: " + sockets.size());
		for (Map.Entry<String, IECSNode> entry : nodes.entrySet()) {
			IECSNode node = entry.getValue();
			System.out.println("Node: " + node.getNodeName() + " | HashedName: " + node.getHashedName());

		}
		System.out.println("++++++++++++++Finished Printing Node Information++++++++");
	}

	private void handleCommand(String cmdLine) {
		String[] tokens = cmdLine.split("\\s+");

		switch (tokens[0]) {
			case "add":
				if (tokens.length != 4) {
					System.out.println(PROMPT + "Usage: add <number of nodes> <cache size> <cache strategy>");
					break;
				}
				try {
					addNodes(Integer.parseInt(tokens[1], 10), tokens[3], Integer.parseInt(tokens[2], 10));

				} catch (NumberFormatException e) {
					System.out.println(PROMPT + "Usage: add <number of nodes> <cache size> <cache strategy>");

				}
				break;

			case "remove":
				if (tokens.length < 2) {
					System.out.println(PROMPT + "Usage: remove <node name> <node name> ...");
					break;
				} else {
					try {
						for (int i = 1; i < tokens.length; i++) {
							String bi = tokens[i];
							removeNodes(new ArrayList<>(Arrays.asList(bi)));
							System.out.println(PROMPT + "Successfully removed node: " + tokens[i]);
						}
					} catch (Exception e) {
						System.out.println("Remove not successful.");
						e.printStackTrace();
					}
				}
				break;

			case "list":

				if (nodes.size() == 0) {
					System.out.println(PROMPT + "0 nodes");
					break;
				}
				printNodes();
				break;

			case "shutdown":
				shutdown();
				break;

			case "start":
				start();
				break;

			case "stop":
				stop();
				break;

			case "q":
				quit();
				break;

			case "ps":
				printServerSystemOut();
				break;
			case "update":
				updateAllNodeMetadata();
				break;

			default:
				System.out.println(PROMPT + "Unknown command");
				break;
		}
	}

}
