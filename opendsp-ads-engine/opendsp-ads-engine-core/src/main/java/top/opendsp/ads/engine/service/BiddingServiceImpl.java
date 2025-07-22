package top.opendsp.ads.engine.service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.grpc.stub.StreamObserver;
import top.opendsp.proto.rtb.OpenDspRtb.BidRequest;
import top.opendsp.proto.rtb.OpenDspRtb.BidResponse;
import top.opendsp.proto.rtb.OpenDspRtb.BidRequest.Imp;
import top.opendsp.proto.rtb.OpenDspRtb.BidRequest.Device;
import top.opendsp.proto.rtb.OpenDspRtb.BidRequest.User;
import top.opendsp.proto.rtb.OpenDspRtb.BidResponse.SeatBid;
import top.opendsp.proto.rtb.OpenDspRtb.BidResponse.SeatBid.Bid;
import top.opendsp.proto.rtb.service.BiddingServiceGrpc.BiddingServiceImplBase;

/**
 * 广告投放引擎实现
 * 
 * @author weiping wang
 */
@Service
public class BiddingServiceImpl extends BiddingServiceImplBase {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("opendsp-bidder");
    
    @Autowired
    private AdMatchingService adMatchingService;
    
    @Autowired
    private PricingService pricingService;
    
    @Autowired
    private TargetingService targetingService;
    
    @Autowired
    private BudgetService budgetService;
    
    @Autowired
    private FrequencyCapService frequencyCapService;
    
    @Autowired
    private BidLogService bidLogService;
    
    @Override
    public void bid(BidRequest request, StreamObserver<BidResponse> responseObserver) {
        String requestId = request.getId();
        long startTime = System.currentTimeMillis();
        
        try {
            LOGGER.info("开始处理竞价请求, requestId: {}", requestId);
            
            // 验证请求
            if (!validateRequest(request)) {
                LOGGER.warn("竞价请求验证失败, requestId: {}", requestId);
                responseObserver.onNext(buildEmptyResponse(requestId));
                responseObserver.onCompleted();
                return;
            }
            
            // 处理每个广告位
            List<SeatBid> seatBids = new ArrayList<>();
            for (Imp imp : request.getImpList()) {
                SeatBid seatBid = processImpression(request, imp);
                if (seatBid != null && !seatBid.getBidList().isEmpty()) {
                    seatBids.add(seatBid);
                }
            }
            
            // 构建响应
            BidResponse response = buildBidResponse(requestId, seatBids);
            
            long processingTime = System.currentTimeMillis() - startTime;
            LOGGER.info("竞价处理完成, requestId: {}, 处理时间: {}ms, 返回广告数: {}", 
                requestId, processingTime, countTotalBids(seatBids));
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            LOGGER.error("竞价处理异常, requestId: {}", requestId, e);
            responseObserver.onNext(buildEmptyResponse(requestId));
            responseObserver.onCompleted();
        }
    }
    
    /**
     * 验证竞价请求
     */
    private boolean validateRequest(BidRequest request) {
        if (Strings.isNullOrEmpty(request.getId())) {
            LOGGER.warn("竞价请求ID为空");
            return false;
        }
        
        if (request.getImpList().isEmpty()) {
            LOGGER.warn("竞价请求没有广告位");
            return false;
        }
        
        // 验证设备信息
        if (!request.hasDevice()) {
            LOGGER.warn("竞价请求缺少设备信息");
            return false;
        }
        
        return true;
    }
    
