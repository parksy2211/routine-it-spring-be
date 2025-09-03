package com.goormi.routine.domain.chat.repository;

import com.goormi.routine.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    Page<ChatMessage> findByRoomIdOrderBySentAtDesc(Long roomId, Pageable pageable);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.roomId = :roomId AND cm.id < :beforeMessageId ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findByRoomIdAndIdLessThanOrderBySentAtDesc(@Param("roomId") Long roomId, 
                                                                  @Param("beforeMessageId") Long beforeMessageId, 
                                                                  Pageable pageable);
    
    List<ChatMessage> findByRoomIdAndSentAtAfterOrderBySentAt(Long roomId, LocalDateTime after);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.roomId = :roomId")
    long countByRoomId(@Param("roomId") Long roomId);
    
    void deleteByRoomIdAndSentAtBefore(Long roomId, LocalDateTime before);
}