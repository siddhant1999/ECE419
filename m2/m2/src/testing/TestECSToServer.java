package testing;

import app_kvServer.KVServer;
import app_kvServer.KeyValue;
import app_kvServer.cache.Disk;
import client.KVStore;
import ecs.IECSNode;
import junit.framework.TestCase;
import shared.HashFunc;
import shared.Metadata;

import java.beans.Transient;
import java.math.BigInteger;
import java.util.TreeMap;

import org.junit.Test;

import ecs.ECSNode;


public class TestECSToServer extends TestCase {
//	KVServer server;
//	KVStore tmpClient;

	public void setUp(){
//		try {
//			// System.out.println("started server");
//			TreeMap<BigInteger, IECSNode> ecsMetadata = new TreeMap<BigInteger, IECSNode>();
//			BigInteger hashIndex = HashFunc.hashString("127.0.0.1:55550"); // not the real hashindex but for testing purposes
//			BigInteger zero = new BigInteger("00000000000000000000000000000000", 16);
//			// Integer fs = Integer.parseInt("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
//			BigInteger fs = new BigInteger("ffffffffffffffffffffffffffffffff", 16);
//			String[] hRange = {zero.toString(), fs.toString()};
//			ECSNode node = new ECSNode(hRange[1], "127.0.0.1", 55550, hRange);
//			ecsMetadata.put(hashIndex, node);
//
//			// System.out.println("started ecs client");
//			this.tmpClient = new KVStore("127.0.0.1", 55550);
//			// System.out.println("here ");
//			// this.server.connectECS();
//			// System.out.println("after ecs");
//			this.tmpClient.connect();
//			// System.out.println("connected ecs client");
//			server = AllTests.fifoServer;
//			this.server.initKVServer(ecsMetadata);
//			this.server.startServer();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
//
//	public void tearDown() {
//		this.server.clearStorage();
//	}
//
//	@Test
//	public void test() {
//		return;
//	}
//
//	@Test
//	public void testPutGet() {
//		Exception ex = null;
//		Boolean instorage = null;
//		try {
//			// this.server.putKV("foo", "value");
//			this.tmpClient.put("foo", "value");
//			instorage = this.server.inStorage("foo");
//		} catch (Exception e) {
//			ex = e;
//		}
//		// System.out.println("error -- " + ex);
//		assertTrue(ex == null && (instorage.equals(true) || instorage == true));
//	}
//
//	@Test
//	public void testLocks(){
//		Exception ex = null;
//		Boolean wl = null;
//		try {
//			this.server.writeLock();
//			wl = this.server.writeLock;
//			this.server.unLockWrite();
//		} catch (Exception e) {
//			ex = e;
//		}
//
//		assertTrue(ex == null && wl == true);
//	}
//
//	@Test
//	public void testLockWrite() {
//		Exception ex = null;
//		try {
//			this.server.writeLock();
//
//			this.server.unLockWrite();
//		} catch (Exception e) {
//			ex = e;
//		}
//
//		assertTrue(ex == null);
//	}
//
//	@Test
//	public void testUpdateMetadata() {
//		TreeMap<String, IECSNode> newEcsMetadata = new TreeMap<String, IECSNode>();
//		BigInteger hashIndex = null;
//		try {
//			hashIndex = HashFunc.hashString("127.0.0.1:55550"); // not the real hashindex but for testing purposes
//		} catch (Exception e) {
//			//TODO: handle exception
//		}
//		BigInteger zero = new BigInteger("00000000000000000000000000000000", 16);
//		BigInteger fs = new BigInteger("3e8", 16);
//		String[] hRange = {zero.toString(), fs.toString()};
//		ECSNode node = new ECSNode(hRange[1], "127.0.0.1", 55550, hRange);
//		newEcsMetadata.put(hashIndex, node);
//
//		this.server.update(newEcsMetadata);
//
//		TreeMap<BigInteger, Metadata> serverMd = new TreeMap<BigInteger, Metadata>();
//		serverMd = this.server.getMetadata();
//
//		// System.out.println("SERVER END HASH " + serverMd.get(hashIndex).getEndHash());
//		assertTrue(serverMd.get(hashIndex).getEndHash().compareTo(fs) == 0 );
//
//	}

}
