package testing;

import org.apache.log4j.Level;

import app_kvServer.KVServer;

import junit.framework.Test;
import junit.framework.TestSuite;
import logger.LogSetup;


public class AllTests {

	//server run in background for ConnectionTest
	public static KVServer M1Server;
	public static KVServer TestServer_Server1,TestServer_Server2;

	static {
		try {
			new LogSetup("logs/testing/test.log", Level.ERROR);
			M1Server = TestUtility.startServer(100, 500, "FIFO", false);

			TestServer_Server1 = TestUtility.startServer(103, 500, "FIFO", false);
			TestServer_Server2 = TestUtility.startServer(104, 500, "FIFO", false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static Test suite() {
		TestSuite clientSuite = new TestSuite("Basic Storage ServerTest-Suite");
		//		clientSuite.addTestSuite(InteractionTest.class);
//		 clientSuite.addTestSuite(AdditionalTest.class);
//		clientSuite.addTestSuite(TestServer.class);
//		 clientSuite.addTestSuite(TestDataManager.class);
		//clientSuite.addTestSuite(testing.PerformanceTest.class);

		//working tests---------------------------------------
		//KVStore-Server Connection Test
		clientSuite.addTestSuite(ConnectionTest.class);


		//Server Tests
		clientSuite.addTestSuite(testing.TestDisk.class);
		clientSuite.addTestSuite(testing.TestFIFOCache.class);
		clientSuite.addTestSuite(testing.TestServer.class);
		clientSuite.addTestSuite(testing.Test3ServerReplication.class);
		clientSuite.addTestSuite(testing.TestClientServerReplication.class);



		//Misc Tests
		clientSuite.addTestSuite(testing.TestMisc.class);


		//ECS-Server Tests
		//clientSuite.addTestSuite(TestECSToServer.class);

		return clientSuite;
	}
	
}