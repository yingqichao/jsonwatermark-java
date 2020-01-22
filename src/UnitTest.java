import Setting.Settings;
import Utils.Util;
import Utils.cyclic.CyclicCoder;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.util.Date;
import java.util.TreeSet;

public class UnitTest {

    public static void main(String[] args) throws Exception{
//        Path testPath = Paths.get("F:/download/test.txt");
//        BasicFileAttributeView basicView = Files. getFileAttributeView(testPath, BasicFileAttributeView.class);
//        BasicFileAttributes basicFileAttributes = basicView.readAttributes();
//        System.out.println("创建时间：" + new Date(basicFileAttributes.creationTime() .toMillis()));
//        System.out.println("最后访问时间：" + new Date(basicFileAttributes. lastAccessTime().toMillis()));
//        System.out.println("最后修改时间：" + new Date(basicFileAttributes. lastModifiedTime().toMillis()));
//        System.out.println("文件大小：" + basicFileAttributes.size());
//        FileOwnerAttributeView ownerView = Files.getFileAttributeView(testPath, FileOwnerAttributeView.class);
//        System.out.println("文件所有者：" + ownerView.getOwner());

        int block_data = Util.bin2dec("0110101000");
        //int res = Utils.cyclic.CyclicCoder.encode(block_data,key);
//        int res = Util.bin2dec("001010");
//        String crc_text = Util.dec2bin(res, 10);
//        int crc_text = Utils.cyclic.CyclicCoder.encode(block_data,Settings.LENGTH,Settings.LONG);
//        System.out.println("Test Encode: "+Util.dec2bin(crc_text,10));
        int res = Utils.cyclic.CyclicCoder.decode_without_correction(block_data,Settings.LENGTH,Settings.LONG);
        System.out.println((res==-1)?"False":"True-"+Util.dec2bin(res,6));


        //batch
        int correct = 0;
        for(int i=0;i<Math.pow(2,10);i++){
            if(Utils.cyclic.CyclicCoder.decode_without_correction(i,Settings.LENGTH,Settings.LONG)!=-1) {
                correct++;
//                System.out.println(Util.dec2bin(i,7));
            }
        }
        System.out.println("Correct: "+correct);


    }
}

