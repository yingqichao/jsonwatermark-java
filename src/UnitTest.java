import Setting.Settings;
import Utils.Util;
import Utils.cyclic.CyclicCoder;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.util.Date;

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

        int block_data = 16;int key = Settings.KEY_POLYNOMIAL;
        int res = Utils.cyclic.CyclicCoder.encode(block_data,key);
        String crc_text = Util.dec2bin(res, Settings.DEFAULT_EMBEDLEN_LENGTH);
        System.out.println("Test Encode: "+crc_text);

        System.out.println((Utils.cyclic.CyclicCoder.decode(res,key)==-1)?"False":"True "+Utils.cyclic.CyclicCoder.decode(res,key));


    }
}

