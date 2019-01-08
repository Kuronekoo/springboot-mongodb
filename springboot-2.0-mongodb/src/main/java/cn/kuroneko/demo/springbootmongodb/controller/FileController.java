package cn.kuroneko.demo.springbootmongodb.controller;

import cn.kuroneko.demo.springbootmongodb.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/")
public class FileController {

    @Autowired
    FileService fileService;

    @PostMapping("/upload/batch")
    public String upload(@RequestParam("files")MultipartFile[] files,
                         HttpServletRequest request){

        fileService.uploadBatch(files);
        return "success";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file")MultipartFile file,
                         HttpServletRequest request){

        fileService.upload(file);
        return "success";
    }

    @GetMapping("/download")
    public String download(@RequestParam(value = "mongoId",required = true)String mongoId,
                           HttpServletRequest request, HttpServletResponse response) throws IOException {
        fileService.download(request,response,mongoId);
        return "success";
    }


    @DeleteMapping("/delete")
    public String delete(@RequestParam(value = "mongoId",required = true)String mongoId,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        fileService.delete(mongoId);
        return "success";
    }
}
