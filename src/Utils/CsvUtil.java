package Utils;
import com.csvreader.CsvWriter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
/**
 * @author Qichao Ying
 * @date 2019/8/14 15:09
 * @Description DEFAULT
 */
public class CsvUtil {
    // 读取csv文件的内容
    public static ArrayList<String> readCsv(String filepath) {
        File csv = new File(filepath); // CSV文件路径
        csv.setReadable(true);//设置可读
        csv.setWritable(true);//设置可写
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(csv),"GBK"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String line = "";
        String everyLine = "";
        ArrayList<String> allString = new ArrayList<>();
        try {
            while ((line = br.readLine()) != null) // 读取到的内容给line变量
            {
                everyLine = line;
                System.out.println(everyLine);
                allString.add(everyLine);
            }
            System.out.println("csv表格中所有行数：" + allString.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allString;

    }

    public static boolean writeCSV(String path,List<String> writearraylist) {
        String csvFilePath = path;

        try {

            // 创建CSV写对象 例如:CsvWriter(文件路径，分隔符，编码格式);
            CsvWriter csvWriter = new CsvWriter(csvFilePath, ',', Charset.forName("GBK"));
            // 写内容
            String[] headers = {"FileName","FileSize","FileMD5"};
            csvWriter.writeRecord(headers);
            for(int i=0;i<writearraylist.size();i++){
                String[] writeLine=writearraylist.get(i).split(",");
                System.out.println(writeLine);
                csvWriter.writeRecord(writeLine);
            }

            csvWriter.close();
            System.out.println("--------CSV文件已经写入--------");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        String path = "src//embedded_results//testWrite.csv";
        List<String> list = new LinkedList<>();
        list.add("1,2,3,4,5,6");
        list.add("Alice,Alice,Alice,Alice,Alice,Alice");
        writeCSV(path,list);
        List<String> receive = readCsv(path);
//        for(String str:receive){
//            System.out.println(str);
//        }
    }

}
