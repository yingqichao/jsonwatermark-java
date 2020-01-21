import Utils.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import static Setting.Settings.LONG;

/**
 * @author Qichao Ying
 * @date 2019/8/16 12:32
 * @Description DEFAULT
 */
public class GeneralEmbedding {

    public static void main(String[] s) throws Exception {
        String foldname = "EXCEL";
        String filename = "1";
        String append = ".xlsx";
        String wmStr = Util.readWatermark("src//watermark.txt");
        int[] args = new int[]{};
        String binarySeq = Util.StreamFromString(wmStr);
        System.out.println("Bit Num: "+binarySeq.length());
        System.out.println("embedded watermark : " + wmStr);
        String filePath = "src//resources//"+foldname+"//"+filename+append;
        System.out.println("\n================= Embed from file " + "\"" + filePath + "\" =================");

        if(append.equals(".xlsx") || append.equals(".xls") || append.equals(".csv")){
            System.out.println("[Service Started] Excel watermarking detected...");
            ExcelEncoder embed = new ExcelEncoder(binarySeq, filePath, 1,0.05,LONG);
            try {
//            FileOutputStream out = new FileOutputStream("src//embedded_results//" + filename + "_embedded" + append);
                embed.run(filePath, "src//embedded_results//" + filename + "_embedded" + append,args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(append.equals(".json")){
            String[] ban = new String[]{"logDataeventInfodetectStart"};
            Set<String> banList = new HashSet<>();
            for(String str:ban)
                banList.add(str);

            System.out.println("[Service Started] JSON watermarking detected...");
            JsonParser parser = new JsonParser() ;
            String emb_path = "src//resources//JSON//"+filename+".json";
            JsonObject object = (JsonObject)parser.parse(new FileReader(emb_path));
            JSONEncoder encoder = new JSONEncoder(binarySeq);
            encoder.run(object,emb_path,banList);

//            FileOutputStream out=new FileOutputStream("src//embedded_results//"+filename+"_embedded"+".json");
//            Util.writeJsonStream(out,jsonElement);
        }else throw new Exception("Unsupportted type of file");
    }

}
