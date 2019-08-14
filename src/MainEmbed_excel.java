import Utils.Util;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Qichao Ying
 * @date 2019/8/7 15:37
 * @Description DEFAULT
 */
public class MainEmbed_excel {
    public static void main(String args[]){
//        String MODE = "EXTRACT";

        String filename = "ta_cb_person_heatmap_collect_deleted";
        String append = ".csv";

//        String[] Keys = {"id", "name", "time", "phone", "date"};

        String wmStr = Util.readWatermark("src//watermark.txt");
        String binarySeq = Util.StreamFromString(wmStr);
        System.out.println("Bit Num: "+binarySeq.length());

        System.out.println("embedded ExcelWatermarkHelper : " + wmStr);int embedMsgLen = 0;


        String filePath = "C:\\Users\\admin\\Desktop\\ta_cb_person_heatmap_collect_deleted.csv";
        ExcelEncoder embed = new ExcelEncoder(binarySeq,new File(filePath));
        try {
            FileOutputStream out = new FileOutputStream("src//embedded_results//" + filename + "_embedded" + append);
            embed.run(filePath, 1,out);
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }


}
