/**
 * @author Qichao Ying
 * @date 2019/8/12 16:26
 * @Description DEFAULT
 */
import ExcelWatermarkHelper.excel.ExcelUtil;
import ExcelWatermarkHelper.utils.WatermarkUtils;
import GeneralHelper.LtDecoder;
import Setting.Settings;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import static Setting.Settings.DEFAULT_EMBEDLEN;
import static java.lang.Integer.max;
import static java.lang.Math.min;

public class ExcelDecoder extends AbstractDecoder{

    File file;
    FileOutputStream out;
    Workbook wb ;
    String fileVersion;
    ExcelUtil excl = new ExcelUtil();
    List<Integer> keyCols = new LinkedList<>();
    int keyIndex = 0;

    int[] exclRow = null;
    int[] exclCol = null;
    int sheetNum = -1;

    public static int sum = 0;
    public static int valid = 0;
    public static int updated = 0;

    public ExcelDecoder(int seed, double c, double delta, String f_bytes,File file) {
        this.file = file;
        this.fileVersion = file.getName().substring(file.getName().lastIndexOf("."));
        this.wb = excl.getWorkbook(file);
        this.getSheetsRowAndCol();
    }

    /*
     * 获得workbook的最大行列
     */
    private void getSheetsRowAndCol(){
        this.sheetNum = this.wb.getNumberOfSheets();
        exclRow = new int[this.sheetNum];
        exclCol = new int[this.sheetNum];
        for(int i = 0; i < this.sheetNum; i++){
            exclRow[i] = this.wb.getSheetAt(i).getPhysicalNumberOfRows();
            if(0 != exclRow[i]){
                for(int j = 0; j < min(exclRow[i], 20); j++){
                    exclCol[i] =  max(this.wb.getSheetAt(i).getRow(j).getPhysicalNumberOfCells(), exclCol[i]);
                }
            }
        }
    }

    public void decode(int row,int filesize){
        this.excl.getExactValue(this.wb, 0, row, col)

        decoder = new LtDecoder(Settings.DEFAULT_C,Settings.DEFAULT_DELTA);
        //get dynamically embedment: calculate total sum
        PriorityQueue<Map.Entry<Integer,String>> pq = new PriorityQueue<>((a,b)->(b.getValue().length()-a.getValue().length()));
        int totalLen = 0;List<Integer> eachLen = new LinkedList<>();
        for(int col=0;col<exclCol[0];col++){
            if(col!=keyIndex) {
                String str = this.excl.getExactValue(this.wb, 0, row, col).toString().replaceAll("[^A-Za-z0-9]", "");
                totalLen += str.length();
                pq.offer(new AbstractMap.SimpleEntry<>(col,str));
            }
        }
        //data extraction
        int beginInd = 0;
        while(pq.size()!=0){
            Map.Entry<Integer,String> entry = pq.poll();
            //data embedment according to length of value
            int len = (int)Math.ceil(DEFAULT_EMBEDLEN*entry.getValue().toString().length()/(double)totalLen);

            String modified = modify(row,entry.getKey(),entry.getValue(),crc_text.substring(beginInd,Math.min(beginInd+len,crc_text.length())));

            beginInd += len;
            if(beginInd>=DEFAULT_EMBEDLEN)    break;
        }





        for(String key:JSON.keySet()){
            String lt_block = JSON.get(key);
            if(decoder.consume_block(filesize,key,lt_block,1)){
                decoder.received_packs++;
                if(decoder.is_done()){
                    success_time++;
                    System.out.println("--> Decoded Successfully <--... The ExcelWatermarkHelper is now successfully retrieved. Time: "+success_time);
                    List<Integer> buff = decoder.bytes_dump();
                    String str = "";
                    for(int i=0;i<buff.size();i++) {
                        int tmp = buff.get(i);
                        if(tmp==-1)
                            str+="?";
                        else
                            str += (char)('a'+tmp);
                    }
                    secret_data.add(str);
                    decoder.succeed_and_init();
                }else{
                    System.out.println("Need more Packs...Received: "+decoder.received_packs);
                }
            }
        }

    }

    public List<String> run(String filePath) throws Exception{
        //Reads from stream, applying the LT decoding algorithm to incoming encoded blocks until sufficiently many blocks have been received to reconstruct the entire file.
        System.out.println("-----------------------------Extraction---------------------------------------");

        WatermarkUtils watermarkUtils = new WatermarkUtils(new File(filePath));

//        // 解析string
//        int filesize = eliminateLevels(object);
////        modified_json= Utils.Util.eliminateLevels(JSON, "");
        decode(JSON, filesize);
//
//        return this.secret_data;
    }


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


}
