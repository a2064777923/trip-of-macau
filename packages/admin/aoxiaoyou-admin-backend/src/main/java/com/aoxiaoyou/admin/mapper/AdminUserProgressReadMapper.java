package com.aoxiaoyou.admin.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminUserProgressReadMapper {

    @Select({
            "<script>",
            "SELECT",
            "  e.id AS elementId,",
            "  e.element_code AS elementCode,",
            "  e.element_type AS elementType,",
            "  e.owner_type AS ownerType,",
            "  e.owner_id AS ownerId,",
            "  e.owner_code AS ownerCode,",
            "  e.city_id AS cityId,",
            "  e.sub_map_id AS subMapId,",
            "  e.poi_id AS poiId,",
            "  e.indoor_building_id AS indoorBuildingId,",
            "  e.indoor_floor_id AS indoorFloorId,",
            "  e.story_chapter_id AS storyChapterId,",
            "  e.title_zh AS titleZh,",
            "  e.title_en AS titleEn,",
            "  e.title_zht AS titleZht,",
            "  e.title_pt AS titlePt,",
            "  e.weight_level AS weightLevel,",
            "  e.weight_value AS weightValue,",
            "  e.include_in_exploration AS includeInExploration,",
            "  e.status AS status",
            "FROM exploration_elements e",
            "WHERE e.deleted = 0",
            "<if test='activeOnly'>",
            "  AND e.status = 'published'",
            "  AND e.include_in_exploration = 1",
            "</if>",
            "<choose>",
            "  <when test=\"scopeType == 'global' or scopeId == null\"></when>",
            "  <when test=\"scopeType == 'city'\"> AND e.city_id = #{scopeId}</when>",
            "  <when test=\"scopeType == 'sub_map'\"> AND e.sub_map_id = #{scopeId}</when>",
            "  <when test=\"scopeType == 'poi'\"> AND e.poi_id = #{scopeId}</when>",
            "  <when test=\"scopeType == 'indoor_building'\"> AND e.indoor_building_id = #{scopeId}</when>",
            "  <when test=\"scopeType == 'indoor_floor'\"> AND e.indoor_floor_id = #{scopeId}</when>",
            "  <when test=\"scopeType == 'storyline'\"> AND e.storyline_id = #{scopeId}</when>",
            "  <when test=\"scopeType == 'story_chapter'\"> AND e.story_chapter_id = #{scopeId}</when>",
            "  <when test=\"scopeType == 'task'\"> AND e.owner_type = 'experience_flow_step' AND e.owner_id = #{scopeId}</when>",
            "  <when test=\"scopeType == 'collectible'\"> AND e.owner_type = 'collectible' AND e.owner_id = #{scopeId}</when>",
            "  <when test=\"scopeType == 'reward'\"> AND e.owner_type = 'reward' AND e.owner_id = #{scopeId}</when>",
            "  <when test=\"scopeType == 'media'\"> AND e.owner_type = 'content_asset' AND e.owner_id = #{scopeId}</when>",
            "</choose>",
            "ORDER BY e.sort_order ASC, e.id ASC",
            "</script>"
    })
    List<ProgressElementRow> selectScopeElements(
            @Param("scopeType") String scopeType,
            @Param("scopeId") Long scopeId,
            @Param("activeOnly") boolean activeOnly);

    @Select({
            "SELECT",
            "  e.id AS eventId,",
            "  e.user_id AS userId,",
            "  e.element_id AS elementId,",
            "  e.element_code AS elementCode,",
            "  e.event_type AS eventType,",
            "  e.occurred_at AS occurredAt",
            "FROM user_exploration_events e",
            "WHERE e.user_id = #{userId}",
            "ORDER BY e.occurred_at ASC, e.id ASC"
    })
    List<ProgressEventRow> selectUserEvents(@Param("userId") Long userId);

    @Select({
            "<script>",
            "SELECT",
            "  e.id AS elementId,",
            "  e.element_code AS elementCode,",
            "  e.element_type AS elementType,",
            "  e.owner_type AS ownerType,",
            "  e.owner_id AS ownerId,",
            "  e.owner_code AS ownerCode,",
            "  e.city_id AS cityId,",
            "  e.sub_map_id AS subMapId,",
            "  e.poi_id AS poiId,",
            "  e.indoor_building_id AS indoorBuildingId,",
            "  e.indoor_floor_id AS indoorFloorId,",
            "  e.story_chapter_id AS storyChapterId,",
            "  e.title_zh AS titleZh,",
            "  e.title_en AS titleEn,",
            "  e.title_zht AS titleZht,",
            "  e.title_pt AS titlePt,",
            "  e.weight_level AS weightLevel,",
            "  e.weight_value AS weightValue,",
            "  e.include_in_exploration AS includeInExploration,",
            "  e.status AS status",
            "FROM exploration_elements e",
            "WHERE e.deleted = 0",
            "  AND (",
            "    <if test='elementIds != null and elementIds.size() > 0'>",
            "      e.id IN",
            "      <foreach item='elementId' collection='elementIds' open='(' separator=',' close=')'>",
            "        #{elementId}",
            "      </foreach>",
            "    </if>",
            "    <if test='elementCodes != null and elementCodes.size() > 0'>",
            "      <if test='elementIds != null and elementIds.size() > 0'> OR </if>",
            "      e.element_code IN",
            "      <foreach item='elementCode' collection='elementCodes' open='(' separator=',' close=')'>",
            "        #{elementCode}",
            "      </foreach>",
            "    </if>",
            "  )",
            "ORDER BY e.sort_order ASC, e.id ASC",
            "</script>"
    })
    List<ProgressElementRow> selectElementsByIdsOrCodes(
            @Param("elementIds") List<Long> elementIds,
            @Param("elementCodes") List<String> elementCodes);

    @Select({
            "<script>",
            "SELECT MAX(s.computed_at)",
            "FROM user_exploration_state s",
            "WHERE s.user_id = #{userId}",
            "  AND s.scope_type = #{scopeType}",
            "  <choose>",
            "    <when test='scopeId != null'> AND s.scope_id = #{scopeId}</when>",
            "    <otherwise> AND s.scope_id IS NULL</otherwise>",
            "  </choose>",
            "</script>"
    })
    LocalDateTime selectLastRecomputeTime(
            @Param("userId") Long userId,
            @Param("scopeType") String scopeType,
            @Param("scopeId") Long scopeId);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ProgressElementRow {
        private Long elementId;
        private String elementCode;
        private String elementType;
        private String ownerType;
        private Long ownerId;
        private String ownerCode;
        private Long cityId;
        private Long subMapId;
        private Long poiId;
        private Long indoorBuildingId;
        private Long indoorFloorId;
        private Long storyChapterId;
        private String titleZh;
        private String titleEn;
        private String titleZht;
        private String titlePt;
        private String weightLevel;
        private Integer weightValue;
        private Boolean includeInExploration;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ProgressEventRow {
        private Long eventId;
        private Long userId;
        private Long elementId;
        private String elementCode;
        private String eventType;
        private LocalDateTime occurredAt;
    }
}
