import java.util.HashMap;


public class ID_passwords {

    HashMap<String,String> loginfo = new HashMap<String,String>();

    ID_passwords(){
        loginfo.put("KG","0928");
        loginfo.put("JK","1234");
        loginfo.put("KD","4321");
    }
    protected HashMap getLogInfo(){
        return loginfo;
    }
}