package hello.upload.domain;

import lombok.Data;

//업로드 파일 정보 보관
@Data
public class UploadFile {

    //uploadFileName : 고객이 업로드한 파일명
    //storeFileName : 서버 내부에서 관리하는 파일명

    //구분 이유
    //다른 사람이 같은 파일명을 올리면 파일이 덮혀지기 때문에
    //storeFileName은 uuid 로 유일무이하게
    private String uploadFileName;
    private String storeFileName;

    public UploadFile(String uploadFileName, String storeFileName) {
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
    }
}
