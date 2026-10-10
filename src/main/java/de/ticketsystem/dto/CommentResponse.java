package de.ticketsystem.dto;

import de.ticketsystem.domain.Role;
import de.ticketsystem.domain.TicketComment;
import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long authorId,
        String authorUsername,
        Role authorRole,
        String commentText,
        boolean internal,
        LocalDateTime createdAt
) {
    public static CommentResponse fromEntity(TicketComment comment) {
        if (comment == null) return null;
        return new CommentResponse(
                comment.getId(),
                comment.getAuthor() != null ? comment.getAuthor().getId() : null,
                comment.getAuthor() != null ? comment.getAuthor().getUsername() : null,
                comment.getAuthor() != null ? comment.getAuthor().getRole() : null,
                comment.getCommentText(),
                comment.isInternal(),
                comment.getCreatedAt()
        );
    }
}
