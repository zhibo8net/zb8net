package website2018.api.admin;

import java.io.File;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.Files;

import website2018.base.BaseEndPoint;
import website2018.utils.UploadUtils;

@Controller
public class FileUploadEndpoint extends BaseEndPoint {

    //返回例如http://127.0.0.1/image/2018/2/2/aakjfkasjfk.jpeg
    @RequestMapping(value = "/api/admin/file", method = RequestMethod.POST)
    @ResponseBody
    public String upload(@RequestParam("image") MultipartFile file) throws Exception {

        String ext = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");

        String fileName = UUID.randomUUID().toString() + "." + ext;

        String nameWithTimePrefix = UploadUtils.nameWithTimePrefix(fileName);
        
        String fullPath = uploadPath + nameWithTimePrefix;
        
        File diskFile = new File(fullPath);
        
        Files.createParentDirs(diskFile);

        file.transferTo(diskFile);

        return webImageBase + nameWithTimePrefix;
    }

}
