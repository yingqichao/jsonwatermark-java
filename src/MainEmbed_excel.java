/**
 * @author Qichao Ying
 * @date 2019/8/7 15:37
 * @Description DEFAULT
 */
public class MainEmbed_excel {
    public static void main(String args[]){
        String MODE = "EXTRACT";

        String[] Keys = {"id", "name", "time", "phone", "date"};

        String wmStr = "一个测试abc!";
        System.out.println("embedded watermark : " + wmStr);int embedMsgLen = 0;


        if(MODE=="BOTH" || MODE=="EMBED" ) {
            String filePath = "C:\\Users\\admin\\Desktop\\ta_cb_person_heatmap_collect.xls";
            excel_embedding embed = new excel_embedding();


            // 不允许修改字段，对大小写不敏感



            System.out.println("\n================= Test for file " + "\"" + filePath + "\" =================");
            embedMsgLen = embed.embed(filePath, wmStr, Keys);
            if (embedMsgLen == -1) {
                System.out.println("embedding failed in " + filePath);
            }
        }

        if(MODE=="BOTH" || MODE=="EXTRACT" ) {
            String filePath = "C:\\Users\\admin\\Desktop\\ta_cb_person_heatmap_collect_deleted.xls";
            excel_extraction extract = new excel_extraction();
            System.out.println("\n================= Extract from file " + "\"" + filePath + "\" =================");
            String[] extWmStr = extract.extract(filePath, embedMsgLen, Keys);

            if (extWmStr.length == 0)
                System.out.println("there is no watermark embedded in this EXCEL file.");
            else {
                for (String wm : extWmStr) {
                    System.out.println("extracted watermark : " + wm);
                    int cnt = 0;
                    for (int i = 0; i < extWmStr.length; i++) {
                        cnt = cnt + (wmStr.equals(wm) ? 0 : 1);
                    }
                    System.out.println("******* There are " + cnt + " bits error");
                }
            }
        }
    }
}
