package hello.upload.controller;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

//데이터 전달하는 form
//상품 저장용 폼이다.
//List<MultipartFile> imageFiles : 이미지를 다중 업로드 하기 위해 MultipartFile 를 사용했다.
//MultipartFile attachFile : 멀티파트는 @ModelAttribute 에서 사용할 수 있다
@Data
public class ItemForm {

    private Long itemId;
    private String itemName;
    private MultipartFile attachFile;
    private List<MultipartFile> imageFiles;
}
