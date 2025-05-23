package com.grading.system.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grading.common.utils.SecurityUtils;
import com.grading.system.domain.GradingContentDetail;
import com.grading.system.mapper.GradingContentDetailMapper;
import com.grading.system.model.bo.GradingUserScore;
import com.grading.system.service.IGradingContentDetailService;
import com.grading.system.service.IGradingContentService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 考核内容值Service业务层处理
 *
 * @author ruoyi
 * @date 2025-04-18
 */
@Service
public class GradingContentDetailServiceImpl extends ServiceImpl<GradingContentDetailMapper, GradingContentDetail> implements IGradingContentDetailService {
    @Resource
    private IGradingContentService iGradingContentService;

    @Override
    public List<GradingContentDetail> listGradingContentDetail(Long gradingUsrId, List<Long> contentIds) {
        Assert.isTrue(ObjectUtils.allNotNull(gradingUsrId, contentIds), "参数不能为空");

        return lambdaQuery().eq(GradingContentDetail::getGradingUserId, gradingUsrId)
                .in(GradingContentDetail::getContentId, contentIds).list();
    }

    @Override
    public List<GradingContentDetail> listByContentIds(List<Long> contentIds) {
        if (CollectionUtils.isEmpty(contentIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(GradingContentDetail::getContentId, contentIds).list();
    }

    @Override
    public List<GradingUserScore> listUserGradingUser(Long gradingId, Long userId) {
        Assert.notNull(gradingId, "考核表ID不能为空");
        return baseMapper.listUserGradingUser(gradingId, userId);
    }
}
