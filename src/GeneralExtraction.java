import Setting.Settings;
import Utils.Util;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Qichao Ying
 * @date 2019/8/16 12:32
 * @Description DEFAULT
 */
public class GeneralExtraction {

    public static void main(String[] s) {
        // For Comparison Only
        String wmStr = Util.readWatermark("src//watermark.txt");
        List<String> list = new LinkedList<>();
        List<String> chinese_list = new LinkedList<>();
        int[] args = new int[]{};
        String filename = "1";//ta_cb_person_heatmap_collect_deleted
        String append = ".xlsx";
        String filePath = "src//embedded_results//"+filename+"_embedded"+append;
        try {
            if(append.equals(".xlsx") || append.equals(".xls") || append.equals(".csv")) {
                System.out.println("[Service Started] Excel watermarking detected...");


                //CSV默认第一行是无效信息，第二行是表头，信息从第三行开始(使用嵌入csv的数据包会这样，对于一般的csv也可能没有第一行的无效信息)
                // xls默认第一行表头，第二行开始是信息
                int startRow = (append.equals(".csv")) ? 2 : 1;
    //        String[] Keys = {"id", "name", "time", "phone", "date"};


                ExcelDecoder extract = new ExcelDecoder(new File(filePath), startRow,0.05, Settings.LONG);
                System.out.println("\n================= Extract from file " + "\"" + filePath + "\" =================");

                    extract.run(filePath,args);

                    list = extract.getEnglishResult();
                    chinese_list = extract.getChineseResult();

            }else if(append.equals(".json")) {
                JsonParser parser = new JsonParser();
                String watermark = Util.readWatermark("src/ExcelWatermarkHelper.txt");
                String emb_path = "src//embedded_results//" + filename + "_data_" + watermark + ".json";
                JsonObject object = (JsonObject) parser.parse(new FileReader(emb_path));

                JSONDecoder decoder = new JSONDecoder();
                decoder.run(object,emb_path);

                list = decoder.getEnglishResult();
                chinese_list = decoder.getEnglishResult();

            }

            printer.print(list,chinese_list);
//            System.out.println("-----------提取得到的信息是------------");
//            //打印提取结果
//            System.out.println("The ExcelWatermarkHelper is SUCCESSFULLY retrieved " + list.size() + " time(s)！");
//            for (String str : list) {
//                System.out.println(str);
//            }
//
//            System.out.println("----如果您发现上面的解析内容是乱码，那么也可以参考以下gbk中文解码的水印内容----");
//            for (String str : chinese_list) {
//                System.out.println(str);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
