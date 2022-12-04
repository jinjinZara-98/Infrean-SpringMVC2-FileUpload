package hello.upload.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/spring")
public class SpringUploadController {

    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/upload")
    public String newFile() {
        return "upload-form";
    }

    //파일을 선택해 제출버튼을 눌렀을때
    //각 part를 파라미터로 갖고옴, 홈페이지 타임리프로 파라미터 똑같이 받음

    //코드를 보면 스프링 답게 딱 필요한 부분의 코드만 작성하면 된다.
    //@RequestParam MultipartFile file
    //업로드하는 HTML Form의 name에 맞추어 @RequestParam 을 적용하면 된다.
    //추가로 @ModelAttribute 에서도 MultipartFile 을 동일하게 사용할 수 있다.
    @PostMapping("/upload")
    public String saveFile(@RequestParam String itemName,
                           @RequestParam MultipartFile file, HttpServletRequest request) throws IOException {

        log.info("request={}", request);
        log.info("itemName={}", itemName);
        log.info("multipartFile={}", file);

        //파일 비어있다면
        if (!file.isEmpty()) {

            //서블릿처럼 얻어온 파일 경로와 업로드 파일 명을 합친 값 만듬
            String fullPath = fileDir + file.getOriginalFilename();

            log.info("파일 저장 fullPath={}", fullPath);

            //파일을 이 경로에다가 저장해줌, 예외처리 해줘야함
            file.transferTo(new File(fullPath));
        }

        return "upload-form";
    }
}
