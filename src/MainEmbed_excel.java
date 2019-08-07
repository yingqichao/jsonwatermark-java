import Utils.Util;

/**
 * @author Qichao Ying
 * @date 2019/8/7 15:37
 * @Description DEFAULT
 */
public class MainEmbed_excel {
    public static void main(String args[]){
//        String MODE = "EXTRACT";

        String filename = "ta_cb_person_heatmap_collect.xls";

        String[] Keys = {"id", "name", "time", "phone", "date"};

        String wmStr = Util.readWatermark("src//watermark.txt");
        String binarySeq = Util.StreamFromString(wmStr);
        System.out.println("Bit Num: "+binarySeq.length());

        System.out.println("embedded watermark : " + wmStr);int embedMsgLen = 0;


        String filePath = "src//resources//EXCEL//"+filename;
        excel_embedding embed = new excel_embedding();


        // 不允许修改字段，对大小写不敏感


        System.out.println("\n================= Test for file " + "\"" + filePath + "\" =================");
        embedMsgLen = embed.embed(filePath, wmStr, Keys);
        if (embedMsgLen == -1) {
            System.out.println("embedding failed in " + filePath);
        }
    }


}
