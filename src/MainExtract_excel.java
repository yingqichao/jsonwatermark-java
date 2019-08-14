import Utils.Util;

import java.io.File;
import java.util.List;

/**
 * @author Qichao Ying
 * @date 2019/8/7 15:50
 * @Description DEFAULT
 */
public class MainExtract_excel {
    public static void main(String[] args) {

        String filename = "ta_cb_person_heatmap_collect_deleted";String append = ".csv";

        String[] Keys = {"id", "name", "time", "phone", "date"};

        String wmStr = Util.readWatermark("src//watermark.txt");
        int embedMsgLen = 0;

        String filePath = "src//embedded_results//"+filename+"_embedded"+append;
        ExcelDecoder extract = new ExcelDecoder(new File(filePath));
        System.out.println("\n================= Extract from file " + "\"" + filePath + "\" =================");
        try {
            List<String> list = extract.run(filePath, 1, 2*wmStr.length());


            System.out.println("-----------提取得到的信息是------------");
            //打印提取结果
            System.out.println("The ExcelWatermarkHelper is SUCCESSFULLY retrieved "+list.size()+" time(s)！");
            for(String str:list){
                System.out.println(str);
            }

            System.out.println("----如果您发现上面的解析内容是乱码，那么也可以参考以下gbk中文解码的水印内容----");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
