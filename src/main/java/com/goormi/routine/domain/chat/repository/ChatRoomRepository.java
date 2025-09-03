package com.goormi.routine.domain.chat.repository;

import com.goormi.routine.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    Page<ChatRoom> findByIsActiveTrue(Pageable pageable);
    
    Page<ChatRoom> findByGroupIdAndIsActiveTrue(Long groupId, Pageable pageable);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.id IN " +
           "(SELECT cm.roomId FROM ChatMember cm WHERE cm.userId = :userId AND cm.isActive = true) " +
           "AND cr.isActive = true")
    List<ChatRoom> findActiveRoomsByUserId(@Param("userId") Long userId);
    
    Optional<ChatRoom> findByIdAndIsActiveTrue(Long id);
    
    Optional<ChatRoom> findFirstByGroupIdAndIsActiveTrue(Long groupId);
    
    boolean existsByGroupIdAndRoomNameAndIsActiveTrue(Long groupId, String roomName);
}