package top.opendsp.ads.engine.service;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.github.javagossip.opendsp.dao.AdGroupDao;
import io.github.javagossip.opendsp.dao.CreativeDao;
import io.github.javagossip.opendsp.dao.AdSlotDao;
import io.github.javagossip.opendsp.model.AdGroup;
import io.github.javagossip.opendsp.model.Creative;
import io.github.javagossip.opendsp.model.AdSlot;
import top.opendsp.proto.rtb.OpenDspRtb.BidRequest;
import top.opendsp.proto.rtb.OpenDspRtb.BidRequest.Imp;

/**
 * 广告匹配服务
 * 负责根据广告位特征匹配合适的广告
 * 
 * @author weiping wang
 */
@Service
public class AdMatchingService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AdMatchingService.class);
    
    @Autowired
    private AdGroupDao adGroupDao;
    
    @Autowired
    private CreativeDao creativeDao;
    
    @Autowired
    private AdSlotDao adSlotDao;
    
    /**
     * 根据竞价请求和广告位匹配广告
     */
    public List<AdCandidate> matchAds(BidRequest request, Imp imp) {
        LOGGER.debug("开始匹配广告, impId: {}", imp.getId());
        
        List<AdCandidate> candidates = new ArrayList<>();
        
        try {
            // 1. 根据广告位类型匹配DSP内部广告位
            List<AdSlot> matchedSlots = matchAdSlots(imp);
            if (CollectionUtils.isEmpty(matchedSlots)) {
                LOGGER.debug("没有匹配的广告位, impId: {}", imp.getId());
                return candidates;
            }
            
            // 2. 遍历匹配的广告位，查找活跃的广告组
            for (AdSlot adSlot : matchedSlots) {
                List<AdGroup> activeAdGroups = findActiveAdGroups(adSlot.getId());
                
                // 3. 为每个广告组查找创意
                for (AdGroup adGroup : activeAdGroups) {
                    List<Creative> creatives = findActiveCreatives(adGroup.getId());
                    
                    // 4. 创建广告候选对象
                    for (Creative creative : creatives) {
                        AdCandidate candidate = new AdCandidate(
                            adGroup.getAdvertiserId(),
                            adGroup.getCampaignId(),
                            adGroup.getId(),
                            creative.getId(),
                            adSlot.getId()
                        );
                        
                        // 设置私有竞价ID
                        candidate.setDealId(adGroup.getDealId());
                        
                        // 设置监测地址
                        candidate.setImpTrackers(parseTrackingUrls(adGroup.getImpTrackingUrls()));
                        candidate.setClickTrackers(parseTrackingUrls(adGroup.getClickTrackingUrls()));
                        
                        // 设置定向数据
                        populateTargetingData(candidate, adGroup);
                        
                        candidates.add(candidate);
                    }
                }
            }
            
            LOGGER.debug("匹配到 {} 个广告候选, impId: {}", candidates.size(), imp.getId());
            
        } catch (Exception e) {
            LOGGER.error("广告匹配异常, impId: {}", imp.getId(), e);
        }
        
        return candidates;
    }
    
    /**
     * 匹配广告位
     */
    private List<AdSlot> matchAdSlots(Imp imp) {
        List<AdSlot> matchedSlots = new ArrayList<>();
        
        try {
            // 获取所有活跃的广告位
            List<AdSlot> allSlots = adSlotDao.list(
                adSlotDao.queryChain().eq(AdSlot::getStatus, 1)
            );
            
            for (AdSlot slot : allSlots) {
                if (isSlotMatched(slot, imp)) {
                    matchedSlots.add(slot);
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("匹配广告位异常", e);
        }
        
        return matchedSlots;
    }
    
    /**
     * 判断广告位是否匹配
     */
    private boolean isSlotMatched(AdSlot slot, Imp imp) {
        // 检查广告位类型
        if (imp.hasBanner() && slot.getAdType() == 1) {
            // Banner广告位匹配
            if (imp.getBanner().hasW() && imp.getBanner().hasH()) {
                return slot.getWidth() != null && slot.getHeight() != null &&
                       slot.getWidth().equals((int)imp.getBanner().getW()) &&
                       slot.getHeight().equals((int)imp.getBanner().getH());
            }
        } else if (imp.hasVideo() && slot.getAdType() == 2) {
            // 视频广告位匹配
            if (imp.getVideo().hasW() && imp.getVideo().hasH()) {
                return slot.getWidth() != null && slot.getHeight() != null &&
                       slot.getWidth().equals((int)imp.getVideo().getW()) &&
                       slot.getHeight().equals((int)imp.getVideo().getH());
            }
        } else if (imp.hasNative() && slot.getAdType() == 3) {
            // 原生广告位匹配
            return true;
        }
        
        return false;
    }
    
    /**
     * 查找活跃的广告组
     */
    private List<AdGroup> findActiveAdGroups(Integer adSlotId) {
        try {
            return adGroupDao.list(
                adGroupDao.queryChain()
                    .eq(AdGroup::getAdSlotId, adSlotId)
                    .eq(AdGroup::getStatus, 1) // 正常状态
                    .le(AdGroup::getBeginDate, new java.util.Date()) // 开始时间 <= 当前时间
                    .ge(AdGroup::getEndDate, new java.util.Date()) // 结束时间 >= 当前时间
            );
        } catch (Exception e) {
            LOGGER.error("查找活跃广告组异常, adSlotId: {}", adSlotId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 查找活跃的创意
     */
    private List<Creative> findActiveCreatives(Integer adGroupId) {
        try {
            return creativeDao.list(
                creativeDao.queryChain()
                    .eq(Creative::getAdGroupId, adGroupId)
                    .eq(Creative::getStatus, 1) // 正常状态
                    .eq(Creative::getAuditStatus, 1) // 审核通过
            );
        } catch (Exception e) {
            LOGGER.error("查找活跃创意异常, adGroupId: {}", adGroupId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 解析监测地址
     */
    private List<String> parseTrackingUrls(String trackingUrls) {
        List<String> urls = new ArrayList<>();
        if (trackingUrls != null && !trackingUrls.isEmpty()) {
            String[] urlArray = trackingUrls.split(",");
            for (String url : urlArray) {
                urls.add(url.trim());
            }
        }
        return urls;
    }
    
    /**
     * 填充定向数据
     */
    private void populateTargetingData(AdCandidate candidate, AdGroup adGroup) {
        candidate.getTargetingData().put("targetAdx", adGroup.getTargetAdx());
        candidate.getTargetingData().put("targetMedia", adGroup.getTargetMedia());
        candidate.getTargetingData().put("targetRegion", adGroup.getTargetRegion());
        candidate.getTargetingData().put("targetOs", adGroup.getTargetOs());
        candidate.getTargetingData().put("targetCarrier", adGroup.getTargetCarrier());
        candidate.getTargetingData().put("targetDeviceType", adGroup.getTargetDeviceType());
        candidate.getTargetingData().put("targetDeviceMake", adGroup.getTargetDeviceMake());
        candidate.getTargetingData().put("targetDeviceModel", adGroup.getTargetDeviceModel());
        candidate.getTargetingData().put("targetConnectionType", adGroup.getTargetConnectionType());
        candidate.getTargetingData().put("freqCapping", adGroup.getFreqCapping());
        candidate.getTargetingData().put("biddingMethod", adGroup.getBiddingMethod());
        candidate.getTargetingData().put("bidPrice", adGroup.getBidPrice());
        candidate.getTargetingData().put("budget", adGroup.getBudget());
    }
} 