import Utils.Util;

/**
 * @author Qichao Ying
 * @date 2019/8/7 15:50
 * @Description DEFAULT
 */
public class MainExtract_excel {
    public static void main(String[] args) {

        String filename = "ta_cb_person_heatmap_collect.xls";

        String[] Keys = {"id", "name", "time", "phone", "date"};

        String wmStr = Util.readWatermark("src//ExcelWatermarkHelper.txt");
        int embedMsgLen = 0;

        String filePath = "src//resources//EXCEL//"+filename;
        ExcelDecoder extract = new ExcelDecoder();
        System.out.println("\n================= Extract from file " + "\"" + filePath + "\" =================");
        String[] extWmStr = extract.extract(filePath, embedMsgLen, Keys);

        if (extWmStr.length == 0)
            System.out.println("there is no ExcelWatermarkHelper embedded in this EXCEL file.");
        else {

        }
    }
}
