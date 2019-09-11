import GeneralHelper.LtDecoder;
import Setting.Settings;
import Utils.Util;
import com.google.gson.*;

import java.util.*;

public abstract class AbstractDecoder {
    public int success_time = 0;
    public LtDecoder decoder = null;
    public List<String> secret_data = new LinkedList<>();
    public List<String> secret_data_chinese = new LinkedList<>();
    public int minRequire;

    public List<String> getChineseResult(){
        return this.secret_data_chinese;
    }

    public List<String> getEnglishResult(){
        return this.secret_data;
    }

}
