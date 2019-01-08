package cn.kuroneko.demo.springbootmongodb.service;


import cn.kuroneko.demo.springbootmongodb.utils.IdGenerator;
import com.google.common.base.Preconditions;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
@Slf4j
public class FileService {


    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Resource
    private MongoDbFactory mongoDbFactory;

    public static final String UTF_8_CHARSET="UTF-8";


    @Transactional
    public void uploadBatch(MultipartFile[] files){
        Preconditions.checkArgument(null != files, "上传的文件不能为空");
        for (MultipartFile file:
             files) {
            upload(file);
        }
    }

    @Transactional
    public void upload(MultipartFile file){
        Preconditions.checkArgument(null != file, "上传的文件不能为空");
        if(!file.isEmpty()){
            String originalName = file.getOriginalFilename();
            String newName = IdGenerator.createNewId() + originalName;
            long size = file.getSize();
            log.info("文件上传 文件名 {} 文件大小{}",originalName,size);
            try {
                ObjectId store = gridFsTemplate.store(file.getInputStream(), newName);
                if(null==store){
                    throw new RuntimeException("文件上传出错");
                }
                //把文件的信息存入数据库中，以便查询
                log.info("文件上传成功 文件名 {} 文件大小{} mongodbId {}",originalName,size,store.toString());
            } catch (IOException e) {
                e.printStackTrace();
                log.error("文件上传出错"+e.getMessage());
                throw new RuntimeException("文件上传出错"+e.getMessage());
            }
        }else{
            throw new RuntimeException("上传的文件为空");
        }
    }

    //实际使用应当传入的是存在数据库的主键，通过主键查出文件的mongoId
    public void download(HttpServletRequest request,HttpServletResponse response,String mongoId) throws IOException {
        Preconditions.checkArgument(StringUtils.isNoneBlank(mongoId), "下载文件id不能为空");
        GridFSFile file = gridFsTemplate.findOne(new Query().addCriteria(Criteria.where("_id").is(mongoId)));
        GridFsResource gridFsResource = new GridFsResource(file, GridFSBuckets.create(mongoDbFactory.getDb()).openDownloadStream(file.getObjectId()));
        //从数据库查出文件名
        String fileName = "fileNameFromDataBase";
        String userAgent = request.getHeader("User-Agent").toUpperCase();
        //IE浏览器：
        if (userAgent.contains("MSIE") ||
                userAgent.contains("TRIDENT")
                || userAgent.contains("EDGE")) {
            fileName = java.net.URLEncoder.encode(fileName, UTF_8_CHARSET);
        //火狐浏览器
        } else if (userAgent.contains("Firefox")) {
            BASE64Encoder base64Encoder = new BASE64Encoder();
            fileName = "=?utf-8?B?"
                    + base64Encoder.encode(fileName.getBytes(UTF_8_CHARSET))
                    + "?=";
        //其他浏览器
        } else {
            fileName = java.net.URLEncoder.encode(fileName, UTF_8_CHARSET);
        }
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        response.setContentLength((new Long(file.getLength()).intValue()));
        log.info("开始下载... {}",fileName);
        IOUtils.copy(gridFsResource.getInputStream(),response.getOutputStream());
    }

    //实际使用应当传入的是存在数据库的主键，通过主键查出文件的mongoId
    @Transactional
    public void delete(String mongoId) {
        Preconditions.checkArgument(StringUtils.isNoneBlank(mongoId), "下载文件id不能为空");
        log.info("文件删除的mongoId{}",mongoId);
        Query query = new Query().addCriteria(Criteria.where("_id").is(mongoId));
        gridFsTemplate.delete(query);
        log.info("文件删除成功");
    }



}
