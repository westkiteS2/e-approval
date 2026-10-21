package com.example.approval.document.controller;

import com.example.approval.document.domain.DocumentStatus;
import com.example.approval.document.dto.DocumentDetailResponse;
import com.example.approval.document.dto.DocumentRequest;
import com.example.approval.document.dto.DocumentSummaryResponse;
import com.example.approval.document.service.DocumentService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 결재 문서 API (로그인 필요 – SecurityConfig 의 anyRequest().authenticated())
 *
 * 작성/수정은 multipart/form-data:
 *   data  : 문서 내용 JSON (DocumentRequest)
 *   files : 첨부파일 (여러 개, 선택)
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /** 내가 작성한 문서 목록. ?status=DRAFT|IN_PROGRESS|APPROVED|REJECTED (없으면 전체) */
    @GetMapping
    public List<DocumentSummaryResponse> myDocuments(Authentication auth,
                                                     @RequestParam(required = false) DocumentStatus status) {
        return documentService.myDocuments(auth.getName(), status);
    }

    /** 내가 결재할 차례인 문서 목록 */
    @GetMapping("/inbox")
    public List<DocumentSummaryResponse> inbox(Authentication auth) {
        return documentService.inbox(auth.getName());
    }

    @GetMapping("/{id}")
    public DocumentDetailResponse detail(Authentication auth, @PathVariable Long id) {
        return documentService.getDetail(auth.getName(), id);
    }

    /** 새 문서 작성 (data.submit=true 면 바로 상신) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDetailResponse create(Authentication auth,
                                         @RequestPart("data") DocumentRequest data,
                                         @RequestPart(name = "files", required = false) List<MultipartFile> files) {
        return documentService.create(auth.getName(), data, files);
    }

    /** 임시저장 문서 수정 (파일 업로드 호환성을 위해 PUT 대신 POST 사용) */
    @PostMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentDetailResponse update(Authentication auth,
                                         @PathVariable Long id,
                                         @RequestPart("data") DocumentRequest data,
                                         @RequestPart(name = "files", required = false) List<MultipartFile> files) {
        return documentService.update(auth.getName(), id, data, files);
    }

    public record DecisionRequest(String comment) {}

    @PostMapping("/{id}/approve")
    public DocumentDetailResponse approve(Authentication auth, @PathVariable Long id, @RequestBody(required = false) DecisionRequest body) {
        return documentService.decide(auth.getName(), id, true, body == null ? null : body.comment());
    }

    @PostMapping("/{id}/reject")
    public DocumentDetailResponse reject(Authentication auth, @PathVariable Long id, @RequestBody DecisionRequest body) {
        return documentService.decide(auth.getName(), id, false, body == null ? null : body.comment());
    }

    /** 회수 (첫 결재자가 처리하기 전까지만) */
    @PostMapping("/{id}/withdraw")
    public DocumentDetailResponse withdraw(Authentication auth, @PathVariable Long id) {
        return documentService.withdraw(auth.getName(), id);
    }

    /** 임시저장 문서 삭제 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        documentService.delete(auth.getName(), id);
    }

    /** 첨부파일 다운로드 (열람 권한이 있는 사람만) */
    @GetMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<Resource> download(Authentication auth,
                                             @PathVariable Long id,
                                             @PathVariable Long attachmentId) {
        DocumentService.AttachmentFile file = documentService.loadAttachment(auth.getName(), id, attachmentId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.originalName(), StandardCharsets.UTF_8) // 한글 파일명 지원
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(file.resource());
    }
}
