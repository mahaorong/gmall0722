package com.atguigu.gmall.manager;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManagerWebApplicationTests {

    @Test
    public void contextLoads() throws IOException, MyException {
        String path = GmallManagerWebApplicationTests.class.getClassLoader().getResource("tracker.conf").getPath();

        ClientGlobal.init(path);

        TrackerClient trackerClient = new TrackerClient();

        TrackerServer connection = trackerClient.getConnection();

        System.out.println(connection);

        // tracker返回一个storage
        StorageClient storageClient = new StorageClient(connection, null);

        // 上传文件
        String[] strs = storageClient.upload_file("d:/123.jpg", "jpg", null);

        // 返回上传数据的元数据信息
        for (String str : strs) {
            System.out.println(str);
        }
    }

}
