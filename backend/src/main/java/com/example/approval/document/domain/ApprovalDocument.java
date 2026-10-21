package com.example.approval.document.domain;

import com.example.approval.domain.User;
import com.example.approval.exception.ApiException;
import jakarta.persistence.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 결재 문서.
 * 상태 흐름: DRAFT(임시저장) → IN_PROGRESS(진행중) → APPROVED / REJECTED
 *           IN_PROGRESS 에서 첫 결재자가 처리하기 전이면 회수하여 DRAFT 로 되돌릴 수 있음
 */
@Entity
@Table(name = "approval_documents")
public class ApprovalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String docNumber; // 상신할 때 발급 (예: EXP-2026-0001)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 5000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "writer_id")
    private User writer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    // ----- 지출결의서 전용 -----
    private Long amount;
    private LocalDate expenseDate;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ExpenseCategory expenseCategory;

    // ----- 휴가신청서 전용 -----
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LeaveType leaveType;
    private LocalDate leaveStartDate;
    private LocalDate leaveEndDate;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<ApprovalLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Attachment> attachments = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;

    protected ApprovalDocument() {}

    public ApprovalDocument(User writer, DocumentType type) {
        this.writer = writer;
        this.type = type;
        this.title = "";
        this.status = DocumentStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    // ===================== 내용 변경 =====================

    /** 제목/본문/양식별 항목 변경. 선택한 양식과 상관없는 항목은 비워서 저장 */
    public void changeContent(DocumentType type, String title, String content,
                              Long amount, LocalDate expenseDate, ExpenseCategory expenseCategory,
                              LeaveType leaveType, LocalDate leaveStartDate, LocalDate leaveEndDate) {
        requireDraft();
        this.type = type;
        this.title = title;
        this.content = content;

        boolean expense = type == DocumentType.EXPENSE;
        this.amount = expense ? amount : null;
        this.expenseDate = expense ? expenseDate : null;
        this.expenseCategory = expense ? expenseCategory : null;

        boolean leave = type == DocumentType.LEAVE;
        this.leaveType = leave ? leaveType : null;
        this.leaveStartDate = leave ? leaveStartDate : null;
        this.leaveEndDate = leave ? leaveEndDate : null;

        this.updatedAt = LocalDateTime.now();
    }

    /** 결재선을 통째로 교체 (approvers 순서 = 결재 순서) */
    public void replaceApprovers(List<User> approvers) {
        requireDraft();
        this.lines.clear();
        for (int i = 0; i < approvers.size(); i++) {
            this.lines.add(new ApprovalLine(this, approvers.get(i), i + 1));
        }
    }

    public void addAttachment(Attachment attachment) {
        requireDraft();
        attachment.setDocument(this);
        this.attachments.add(attachment);
    }

    public void removeAttachment(Attachment attachment) {
        requireDraft();
        this.attachments.remove(attachment);
    }

    // ===================== 상태 변경 =====================

    /** 상신: 첫 번째 결재자에게 차례가 넘어감 */
    public void submit(String docNumber) {
        requireDraft();
        if (lines.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "결재자를 1명 이상 지정하세요.");
        }
        if (this.docNumber == null) {
            this.docNumber = docNumber; // 회수 후 재상신하면 기존 번호 유지
        }
        this.status = DocumentStatus.IN_PROGRESS;
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = this.submittedAt;
        lines.forEach(ApprovalLine::reset);
        lines.get(0).markCurrent();
    }

    /** 현재 결재자의 승인; 마지막 단계면 문서 완료 */
    public void approve(User actor, String comment) {
        ApprovalLine line = getCurrentLine().orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "결재 대기 중인 단계가 없습니다."));
        if (!line.getApprover().getId().equals(actor.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "현재 결재자만 승인할 수 있습니다.");
        line.approve(comment);
        int index = lines.indexOf(line);
        if (index + 1 < lines.size()) lines.get(index + 1).markCurrent();
        else { status = DocumentStatus.APPROVED; completedAt = LocalDateTime.now(); }
        updatedAt = LocalDateTime.now();
    }

    public void reject(User actor, String comment) {
        ApprovalLine line = getCurrentLine().orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "결재 대기 중인 단계가 없습니다."));
        if (!line.getApprover().getId().equals(actor.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "현재 결재자만 반려할 수 있습니다.");
        line.reject(comment);
        status = DocumentStatus.REJECTED;
        completedAt = LocalDateTime.now();
        updatedAt = completedAt;
    }

    /** 회수: 아직 아무도 결재하지 않았을 때만 임시저장 상태로 되돌림 */
    public void withdraw() {
        if (!canWithdraw()) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 결재가 진행되어 회수할 수 없습니다.");
        }
        this.status = DocumentStatus.DRAFT;
        this.submittedAt = null;
        this.updatedAt = LocalDateTime.now();
        lines.forEach(ApprovalLine::reset);
    }

    public boolean canWithdraw() {
        return status == DocumentStatus.IN_PROGRESS
                && !lines.isEmpty()
                && lines.get(0).getStatus() == LineStatus.CURRENT;
    }

    public boolean isDraft() { return status == DocumentStatus.DRAFT; }

    public boolean isWrittenBy(User user) { return writer.getId().equals(user.getId()); }

    public boolean hasApprover(User user) {
        return lines.stream().anyMatch(l -> l.getApprover().getId().equals(user.getId()));
    }

    public Optional<ApprovalLine> getCurrentLine() {
        return lines.stream().filter(l -> l.getStatus() == LineStatus.CURRENT).findFirst();
    }

    private void requireDraft() {
        if (!isDraft()) {
            throw new ApiException(HttpStatus.CONFLICT, "임시저장 상태의 문서만 수정할 수 있습니다.");
        }
    }

    // ===================== getter =====================

    public Long getId() { return id; }
    public String getDocNumber() { return docNumber; }
    public DocumentType getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public User getWriter() { return writer; }
    public DocumentStatus getStatus() { return status; }
    public Long getAmount() { return amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public ExpenseCategory getExpenseCategory() { return expenseCategory; }
    public LeaveType getLeaveType() { return leaveType; }
    public LocalDate getLeaveStartDate() { return leaveStartDate; }
    public LocalDate getLeaveEndDate() { return leaveEndDate; }
    public List<ApprovalLine> getLines() { return Collections.unmodifiableList(lines); }
    public List<Attachment> getAttachments() { return Collections.unmodifiableList(attachments); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
