import GeneralHelper.LtDecoder;
import Setting.Settings;
import Utils.Util;
import com.google.gson.*;

import java.util.*;

public abstract class AbstractDecoder {
    public int success_time = 0;
    public LtDecoder decoder = null;
    public List<String> secret_data = new LinkedList<>();


}
