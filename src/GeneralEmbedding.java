import Utils.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileOutputStream;
import java.io.FileReader;

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
            ExcelEncoder embed = new ExcelEncoder(binarySeq, filePath, 1);
            try {
//            FileOutputStream out = new FileOutputStream("src//embedded_results//" + filename + "_embedded" + append);
                embed.run(filePath, "src//embedded_results//" + filename + "_embedded" + append,args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(append.equals(".json")){
            System.out.println("[Service Started] JSON watermarking detected...");
            JsonParser parser = new JsonParser() ;
            JsonObject object = (JsonObject)parser.parse(new FileReader("src//resources//JSON//"+filename+".json"));
            JSONEncoder encoder = new JSONEncoder(binarySeq);
            JsonElement jsonElement = encoder.run(object);

            FileOutputStream out=new FileOutputStream("src//embedded_results//"+filename+"_embedded"+".json");
            Util.writeJsonStream(out,jsonElement);
        }else throw new Exception("Unsupportted type of file");
    }

}
