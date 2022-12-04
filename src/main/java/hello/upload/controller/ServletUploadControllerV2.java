package hello.upload.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

//서블릿이 제공하는 Part 는 편하기는 하지만, HttpServletRequest를 컨트롤러에서 받아사용해야 하고,
//추가로 파일/부분만 구분하려면 여러가지 코드를 넣어야 한다

@Slf4j
@Controller
@RequestMapping("/servlet/v2")
public class ServletUploadControllerV2 {

    //application.properties에 있는 속성 그대로 가져옴
    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/upload")
    public String newFile() {
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFileV1(HttpServletRequest request) throws ServletException, IOException {
        log.info("request={}", request);

        String itemName = request.getParameter("itemName");
        log.info("itemName={}", itemName);

        Collection<Part> parts = request.getParts();
        log.info("parts={}", parts);

        for (Part part : parts) {
            log.info("==== PART ====");
            log.info("name={}", part.getName());

            //parts도 헤더와 바디로 구성되어 있음
            //파일을 넘길때는 content-disposition과 content-type 두개의 헤더가 넘어감
            //그냥 폼데이터는 content-disposition

            //홈페이지에서 파일 선택후 제출버튼 누르면 로그 뜸뜸
            Collection<String> headerNames = part.getHeaderNames();

            for (String headerName : headerNames) {
                log.info("header {}: {}", headerName, part.getHeader(headerName));
            }

            //멀티파트 형식은 전송 데이터를 하나하나 각각 부분( Part )으로 나누어 전송한다.
            // parts 에는 이렇게 나누어진 데이터가 각각 담긴다.
            //서블릿이 제공하는 Part 는 멀티파트 형식을 편리하게 읽을 수 있는 다양한 메서드를 제공한다.
            //Part 주요 메서드
            //part.getSubmittedFileName() : 클라이언트가 전달한 파일명
            //part.getInputStream(): Part의 전송 데이터를 읽을 수 있다.
            //part.write(...): Part를 통해 전송된 데이터를 저장할 수 있다.

            //큰 용량의 파일을 업로드를 테스트 할 때는 로그가 너무 많이 남아서 다음 옵션을 끄는 것이 좋다.
            //> logging.level.org.apache.coyote.http11=debug
            //> 다음 부분도 파일의 바이너리 데이터를 모두 출력하므로 끄는 것이 좋다.
            //> log.info("body={}", body);

            //편의 메서드
            //content-disposition; filename
            //사용자가 전송한 파일명
            log.info("submittedFilename={}", part.getSubmittedFileName());
            log.info("size={}", part.getSize()); //part body size

            //바디에 있는 데이터 읽기
            InputStream inputStream = part.getInputStream();
            //.copyToString는 읽은걸 string으로, 항상 바이너리를 문자로 바꿀때는 캐릭터셋 정해줘야함
            String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

            log.info("body={}", body);

            //파일에 저장하기,
            //파일이 있는지 확인
            if (StringUtils.hasText(part.getSubmittedFileName())) {
                //이전에 저장해둔 경로와 part에서 파일이름을 갖고와 합침
                String fullPath = fileDir + part.getSubmittedFileName();

                log.info("파일 저장 fullPath={}", fullPath);

                //파일이 그 경로에 저장됨
                part.write(fullPath);
            }
        }

        return "upload-form";
    }
}
