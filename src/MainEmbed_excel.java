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

        String foldname = "EXCEL";
        String filename = "1";
        String append = ".xlsx";
        String wmStr = Util.readWatermark("src//watermark.txt");
        String binarySeq = Util.StreamFromString(wmStr);
        System.out.println("Bit Num: "+binarySeq.length());

        System.out.println("embedded ExcelWatermarkHelper : " + wmStr);

        String filePath = "src//resources//"+foldname+"//"+filename+append;
        ExcelEncoder embed = new ExcelEncoder(binarySeq,filePath,1);
        try {
//            FileOutputStream out = new FileOutputStream("src//embedded_results//" + filename + "_embedded" + append);
            embed.run(filePath,"src//embedded_results//" + filename + "_embedded" + append);
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }


}
