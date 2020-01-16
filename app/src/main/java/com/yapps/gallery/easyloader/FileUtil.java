package com.yapps.gallery.easyloader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 文件操作类
 * @Description: 文件操作类

 * @FileName: FileUtil.java

 * @Package com.device.photo

 * @Author Hanyonglu

 * @Date 2012-5-10 下午01:37:49

 * @Version V1.0
 */
public class FileUtil {
    public FileUtil() {
// TODO Auto-generated constructor stub
    }

    /**
     * InputStream to byte
     * @param inStream
     * @return
     * @throws Exception
     */
    public byte[] readInputStream(InputStream inStream) throws Exception {
//        用缓冲的原因是起初不知道输入流有多少字节
//        这样最大能读入恰好填满缓冲区的字节数
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

//        from the input stream, we get bytes and write them into the buffer
        while ((len = inStream.read(buffer)) != -1) {
//            read bytes from the buffer and pump it into the output stream
            outStream.write(buffer, 0, len);
        }

        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();

        return data;
    }

    /**
     * Byte to bitmap
     * @param bytes
     * @param opts
     * @return
     */
    public Bitmap getBitmapFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null){
            if (opts != null){
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,opts);
            }
            else{
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        }

        return null;
    }
}
