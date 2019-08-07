import watermark.utils.WatermarkUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.min;

public class excel_extraction {

    /*
     * 对filePath的Excel文件的所有sheet中的浮点数列进行水印提取
     * @param filePath : Excel 文件路径
     * @param msgLen : 二进制的水印长度
     * @return : 返回嵌入的水印信息，包含所有成功提取出来的水印
     */
    public String[] extract(String filePath, int msgLen, String [] Keys){
        List<List<Integer>> extWmBins = new LinkedList<>();
        WatermarkUtils extractionUint = new WatermarkUtils(new File(filePath));

        List<List<Integer>> validCol = extractionUint.findEmbeddingCols(Keys);
        for(int sheet = 0; sheet < validCol.size(); sheet++){
            for(int col = 0; col < validCol.get(sheet).size(); col++) {
                List<Integer> extWmBin = extractionUint.extractFromOneCol(sheet, validCol.get(sheet).get(col), msgLen);
                if(0 == extWmBin.size()){
                    System.out.println("Warning : there is no watermarking in " + validCol.get(sheet).get(col) + "th column" +
                            " of sheet : \"" + extractionUint.getSheetName(sheet) + "\"");
                }else{
                    System.out.println("There is some watermarking in " + validCol.get(sheet).get(col) + "th column" +
                            " of sheet : \"" + extractionUint.getSheetName(sheet) + "\"");
                    //System.out.println("\n======> Watermark : " + extWmBin);
                    extWmBins.add(extWmBin);
                }
            }

        }
        String[] wmStr = new String[extWmBins.size()];
        for(int i = 0; i < extWmBins.size(); i++){
            wmStr[i] = extractionUint.Bin2String(extWmBins.get(i));
        }
        return wmStr;
    }

    /*
     * 对filePath的Excel文件的指定sheet中的前几个浮点数列进行提取
     * @param filePath : Excel 文件路径
     * @param msgLen : 二进制的水印长度
     * @param sheetIndex : 需要提取的sheet的索引下标，从0开始
     * @param embedColNum : 需要提取的浮点数列的个数，选择最前面的开始提取
     * @return : 返回嵌入的水印信息，包含所有提取出来的水印结果
     */
    public String[] extract(String filePath, int msgLen, int sheetIndex, int embedColNum, String [] Keys){
        List<List<Integer>> extWmBins = new LinkedList<>();

        WatermarkUtils extractionUint = new WatermarkUtils(new File(filePath));

        List<List<Integer>> validCol = extractionUint.findEmbeddingCols(Keys);
        for(int col = 0; col < min(embedColNum, validCol.get(sheetIndex).size()); col++) {
            List<Integer> extWmBin = extractionUint.extractFromOneCol(sheetIndex, validCol.get(sheetIndex).get(col), msgLen);
            if(0 == extWmBin.size()){
                System.out.println("Warning : there is no watermarking in this column : " + col +
                        " of sheet : \"" + extractionUint.getSheetName(sheetIndex) + "\"");
            }else{
                System.out.println("There is one watermarking in this column : " + col +
                        " of sheet : \"" + extractionUint.getSheetName(sheetIndex) + "\"" +
                        "\n======> Watermark : " + extWmBin);
                extWmBins.add(extWmBin);
            }
        }
        String[] wmStr = new String[extWmBins.size()];
        for(int i = 0; i < extWmBins.size(); i++){
            wmStr[i] = extractionUint.Bin2String(extWmBins.get(i));
        }
        return wmStr;
    }

    public static void main(String args[]){
        String filePath = "D:/360/360/data/搜索日志广告位统计详情.xlsx";
        String wmStr = "this is a test for excel watermarking embedding !";

        String [] Keys = {"id", "name", "time", "phone", "date"};
        WatermarkUtils extractionUint = new WatermarkUtils(new File(filePath));

        List<List<Integer>> validCol = extractionUint.findEmbeddingCols(Keys);
        List<Integer> wmBin = extractionUint.String2Bin(wmStr);
        List<Integer> wmInt = extractionUint.bin2Int(wmBin);
        int msgLen = wmBin.size();

        List<Integer> extWmBin = extractionUint.extractFromOneCol(0,validCol.get(0).get(2),msgLen);
        if(0 != extWmBin.size()){
            String extWmStr = extractionUint.Bin2String(extWmBin);

            // test for errors
            System.out.println("******* Embedding capacity : " + extWmBin.size() + " bits");
            System.out.println("******* Message : " + extWmStr);
            int cnt = 0;
            for(int i = 0; i < wmBin.size(); i++){
                cnt = cnt + (wmBin.get(i).equals(extWmBin.get(i)) ? 0 : 1);
            }
            System.out.println("******* There are " + cnt + " bits error");
        }else{
            System.out.println("******* There is no watermarking in this column ." );
        }

    }


}