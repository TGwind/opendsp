package top.opendsp.ads.engine.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import top.opendsp.proto.rtb.OpenDspRtb.BidRequest;
import top.opendsp.proto.rtb.OpenDspRtb.BidRequest.Imp;

/**
 * 出价服务
 * 负责计算广告的出价
 * 
 * @author weiping wang
 */
@Service
public class PricingService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PricingService.class);
    
    /**
     * 计算广告候选的出价
     */
    public List<AdCandidate> calculateBidPrices(List<AdCandidate> candidates, BidRequest request, Imp imp) {
        LOGGER.debug("开始计算出价, 候选数量: {}", candidates.size());
        
        for (AdCandidate candidate : candidates) {
            float bidPrice = calculateBidPrice(candidate, request, imp);
            candidate.setBidPrice(bidPrice);
            
            LOGGER.debug("计算出价完成, creativeId: {}, bidPrice: {}", candidate.getCreativeId(), bidPrice);
        }
        
        return candidates;
    }
    
    /**
     * 计算单个广告候选的出价
     */
    private float calculateBidPrice(AdCandidate candidate, BidRequest request, Imp imp) {
        try {
            // 获取基础出价
            Long baseBidPrice = (Long) candidate.getTargetingData().get("bidPrice");
            if (baseBidPrice == null || baseBidPrice <= 0) {
                LOGGER.warn("基础出价为空或无效, creativeId: {}", candidate.getCreativeId());
                return 0f;
            }
            
            // 获取出价方式
            Integer biddingMethod = (Integer) candidate.getTargetingData().get("biddingMethod");
            if (biddingMethod == null) {
                biddingMethod = 1; // 默认CPM
            }
            
            float finalBidPrice = baseBidPrice.floatValue();
            
            // 根据出价方式调整出价
            switch (biddingMethod) {
                case 1: // CPM
                    finalBidPrice = calculateCpmBid(candidate, request, imp, baseBidPrice);
                    break;
                case 2: // CPC
                    finalBidPrice = calculateCpcBid(candidate, request, imp, baseBidPrice);
                    break;
                case 3: // CPA
                    finalBidPrice = calculateCpaBid(candidate, request, imp, baseBidPrice);
                    break;
                case 4: // oCPM
                    finalBidPrice = calculateOcpmBid(candidate, request, imp, baseBidPrice);
                    break;
                case 5: // oCPC
                    finalBidPrice = calculateOcpcBid(candidate, request, imp, baseBidPrice);
                    break;
                default:
                    finalBidPrice = baseBidPrice.floatValue();
            }
            
            // 检查广告位底价
            if (imp.hasBidfloor() && finalBidPrice < imp.getBidfloor()) {
                LOGGER.debug("出价低于底价, creativeId: {}, bidPrice: {}, floorPrice: {}", 
                    candidate.getCreativeId(), finalBidPrice, imp.getBidfloor());
                return 0f; // 不参与竞价
            }
            
            return finalBidPrice;
            
        } catch (Exception e) {
            LOGGER.error("计算出价异常, creativeId: {}", candidate.getCreativeId(), e);
            return 0f;
        }
    }
    
    /**
     * 计算CPM出价
     */
    private float calculateCpmBid(AdCandidate candidate, BidRequest request, Imp imp, Long baseBidPrice) {
        // CPM出价策略：基于基础出价和质量分数
        float qualityScore = calculateQualityScore(candidate, request, imp);
        float adjustedBid = baseBidPrice.floatValue() * qualityScore;
        
        // 添加一定的随机性，避免出价过于集中
        float randomFactor = 0.9f + ThreadLocalRandom.current().nextFloat() * 0.2f; // 0.9 - 1.1
        adjustedBid *= randomFactor;
        
        LOGGER.debug("CPM出价计算, creativeId: {}, baseBid: {}, qualityScore: {}, finalBid: {}", 
            candidate.getCreativeId(), baseBidPrice, qualityScore, adjustedBid);
        
        return adjustedBid;
    }
    
    /**
     * 计算CPC出价
     */
    private float calculateCpcBid(AdCandidate candidate, BidRequest request, Imp imp, Long baseBidPrice) {
        // CPC出价策略：基于预估点击率
        float estimatedCtr = estimateClickThroughRate(candidate, request, imp);
        float adjustedBid = baseBidPrice.floatValue() * estimatedCtr * 1000; // 转换为CPM
        
        LOGGER.debug("CPC出价计算, creativeId: {}, baseBid: {}, estimatedCtr: {}, finalBid: {}", 
            candidate.getCreativeId(), baseBidPrice, estimatedCtr, adjustedBid);
        
        return adjustedBid;
    }
    
    /**
     * 计算CPA出价
     */
    private float calculateCpaBid(AdCandidate candidate, BidRequest request, Imp imp, Long baseBidPrice) {
        // CPA出价策略：基于预估转化率
        float estimatedCvr = estimateConversionRate(candidate, request, imp);
        float adjustedBid = baseBidPrice.floatValue() * estimatedCvr * 1000; // 转换为CPM
        
        LOGGER.debug("CPA出价计算, creativeId: {}, baseBid: {}, estimatedCvr: {}, finalBid: {}", 
            candidate.getCreativeId(), baseBidPrice, estimatedCvr, adjustedBid);
        
        return adjustedBid;
    }
    
    /**
     * 计算oCPM出价
     */
    private float calculateOcpmBid(AdCandidate candidate, BidRequest request, Imp imp, Long baseBidPrice) {
        // oCPM出价策略：基于机器学习预测的转化价值
        float predictedValue = predictConversionValue(candidate, request, imp);
        float adjustedBid = baseBidPrice.floatValue() * (predictedValue / 100f); // 归一化
        
        LOGGER.debug("oCPM出价计算, creativeId: {}, baseBid: {}, predictedValue: {}, finalBid: {}", 
            candidate.getCreativeId(), baseBidPrice, predictedValue, adjustedBid);
        
        return adjustedBid;
    }
    
    /**
     * 计算oCPC出价
     */
    private float calculateOcpcBid(AdCandidate candidate, BidRequest request, Imp imp, Long baseBidPrice) {
        // oCPC出价策略：基于机器学习预测的点击价值
        float predictedClickValue = predictClickValue(candidate, request, imp);
        float adjustedBid = baseBidPrice.floatValue() * (predictedClickValue / 100f); // 归一化
        
        LOGGER.debug("oCPC出价计算, creativeId: {}, baseBid: {}, predictedClickValue: {}, finalBid: {}", 
            candidate.getCreativeId(), baseBidPrice, predictedClickValue, adjustedBid);
        
        return adjustedBid;
    }
    
    /**
     * 计算质量分数
     */
    private float calculateQualityScore(AdCandidate candidate, BidRequest request, Imp imp) {
        // 简化的质量分数计算：
        // 1. 历史CTR表现
        // 2. 创意质量
        // 3. 落地页质量
        // 4. 相关性匹配度
        
        float baseScore = 1.0f;
        
        // 基于历史表现调整（这里简化处理）
        // 实际应用中应该从历史数据中获取
        float historicalCtr = getHistoricalCtr(candidate);
        if (historicalCtr > 0.01f) { // 历史CTR > 1%
            baseScore *= 1.2f;
        } else if (historicalCtr > 0.005f) { // 历史CTR > 0.5%
            baseScore *= 1.1f;
        }
        
        // 设备类型匹配度
        if (isDeviceMatched(candidate, request)) {
            baseScore *= 1.1f;
        }
        
        // 地域匹配度
        if (isRegionMatched(candidate, request)) {
            baseScore *= 1.05f;
        }
        
        return Math.min(baseScore, 1.5f); // 最高不超过1.5倍
    }
    
    /**
     * 预估点击率
     */
    private float estimateClickThroughRate(AdCandidate candidate, BidRequest request, Imp imp) {
        // 简化的CTR预估：
        // 实际应用中应该使用机器学习模型
        float baseCtr = 0.01f; // 基础CTR 1%
        
        // 根据设备类型调整
        if (request.hasDevice()) {
            if (request.getDevice().hasDevicetype()) {
                switch (request.getDevice().getDevicetype().getNumber()) {
                    case 1: // 手机
                        baseCtr *= 1.2f;
                        break;
                    case 2: // 平板
                        baseCtr *= 1.0f;
                        break;
                    case 3: // 桌面
                        baseCtr *= 0.8f;
                        break;
                }
            }
        }
        
        // 根据广告位类型调整
        if (imp.hasBanner()) {
            baseCtr *= 1.0f;
        } else if (imp.hasVideo()) {
            baseCtr *= 1.5f;
        } else if (imp.hasNative()) {
            baseCtr *= 1.3f;
        }
        
        return baseCtr;
    }
    
    /**
     * 预估转化率
     */
    private float estimateConversionRate(AdCandidate candidate, BidRequest request, Imp imp) {
        // 简化的CVR预估
        float baseCvr = 0.001f; // 基础CVR 0.1%
        
        // 根据推广类型调整
        Integer promotionType = (Integer) candidate.getTargetingData().get("promotionType");
        if (promotionType != null) {
            switch (promotionType) {
                case 1: // 品牌推广
                    baseCvr *= 0.5f;
                    break;
                case 2: // 效果推广
                    baseCvr *= 2.0f;
                    break;
                case 3: // 电商推广
                    baseCvr *= 1.5f;
                    break;
                case 4: // 应用推广
                    baseCvr *= 1.8f;
                    break;
            }
        }
        
        return baseCvr;
    }
    
    /**
     * 预测转化价值
     */
    private float predictConversionValue(AdCandidate candidate, BidRequest request, Imp imp) {
        // 简化的转化价值预测
        // 实际应用中应该使用机器学习模型
        return 50.0f + ThreadLocalRandom.current().nextFloat() * 50.0f; // 50-100之间
    }
    
    /**
     * 预测点击价值
     */
    private float predictClickValue(AdCandidate candidate, BidRequest request, Imp imp) {
        // 简化的点击价值预测
        return 10.0f + ThreadLocalRandom.current().nextFloat() * 20.0f; // 10-30之间
    }
    
    /**
     * 获取历史CTR
     */
    private float getHistoricalCtr(AdCandidate candidate) {
        // TODO: 从统计数据中获取历史CTR
        // 这里简化处理，返回随机值
        return ThreadLocalRandom.current().nextFloat() * 0.02f; // 0-2%
    }
    
    /**
     * 检查设备类型是否匹配
     */
    private boolean isDeviceMatched(AdCandidate candidate, BidRequest request) {
        // 简化检查
        return true;
    }
    
    /**
     * 检查地域是否匹配
     */
    private boolean isRegionMatched(AdCandidate candidate, BidRequest request) {
        // 简化检查
        return true;
    }
} 