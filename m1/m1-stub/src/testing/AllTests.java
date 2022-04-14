package testing;

import java.io.IOException;

import org.apache.log4j.Level;

import app_kvServer.KVServer;

//  import testing.TestingCache;
//  import testing.TestingDisk;
//  import testing.TestingFIFO;
//  import testing.TestingLFU;
//  import testing.TestingLRU;

import junit.framework.Test;
import junit.framework.TestSuite;
import logger.LogSetup;


public class AllTests {

	public static KVServer fifoServer;
//	public static KVServer lfuServer;
//	public static KVServer lruServer;

	static {
		try {
			new LogSetup("logs/testing/test.log", Level.ERROR);
			fifoServer = new KVServer(50000, 500, "FIFO");
//			lfuServer = new KVServer(50001, 100, "LFU");
//			lruServer = new KVServer(50002, 100, "LRU");
			fifoServer.start();
//			lfuServer.start();
//			fifoServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static Test suite() {
		TestSuite clientSuite = new TestSuite("Basic Storage ServerTest-Suite");
		clientSuite.addTestSuite(TestServer.class);
		// clientSuite.addTestSuite(ConnectionTest.class);
		// clientSuite.addTestSuite(InteractionTest.class);
		// clientSuite.addTestSuite(AdditionalTest.class);
		// clientSuite.addTestSuite(TestingCache.class);
		// clientSuite.addTestSuite(TestingDisk.class);
		// clientSuite.addTestSuite(TestingFIFO.class);
		// clientSuite.addTestSuite(TestingLFU.class);
		// clientSuite.addTestSuite(TestingLRU.class);
		// clientSuite.addTestSuite(TestDisk.class);
		return clientSuite;
	}
	
}