package hello.upload.controller;

import hello.upload.domain.Item;
import hello.upload.domain.ItemRepository;
import hello.upload.domain.UploadFile;
import hello.upload.file.FileStore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemForm form) {
        return "item-form";
    }

    //제출 버튼 눌러 저장이 되는
    //ItemForm 필드에 MultiPartFile 타입의 필드 있어 파일 받을 수 있음
    @PostMapping("/items/new")
    public String saveItem(@ModelAttribute ItemForm form, RedirectAttributes redirectAttributes) throws IOException {

        //첨부 파일, 컴퓨터에 저장되면서 업로드 파일명과 컴퓨터에 저장된 파일명 반환하는
        //form에서 파일 갖고와 사용자가 업로드한 파일이름과 서버에 저장할 파일 이름 반환
        UploadFile attachFile = fileStore.storeFile(form.getAttachFile());

        //이미지 파일들
        //여러개의 이미지 파일도 갖고와서 사용자가 업로드한 파일이름과 서버에 저장할 파일 이름 리스트로 반환
        List<UploadFile> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

        //데이터베이스에 저장
        //파일은 db에 저장하지 않음, 스토리지에
        //db에는 경로정도만
        Item item = new Item();
        item.setItemName(form.getItemName());
        //실제 업로드한 파일명과, 컴퓨터에 저장된 파일명 둘 다 저장
        //파일 1개, 파일 여러 개
        item.setAttachFile(attachFile);
        item.setImageFiles(storeImageFiles);
        itemRepository.save(item);

        //저장된 디비 기본키 값
        redirectAttributes.addAttribute("itemId", item.getId());

        //기본키값을 url 요청에 넣어 리다이렉트
        //즉 제출 버튼 누르면 파일을 저장함과 동시에 저장한 파일 보여주는 페이지를 url 요청
        return "redirect:/items/{itemId}";
    }

    //저장된걸 고객에게 보여주고 다운로드 할 수 있는 기능
    @GetMapping("/items/{id}")
    public String items(@PathVariable Long id, Model model) {

        //id에 맞는 상품 찾기
        Item item = itemRepository.findById(id);
        //찾은 상품 모델에 담기
        model.addAttribute("item", item);

        return "item-view";
    }

    //위에 /items/{id} 요청하면 item-view 뷰 화면 보여주면서 랜더링한다
    //item-view 파일에 th:src="|/images/${imageFile.getStoreFileName()}|" 로 url 요청
    //보통 th:src 하면 이미지 경로, model 로 이미지 경로 담아 바로 랜더링해서 이미지 보이지만

    //이미지 보여주게 하는
    //이미지에 파일 이름이 넘어오면, 서버에 저장될 파일이름인 uuid
    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {

        //서버에 저장될 파일이름인 uuid를 넣어 파일의 전체경로를 주면 UrlResource가 그 경로 찾아옴
        //이 경로에 있는 파일에 접근해서 스트림으로 반환하게 됨
        //UrlResource 는 경로에 file 이 붙으면 내부 파일에 접근함, 직접 접근해 자원 갖고와
        //url 요청으로 업로드 파일명이 오면 전체 경로 만들어 파일에 접근해서 바이너리 데이터를 웹브라우저로 전송
       return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    //items/{id} 로 업로드한 파일 조회 화면에서 첨부파일 링크 클릭하면 파일 다운로드 되도록
    //@ResponseBody 안쓰고 ResponseEntity<Resource> 이거 씀
    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId) throws MalformedURLException {
        //아이템을 접근할 수 있는 사용자만 파일을 다운받게 하는
        Item item = itemRepository.findById(itemId);

        //컴퓨터에 저장된 파일명
        String storeFileName = item.getAttachFile().getStoreFileName();

        //사용자가 파일을 다운받을때 다운받은 파일명이 사용자가 업로드한 파일명으로 하게
        String uploadFileName = item.getAttachFile().getUploadFileName();

        //업로드 된거 즉 컴퓨터에 저장된거를 가져오는거니 업로드 된 파일명을 파라미터로 주어 파일 있는 전체 경로를
        //file 뒤에 붙이고, UrlResource 는 경로에 file 이 붙으면 내부 파일에 접근
        //UrlResource 객체에 파일 들어있다
        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));

        log.info("uploadFileName={}", uploadFileName);

        //한글, 특수문자 깨질 수 있기 때문에 인코딩
        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);

        //다운받는 파일명 지정
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        //다운을 받기 위해 추가적인 헤더를 넣어줘야함
        //ResponseEntity.ok().body(resource)만 하면 홈페이지 바디에 파일 내용만 출력됨
        //브라우저가 HttpHeaders.CONTENT_DISPOSITION 이거 보고 첨부파일 인식
        return ResponseEntity.ok()
                //파일 다운로드 받는 헤더와 다운로드 파일명 넣기
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
