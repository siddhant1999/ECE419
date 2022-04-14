package testing;
import java.util.Random;
import client.KVStore;

public class TestUtility {

    public static void putXgetY(KVStore client, double decPUT, int totalRange, String cacheType) throws Exception{
        int numKeys = (int) (totalRange*decPUT);
        Random ran = new Random(1);
        long startTime = System.nanoTime();
        int percentPUT = (int) (decPUT*100);
        for ( int i = 0; i < numKeys ; i++) {
            client.put("key_"+Integer.toString(i), "val_"+Integer.toString(i));
        }
        for (int i = 0 ; i < totalRange-numKeys ; i++) {
            int rand = ran.nextInt(numKeys);
            client.get("key_"+Integer.toString(rand));
        }
        long endTime = System.nanoTime();
        System.out.println(cacheType + " Total Run Time of " + percentPUT + "% put, " + Integer.toString(100-percentPUT) +
                "% get: " + ((float)(endTime-startTime) / 1000000000) + "s");
    }
}
