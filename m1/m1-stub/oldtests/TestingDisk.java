package testing;

import app_kvServer.KeyValue;
import app_kvServer.cache.Disk;
import junit.framework.TestCase;
import org.junit.Test;


public class TestingDisk extends TestCase {
	private Disk disk;

	
	public void setUp(){
		this.disk = new Disk();
	}
	// private void testWritingDisk(){
	public void testWritingDisk(){
		String key = "Key";
		String value = "Key_read";
		KeyValue KV = new KeyValue(key, value);
		String returnPath = this.disk.putOnDisk(KV);
		System.out.println(returnPath);

		String key1 = "Key1";
		String value1 = "Key1_read";
		KeyValue KV1 = new KeyValue(key1, value1);
		String returnPath1 = this.disk.putOnDisk(KV1);
		System.out.println(returnPath1);

		String key3 = "Key3";
		String value3 = "Key3_read";
		KeyValue KV3 = new KeyValue(key3, value3);
		String returnPath3 = this.disk.putOnDisk(KV3);
		System.out.println(returnPath3);

		String key2 = "jey1";
		String value2 = "jey1_read";
		KeyValue KV2 = new KeyValue(key2, value2);
		String returnPath2 = this.disk.putOnDisk(KV2);
		System.out.println(returnPath2);
	}

	// private void testReadingDisk(){
	public void testReadingDisk(){
		// System.out.println("inside read");
		String key = "Key";
		String returnPath = this.disk.getFromDisk(key);
		System.out.println(returnPath);

		String key1 = "Key1";
		String returnPath1 = this.disk.getFromDisk(key1);
		System.out.println(returnPath1);

		String key4 = "Key3";
		String returnPath4 = this.disk.getFromDisk(key4);
		System.out.println(returnPath4);

		String key2 = "jey1";
		String returnPath2 = this.disk.getFromDisk(key2);
		System.out.println(returnPath2);

		String key3 = "pey1";
		String returnPath3 = this.disk.getFromDisk(key3);
		System.out.println("pey 1 " + returnPath3);
	}

	// private void testDeleting() {
	public void testDeleting() {
		String key2 = "Key1";
		String value2 = null;
		KeyValue KV2 = new KeyValue(key2, value2);
		String returnPath2 = this.disk.putOnDisk(KV2);
		System.out.println("rpath = " + returnPath2);
	}

	// private void testReplacing() {
	public void testReplacing() {
		String key2 = "Key";
		String value2 = "NOTVALUE";
		KeyValue KV2 = new KeyValue(key2, value2);
		String returnPath2 = this.disk.putOnDisk(KV2);
		System.out.println("rpath = " + returnPath2);
	}

	public void readDisk(){
		this.disk.readDisk("K");
	}

	@Test
	public void testAll() {
		System.out.println(" ######################### TESTING DISK ######################### ");
		testWritingDisk();
		System.out.println("---------- writeen");
		// td.testReadingDisk();
		readDisk();
		System.out.println("---------- delete");
		testDeleting();
		// td.testReadingDisk();
		System.out.println("---------- replacing");
		testReplacing();
		// td.readDisk();
		// System.out.println("----------");
		// td.testReadingDisk();
	}

}
