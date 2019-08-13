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

    public ExcelDecoder(File file) {
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
        String key = this.excl.getExactValue(this.wb, 0, row, keyIndex).toString();
        //init src_blocks and key for pseudo-random
        decoder = new LtDecoder(Settings.DEFAULT_C,Settings.DEFAULT_DELTA);
        List<Integer> src_blocks = decoder.getSrcBlocks(filesize,key,1);
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
        int remainLen = DEFAULT_EMBEDLEN;int decodeInt = 0;
        while(pq.size()!=0){
            Map.Entry<Integer,String> entry = pq.poll();
            //data embedment according to length of value
            int len = (int)Math.ceil(DEFAULT_EMBEDLEN*entry.getValue().length()/(double)totalLen);

            int retrieve = decoder.extract_excel(((Integer)row).toString(),entry.getValue(),len);

            remainLen -= len;

            retrieve <<= remainLen;

            decodeInt += retrieve;

            if(remainLen<=0)    break;
        }

        if (cyclic.CyclicCoder.decode(decodeInt) != -1) {
            System.out.println("Valid Package.");
            if(decoder.consume_block_excel(src_blocks,decodeInt)) {
                decoder.received_packs++;
                if (decoder.is_done()) {
                    success_time++;
                    System.out.println("--> Decoded Successfully <--... The ExcelWatermarkHelper is now successfully retrieved. Time: " + success_time);
                    List<Integer> buff = decoder.bytes_dump();
                    String str = "";
                    for (int i = 0; i < buff.size(); i++) {
                        int tmp = buff.get(i);
                        if (tmp == -1)
                            str += "?";
                        else
                            str += (char) ('a' + tmp);
                    }
                    secret_data.add(str);
                    decoder.succeed_and_init();
                } else {
                    System.out.println("Need more Packs...Received: " + decoder.received_packs);
                }
            }
        }else{
            System.out.println("Invalid Package.Skipped...");
        }




    }

    public List<String> run(String filePath, int startRow,int filesize) throws Exception{
        //Reads from stream, applying the LT decoding algorithm to incoming encoded blocks until sufficiently many blocks have been received to reconstruct the entire file.
        System.out.println("-----------------------------Extraction---------------------------------------");

//        WatermarkUtils watermarkUtils = new WatermarkUtils(new File(filePath));

        int endRow = exclRow[0];//固定第一个sheet
        //Embedment
        for(int i=startRow;i<endRow;i++){
            decode(i, filesize);
        }

        return this.secret_data;
    }

}
