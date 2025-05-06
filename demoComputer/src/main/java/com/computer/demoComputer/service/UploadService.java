package com.computer.demoComputer.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadService {
    
    public String handleSaveUploadAvatar(MultipartFile multipartFile) {

        if (multipartFile.isEmpty() || multipartFile.getSize() == 0) {
            return "";
        }

        String uploadDir = "uploads/images/avatar";
        String fileName = "";
        
        try {
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }

            fileName = System.currentTimeMillis() + "-" + multipartFile.getOriginalFilename();
            File serverFile = new File(uploadFolder, fileName);

            byte[] bytes = multipartFile.getBytes();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(serverFile));
            bos.write(bytes);
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileName;
    }

    public String handleSaveUploadImage(MultipartFile multipartFile) {

        if (multipartFile.isEmpty() || multipartFile.getSize() == 0) {
            return "";
        }

        String uploadDir = "uploads/images/product";
        String fileName = "";
        
        try {
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }

            fileName = System.currentTimeMillis() + "-" + multipartFile.getOriginalFilename();
            File serverFile = new File(uploadFolder, fileName);

            byte[] bytes = multipartFile.getBytes();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(serverFile));
            bos.write(bytes);
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileName;
    }
}

//    @RequestMapping(value = "/admin/user/create", method = RequestMethod.POST)
//    public String userPageAfterCreate(
//            @ModelAttribute("newUser") User user,
//            @RequestParam("myFile") MultipartFile multipartFile) {
//
//        try {
//            byte[] bytes = multipartFile.getBytes();
//            String rootPath = this.servletContext.getRealPath("/uploads/images");
//
//            File file = new File(rootPath + File.separator + "avatar");
//            if (!file.exists()) {
//                file.mkdirs();
//            }
//
//            File serverFile = new File(file.getAbsolutePath() + File.separator
//                    + System.currentTimeMillis() + "-" + multipartFile.getOriginalFilename());
//
//            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(serverFile));
//            bufferedOutputStream.write(bytes);
//            bufferedOutputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        this.userService.handleSaveUser(user);
//        return "redirect:/admin/user";
//    }