package com.aoxiaoyou.admin.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface UserExplorationEventAdminMapper {

    @Select({
            "SELECT",
            "  e.id AS id,",
            "  e.user_id AS userId,",
            "  e.element_id AS elementId,",
            "  e.element_code AS elementCode,",
            "  e.event_type AS eventType,",
            "  e.event_source AS eventSource,",
            "  e.client_event_id AS clientEventId,",
            "  CAST(e.event_payload_json AS CHAR) AS eventPayloadJson,",
            "  e.duplicate_marked AS duplicateMarked,",
            "  e.duplicate_of_event_id AS duplicateOfEventId,",
            "  CAST(e.repair_note_json AS CHAR) AS repairNoteJson,",
            "  e.occurred_at AS occurredAt",
            "FROM user_exploration_events e",
            "WHERE e.id = #{eventId}",
            "LIMIT 1"
    })
    EventRecord selectEventById(@Param("eventId") Long eventId);

    @Update({
            "UPDATE user_exploration_events",
            "SET element_id = #{elementId},",
            "    element_code = #{elementCode},",
            "    repair_note_json = #{repairNoteJson}",
            "WHERE id = #{eventId}"
    })
    int updateEventLink(
            @Param("eventId") Long eventId,
            @Param("elementId") Long elementId,
            @Param("elementCode") String elementCode,
            @Param("repairNoteJson") String repairNoteJson);

    @Update({
            "UPDATE user_exploration_events",
            "SET duplicate_marked = 1,",
            "    duplicate_of_event_id = #{duplicateOfEventId},",
            "    repair_note_json = #{repairNoteJson}",
            "WHERE id = #{eventId}"
    })
    int markDuplicate(
            @Param("eventId") Long eventId,
            @Param("duplicateOfEventId") Long duplicateOfEventId,
            @Param("repairNoteJson") String repairNoteJson);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class EventRecord {
        private Long id;
        private Long userId;
        private Long elementId;
        private String elementCode;
        private String eventType;
        private String eventSource;
        private String clientEventId;
        private String eventPayloadJson;
        private Boolean duplicateMarked;
        private Long duplicateOfEventId;
        private String repairNoteJson;
        private LocalDateTime occurredAt;
    }
}