    /**
     * 处理单个广告位的竞价
     */
    private SeatBid processImpression(BidRequest request, Imp imp) {
        String impId = imp.getId();
        LOGGER.debug("处理广告位竞价, impId: {}", impId);
        
        try {
            // 1. 广告匹配 - 根据广告位特征匹配合适的广告
            List<AdCandidate> candidates = adMatchingService.matchAds(request, imp);
            if (CollectionUtils.isEmpty(candidates)) {
                LOGGER.debug("没有找到匹配的广告, impId: {}", impId);
                return null;
            }
            
            // 2. 定向过滤 - 根据定向条件过滤广告
            List<AdCandidate> targetedCandidates = targetingService.filterByTargeting(candidates, request);
            if (CollectionUtils.isEmpty(targetedCandidates)) {
                LOGGER.debug("定向过滤后没有广告, impId: {}", impId);
                return null;
            }
            
            // 3. 预算检查 - 检查广告组预算
            List<AdCandidate> budgetFilteredCandidates = budgetService.filterByBudget(targetedCandidates);
            if (CollectionUtils.isEmpty(budgetFilteredCandidates)) {
                LOGGER.debug("预算检查后没有广告, impId: {}", impId);
                return null;
            }
            
            // 4. 频次控制 - 检查用户频次限制
            List<AdCandidate> frequencyFilteredCandidates = frequencyCapService.filterByFrequency(
                budgetFilteredCandidates, request.hasUser() ? request.getUser().getId() : null);
            if (CollectionUtils.isEmpty(frequencyFilteredCandidates)) {
                LOGGER.debug("频次控制后没有广告, impId: {}", impId);
                return null;
            }
            
            // 5. 竞价计算 - 计算每个广告的出价
            List<AdCandidate> pricedCandidates = pricingService.calculateBidPrices(
                frequencyFilteredCandidates, request, imp);
            
            // 6. 排序和选择 - 按出价排序，选择最高价
            pricedCandidates.sort((a, b) -> Float.compare(b.getBidPrice(), a.getBidPrice()));
            
            // 7. 构建竞价响应
            List<Bid> bids = new ArrayList<>();
            for (int i = 0; i < Math.min(pricedCandidates.size(), 3); i++) { // 最多返回3个广告
                AdCandidate candidate = pricedCandidates.get(i);
                
                // 记录竞价日志
                bidLogService.logBid(request.getId(), candidate, true);
                
                Bid bid = Bid.newBuilder()
                    .setId(generateBidId())
                    .setImpid(impId)
                    .setPrice(candidate.getBidPrice())
                    .setCrid(String.valueOf(candidate.getCreativeId()))
                    .setDealid(candidate.getDealId() != null ? candidate.getDealId() : "")
                    .addAllImptrackers(candidate.getImpTrackers())
                    .addAllClktrackers(candidate.getClickTrackers())
                    .build();
                
                bids.add(bid);
                
                LOGGER.debug("生成竞价, impId: {}, creativeId: {}, bidPrice: {}", 
                    impId, candidate.getCreativeId(), candidate.getBidPrice());
            }
            
            if (!bids.isEmpty()) {
                return SeatBid.newBuilder()
                    .addAllBid(bids)
                    .setSeat("opendsp")
                    .build();
            }
            
        } catch (Exception e) {
            LOGGER.error("处理广告位竞价异常, impId: {}", impId, e);
        }
        
        return null;
    }
    
    /**
     * 构建竞价响应
     */
    private BidResponse buildBidResponse(String requestId, List<SeatBid> seatBids) {
        return BidResponse.newBuilder()
            .setId(requestId)
            .addAllSeatbid(seatBids)
            .setBidid(generateBidId())
            .build();
    }
    
    /**
     * 构建空响应
     */
    private BidResponse buildEmptyResponse(String requestId) {
        return BidResponse.newBuilder()
            .setId(requestId)
            .setBidid(generateBidId())
            .build();
    }
    
    /**
     * 生成竞价ID
     */
    private String generateBidId() {
        return "bid_" + System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(10000);
    }
    
    /**
     * 统计总竞价数
     */
    private int countTotalBids(List<SeatBid> seatBids) {
        return seatBids.stream()
            .mapToInt(seatBid -> seatBid.getBidList().size())
            .sum();
    }
}

/**
 * 广告候选对象
 */
class AdCandidate {
    private Integer advertiserId;
    private Integer campaignId;
    private Integer adGroupId;
    private Integer creativeId;
    private Integer adSlotId;
    private String dealId;
    private Float bidPrice;
    private List<String> impTrackers;
    private List<String> clickTrackers;
    private Map<String, Object> targetingData;
    
    // 构造函数
    public AdCandidate(Integer advertiserId, Integer campaignId, Integer adGroupId, 
                      Integer creativeId, Integer adSlotId) {
        this.advertiserId = advertiserId;
        this.campaignId = campaignId;
        this.adGroupId = adGroupId;
        this.creativeId = creativeId;
        this.adSlotId = adSlotId;
        this.impTrackers = new ArrayList<>();
        this.clickTrackers = new ArrayList<>();
        this.targetingData = new HashMap<>();
    }
    
    // Getters and Setters
    public Integer getAdvertiserId() { return advertiserId; }
    public void setAdvertiserId(Integer advertiserId) { this.advertiserId = advertiserId; }
    
    public Integer getCampaignId() { return campaignId; }
    public void setCampaignId(Integer campaignId) { this.campaignId = campaignId; }
    
    public Integer getAdGroupId() { return adGroupId; }
    public void setAdGroupId(Integer adGroupId) { this.adGroupId = adGroupId; }
    
    public Integer getCreativeId() { return creativeId; }
    public void setCreativeId(Integer creativeId) { this.creativeId = creativeId; }
    
    public Integer getAdSlotId() { return adSlotId; }
    public void setAdSlotId(Integer adSlotId) { this.adSlotId = adSlotId; }
    
    public String getDealId() { return dealId; }
    public void setDealId(String dealId) { this.dealId = dealId; }
    
    public Float getBidPrice() { return bidPrice; }
    public void setBidPrice(Float bidPrice) { this.bidPrice = bidPrice; }
    
    public List<String> getImpTrackers() { return impTrackers; }
    public void setImpTrackers(List<String> impTrackers) { this.impTrackers = impTrackers; }
    
    public List<String> getClickTrackers() { return clickTrackers; }
    public void setClickTrackers(List<String> clickTrackers) { this.clickTrackers = clickTrackers; }
    
    public Map<String, Object> getTargetingData() { return targetingData; }
    public void setTargetingData(Map<String, Object> targetingData) { this.targetingData = targetingData; }
}
