package com.jackaroo.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    // 配置文件中，FTP服务器的主机地址
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    // 配置文件中，FTP服务器的用户名
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    // 配置文件中，FTP服务器的密码
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    // 真实使用的FTP服务器的主机地址
    private String ip;
    // 真实使用的FTP服务器的端口，默认是21
    private int port;
    // 真实使用的FTP服务器的用户名
    private String user;
    // 真实使用的FTP服务器的密码
    private String pwd;
    // FTP客户端对象
    private FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    /**
     * 静态的文件上传方法，使用配置文件中的设置
     * @param fileList 需要上传的文件对象集合
     * @return
     * @throws IOException
     */
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp, 21, ftpUser, ftpPass);
        logger.info("开始连接FTP服务器");
        boolean result = ftpUtil.uploadFile("img", fileList);
        logger.info("结束上传，上传结果：{}", result);
        return result;
    }

    /**
     * 文件上传方法
     * @param remotePath 文件需要上传到FTP服务器中的路径
     * @param fileList 需要上传的文件对象集合
     * @return
     * @throws IOException
     */
    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        // 上传成功的标志
        boolean uploaded = true;
        // 文件输入流，供FTPClient对象使用
        FileInputStream fis = null;
        // 判断连接FTP服务器是否成功
        if (connectServer(this.ip, this.port, this.user, this.pwd)) {
            // 连接成功
            try {
                // 切换到指定的目录路径下
                ftpClient.changeWorkingDirectory(remotePath);
                // 设置缓冲区大小，1024 Bytes
                ftpClient.setBufferSize(1024);
                // 设置编码方式
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                for (File fileItem : fileList) {
                    fis = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(), fis);
                }
            } catch (IOException e) {
                logger.error("文件上传异常", e);
                uploaded = false;
            } finally {
                // 关闭流对象，关闭FTP连接
                if (fis != null)
                    fis.close();
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }

    /**
     * 连接FTP服务器
     * @param ip
     * @param port
     * @param user
     * @param pwd
     * @return
     */
    private boolean connectServer(String ip, int port, String user, String pwd) {
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, pwd);
        } catch (IOException e) {
            logger.error("连接FTP服务器异常", e);
        }
        return isSuccess;
    }












}
