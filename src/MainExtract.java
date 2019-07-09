import java.util.*;

public class MainExtract {
    public static void main(String[] args){

        Map<String,String> JSON = Util.readFakeJSON("src/watermarkedJSON.txt");
        String watermark = Util.readWatermark("src/watermark.txt");
        Decoder dec = new Decoder();
        List<String> list = dec.run(JSON, watermark.length());

        System.out.println("-----------提取得到的信息是------------");
        //打印提取结果
        for(String str:list){
            System.out.println(str);
        }

    }
}
