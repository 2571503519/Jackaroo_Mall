package com.jackaroo.service.impl;

import com.google.common.collect.Lists;
import com.jackaroo.service.IFileService;
import com.jackaroo.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     * 上传文件，file（临时） --->  targetFile（应用服务器中）  --->  FTP（FTP文件服务器中）
     * @param file 上传的文件对象
     * @param path 上传的指定路径
     * @return
     */
    @Override
    public String upload(MultipartFile file, String path) {
        // 获取文件的原始名称
        String fileName = file.getOriginalFilename();
        // 获取文件的扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 生成唯一的文件名（包含扩展名）
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件，上传文件名：{}，上传路径：{}，新文件名：{}", fileName, path, uploadFileName);
        // 判断指定的上传路径是否存在，不存在则创建
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        // 创建目标文件对象
        File targetFile = new File(path, uploadFileName);

        try {
            // 将上传的临时文件对象，传输给目标文件
            file.transferTo(targetFile);
            // 将目标文件上传至FTP服务器，如果上传成功，则删除目标文件
            if(FTPUtil.uploadFile(Lists.newArrayList(targetFile)))
                targetFile.delete();
        } catch (IOException e) {
            logger.error("文件上传异常", e);
            return null;
        }
        // 返回上传至服务器后的文件名称
        return targetFile.getName();
    }
}
