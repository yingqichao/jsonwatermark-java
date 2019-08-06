import watermark.utils.WatermarkUtils;

import java.io.File;
import java.util.List;

import static java.lang.Math.min;

public class excel_embedding {
    /*
     * 对filePath的Excel文件的所有sheet中的浮点数列进行嵌入
     * @param filePath : Excel 文件路径
     * @param wmBin : 二进制的水印信息
     * @return : 返回嵌入的水印信息长度，嵌入失败返回 -1
     */
    public static int embed(String filePath, String wmStr, String[] Keys){
        int msgLen = -1;

        WatermarkUtils embeddingUint = new WatermarkUtils(new File(filePath));
        //List<Integer> wmBin = embeddingUint.String2Bin(wmStr);
        List<Integer> wmBin = embeddingUint.String2Bin(wmStr);
        List<Integer> wmInt = embeddingUint.bin2Int(wmBin);
        //int msgLen = wmBin.size();
        //List<Integer> wmInt = embeddingUint.bin2Int(wmBin); // 将二进制水印转化成数字，方便嵌入
        msgLen = wmBin.size();

        List<List<Integer>> validCol = embeddingUint.findEmbeddingCols(Keys); // 寻找所有可能嵌入的浮点数列
        int cnt = 0;
        for(int sheet = 0; sheet < validCol.size(); sheet++){
            if(validCol.get(sheet).size() == 0) {
                System.out.println("Warning : sheet of " + embeddingUint.getSheetName(sheet) + " has no float column for watermark embedding.");
                cnt++;
            }
        }
        if(cnt == validCol.size()){
            System.out.println("Warning : EXCEL file of " + filePath + " has no space for watermark embedding.");
            return -1;
        }

        for(int sheet = 0; sheet < validCol.size(); sheet++){
            for(int col = 0; col < validCol.get(sheet).size(); col++){
                System.out.println("Embedding : " +
                        validCol.get(sheet).get(col) + " col  in sheet " +
                        "\"" + embeddingUint.getSheetName(sheet) + "\"" +
                        "is being embedded");
                embeddingUint.embed2OneCol(sheet, validCol.get(sheet).get(col), wmInt, col); //对一列进行嵌入
                System.out.println("Embedding : " +
                        validCol.get(sheet).get(col) + " col  in sheet " +
                        "\"" + embeddingUint.getSheetName(sheet) + "\"" +
                        "is embedded succeed");
            }
        }

        return  msgLen;
    }

    /*
     * 对filePath的Excel文件的指定sheet中的前几个浮点数列进行嵌入
     * @param filePath : Excel 文件路径
     * @param wmBin : 二进制的水印信息
     * @param sheetIndex : 需要嵌入的sheet的索引下标，从0开始
     * @param embedColNum : 需要嵌入的浮点数列的个数，选择最前面的开始嵌入
     * @return : 返回嵌入的水印信息长度，嵌入失败返回 -1
     */
    public static int embed(String filePath, String wmStr, int sheetIndex, int embedColNum, String [] Keys){
        int msgLen = -1;

        WatermarkUtils embeddingUint = new WatermarkUtils(new File(filePath));
        List<Integer> wmBin = embeddingUint.String2Bin(wmStr);
        List<Integer> wmInt = embeddingUint.bin2Int(wmBin);
        //int msgLen = wmBin.size();
        //List<Integer> wmInt = embeddingUint.bin2Int(wmBin); // 将二进制水印转化成数字，方便嵌入
        msgLen = wmBin.size();

        List<List<Integer>> validCol = embeddingUint.findEmbeddingCols(Keys);
        for(int col = 0; col < min(embedColNum, validCol.get(sheetIndex).size()); col++){
            embeddingUint.embed2OneCol(sheetIndex, validCol.get(sheetIndex).get(col), wmInt, 1);
        }

        return  msgLen;
    }

    public List<Integer> getRandomMsg(int seed, int length){
        return WatermarkUtils.geneRandom(seed, length, 2);
    }

    public static void main(String args[]){
        String filePath = "D:/360/360/data/ta_cb_person_heatmap_collect.xls";
        String [] Keys = {"id", "name", "time", "phone", "date"};

        WatermarkUtils embeddingUint = new WatermarkUtils(new File(filePath));

        // create watermarking
        List<Integer> wmBin = embeddingUint.geneRandom(123456+1,32, 2);
        List<Integer> wmInt = embeddingUint.bin2Int(wmBin);
        int msgLen = wmBin.size();

        List<List<Integer>> validCol = embeddingUint.findEmbeddingCols(Keys);
        embeddingUint.embed2OneCol(0, validCol.get(0).get(1), wmInt, 1);
    }


}