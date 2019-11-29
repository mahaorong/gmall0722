package com.atguigu.gmall.manager.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MyFileUploadUtil {

    // 上传图片
    public static String uploadImage(MultipartFile multipartFile) {
        String url = "http://192.168.147.110";

        String path = MyFileUploadUtil.class.getClassLoader().getResource("tracker.conf").getPath();

        try {
            ClientGlobal.init(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        TrackerClient trackerClient = new TrackerClient();

        TrackerServer connection = null;
        try {
            connection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(connection);

        // tracker返回一个storage
        StorageClient storageClient = new StorageClient(connection, null);

        try {
            String originalFilename = multipartFile.getOriginalFilename();
            int i = originalFilename.lastIndexOf(".");
            String extName = originalFilename.substring(i);
            // 上传文件
            String[] strs = storageClient.upload_file(multipartFile.getBytes(), extName, null);

            // 返回上传数据的元数据信息
            for (String str : strs) {
                url = url + "/" + str;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }


        return url;
    }
}
