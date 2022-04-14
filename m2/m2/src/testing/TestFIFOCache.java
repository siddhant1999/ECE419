package testing;
import app_kvServer.cache.CacheImplement;
import junit.framework.TestCase;
import org.junit.Test;


public class TestFIFOCache extends TestCase {
    private CacheImplement cache;
    public void setUp(){
        cache = new CacheImplement("FIFO", 3);
    }

    @Test
    public void testPutGet(){
        String key = "foo";
        String value = "bar";
        Exception ex = null;
        String getVal = null;
        try {
            cache.put(key,value);
            getVal = cache.get(key);
        } catch (Exception e) {
            ex = e;
        }
        assertTrue(ex==null && getVal.equals(value));
    }

    @Test
    public void testUpdate(){
        String key = "fooUpdate";
        String value = "bar";
        String value2 = "bar2";
        Exception ex = null;
        String getVal = null;
        try {
            cache.put(key, value);
            cache.put(key, value2);
            getVal = cache.get(key);
        } catch (Exception e) {
            ex = e;
        }
        assertTrue(ex==null && getVal.equals(value2));
    }

    @Test
    public void testDelete(){
        String key = "fooDelete";
        String value = "bar";
        Exception ex = null;
        String getVal = null;
        try {
            cache.put(key, value);
            cache.put(key, "null");
            getVal = cache.get(key);
        } catch (Exception e) {
            ex = e;
        }
        assertTrue(ex.getMessage().equals("Key not in cache") && getVal==null);
    }

    @Test
    public void testStrategy(){
        Exception ex = null;
        Boolean correct = true;
        try{
            cache.clearCache();
            for (int i=0; i < 5; i++){
                // 0 1 2 3 4
                //only 2 3 4 should be in cache
                cache.put("key"+Integer.toString(i),"val"+Integer.toString(i));
            }
            for (int i=0; i<5; i++) {
                if (i<2){
                    correct &= !cache.inCache("key"+Integer.toString(i));
                } else {
                    correct &= cache.inCache("key"+Integer.toString(i));
                }
            }
        } catch (Exception e){
            ex = e;
        }
        assert (ex==null && correct);
    }

}
