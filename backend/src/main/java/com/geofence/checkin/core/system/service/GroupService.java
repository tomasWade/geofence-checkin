package com.geofence.checkin.core.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.system.dto.GroupSaveReq;
import com.geofence.checkin.core.system.dto.GroupVO;
import com.geofence.checkin.core.system.entity.SysGroup;
import com.geofence.checkin.core.system.entity.SysGroupMember;
import com.geofence.checkin.core.system.mapper.SysGroupMapper;
import com.geofence.checkin.core.system.mapper.SysGroupMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 分组管理：增删改查 + 成员全量替换维护。
 */
@Service
@RequiredArgsConstructor
public class GroupService {

    private final SysGroupMapper groupMapper;
    private final SysGroupMemberMapper groupMemberMapper;
    private final UserService userService;

    public List<GroupVO> listAll() {
        List<SysGroup> groups = groupMapper.selectList(
                new LambdaQueryWrapper<SysGroup>().orderByAsc(SysGroup::getId));
        List<Long> allUserIds = new ArrayList<>();
        for (SysGroup g : groups) {
            if (g.getManagerUserId() != null) {
                allUserIds.add(g.getManagerUserId());
            }
        }
        Map<Long, String> names = userService.nameMap(allUserIds);
        return groups.stream().map(g -> toVO(g, names)).toList();
    }

    @Transactional
    public Long create(GroupSaveReq req) {
        SysGroup group = new SysGroup();
        group.setName(req.getName());
        group.setManagerUserId(req.getManagerUserId());
        groupMapper.insert(group);
        saveMembers(group.getId(), req.getMemberUserIds());
        return group.getId();
    }

    @Transactional
    public void update(GroupSaveReq req) {
        if (req.getId() == null) {
            throw new BusinessException("分组 id 不能为空");
        }
        SysGroup group = groupMapper.selectById(req.getId());
        if (group == null) {
            throw new BusinessException("分组不存在");
        }
        group.setName(req.getName());
        group.setManagerUserId(req.getManagerUserId());
        groupMapper.updateById(group);
        saveMembers(group.getId(), req.getMemberUserIds());
    }

    @Transactional
    public void delete(Long id) {
        groupMapper.deleteById(id);
        groupMemberMapper.delete(new LambdaQueryWrapper<SysGroupMember>()
                .eq(SysGroupMember::getGroupId, id));
    }

    private void saveMembers(Long groupId, List<Long> memberUserIds) {
        groupMemberMapper.delete(new LambdaQueryWrapper<SysGroupMember>()
                .eq(SysGroupMember::getGroupId, groupId));
        if (memberUserIds != null) {
            for (Long userId : memberUserIds) {
                SysGroupMember m = new SysGroupMember();
                m.setGroupId(groupId);
                m.setUserId(userId);
                groupMemberMapper.insert(m);
            }
        }
    }

    private GroupVO toVO(SysGroup g, Map<Long, String> names) {
        GroupVO vo = new GroupVO();
        vo.setId(g.getId());
        vo.setName(g.getName());
        vo.setManagerUserId(g.getManagerUserId());
        vo.setManagerName(names.get(g.getManagerUserId()));
        vo.setCreatedAt(g.getCreatedAt());
        List<SysGroupMember> members = groupMemberMapper.selectList(
                new LambdaQueryWrapper<SysGroupMember>().eq(SysGroupMember::getGroupId, g.getId()));
        vo.setMemberUserIds(members.stream().map(SysGroupMember::getUserId).toList());
        return vo;
    }
}
