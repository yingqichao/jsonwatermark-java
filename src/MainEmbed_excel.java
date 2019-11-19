import Utils.Util;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Qichao Ying
 * @date 2019/8/7 15:37
 * @Description DEFAULT
 */
public class MainEmbed_excel {
    public static boolean Embed(String foldname,String filename,String append,String waterPath,int[] args){
        //input:foldname 载体文件夹 filename 载体名称 append 载体后缀名 waterPath 水印文件txt位置 args 不允许做嵌入的列indices
        //return: 是否嵌入成功
        int argsLen = args.length;
        String wmStr = Util.readWatermark("src//watermark.txt");
        String binarySeq = Util.StreamFromString(wmStr);
        System.out.println("Bit Num: "+binarySeq.length());

        System.out.println("embedded ExcelWatermarkHelper : " + wmStr);

        String filePath = "src//resources//"+foldname+"//"+filename+append;
        System.out.println("\n================= Embed from file " + "\"" + filePath + "\" =================");
        ExcelEncoder embed = new ExcelEncoder(binarySeq,filePath,1);
        try {
//            FileOutputStream out = new FileOutputStream("src//embedded_results//" + filename + "_embedded" + append);
            embed.run(filePath,"src//embedded_results//" + filename + "_embedded" + append,args);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }

    }

    public static void main(String s[]){
//        String MODE = "EXTRACT";

        int[] args = new int[]{};
        String foldname = "EXCEL";
        String filename = "test4";
        String append = ".xls";
        String waterPath = "src//watermark.txt";
        Embed(foldname,filename,append,waterPath,args);

    }


}

