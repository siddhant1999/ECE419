package app_kvServer;

import java.io.Serializable;

public class KeyValue implements Serializable {
    private String key;
    private String value;

    public KeyValue(String key, String value){
        this.key = key;
        this.value = value;
    }

    public KeyValue getKeyValue(){
        return this;
    }

    public String getValue(){
        return this.value;
    }

    public String getKey(){
        return this.key;
    }

    public void setValue(String value){
        this.value = value;
    }

}
