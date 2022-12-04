package hello.upload.file;

import hello.upload.domain.UploadFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//업로드한 파일명, 실제 폴더에 저장되는 파일명 생성하고
//파일을 저장하는 클래스

//고객이 업로드한 파일명으로 서버 내부에 파일을 저장하면 안된다.
//왜냐하면 서로 다른 고객이 같은 파일이름을 업로드 하는 경우 기존 파일 이름과 충돌이 날 수 있다.
//서버에서는 저장할 파일명이 겹치지 않도록 내부에서 관리하는 별도의 파일명이 필요

// 파일 저장과 관련된 업무 처리
@Component
public class FileStore {

    //파일 저장할 폴더 경로 가져옴
    //이 파일 경로에 파일 저장
    @Value("${file.dir}")
    private String fileDir;

    //파일이름을 받아 총경로 반환
    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    //파일 저장 메서드, 여러개 업로드하는
    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {

        //UploadFile 타입의 리스트 생성
        //업로드한 파일명과, 서버 내부에 저장할 파일명
        List<UploadFile> storeFileResult = new ArrayList<>();

        //파일 하나씩 꺼내
        for (MultipartFile multipartFile : multipartFiles) {

            //비어있지 않으면 multipartFile storeFile메서드에 넣어 결과를
            //사용자가 업로드한 파일이름과 서버에 저장할 파일 이름을 가지고 있는 리스트에 추가
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile));
            }
        }

        //파라미터로 여러 개 들어온 파일들의 각각 사용자가 업로드한 파일이름과 서버에 저장할 파일 이름을 담아 반환
        return storeFileResult;
    }

    //파일 저장 메서드, 1개 들어옴
    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {

        //파일 비어있다면 null반환
        if (multipartFile.isEmpty()) {
            return null;
        }

        //사용자가 업로드한 파일이름 가져옴
        String originalFilename = multipartFile.getOriginalFilename();

        //서버에 저장할 파일 이름,
        //사용자가 업로드한 파일이름을 createStoreFileName() 메서드에
        String storeFileName = createStoreFileName(originalFilename);

        //총 경로를 반환하는 getFullPath에 서버에 저장할 파일 이름 넣어
        //파일의 최종경로 생성하고 파일을 이 경로에 저장
        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        //사용자가 업로드한 파일이름과 서버에 저장할 파일 이름 반환
        return new UploadFile(originalFilename, storeFileName);
    }

    //서버에 저장할 파일이름 만드는 메서드
    private String createStoreFileName(String originalFilename) {

        //사용자가 업로드한 파일에서 확장자 추츨하는 메서드 사용해 확장자 가져오기
        String ext = extractExt(originalFilename);

        //서버에 저장하는 파일명 랜덤으로 만들어 무슨 파일인지 인지할 수 있게 확장자까지 붙이고
        String uuid = UUID.randomUUID().toString();

        //최종적으로 서버에 저장할 파일명 반환
        return uuid + "." + ext;
    }

    //확장자 가져오는 메서드
    private String extractExt(String originalFilename) {

        //.의 위치를 가져옴
        int pos = originalFilename.lastIndexOf(".");

        //점의 위치 다음 글자부터 끝까지 반환
        return originalFilename.substring(pos + 1);
    }

}
