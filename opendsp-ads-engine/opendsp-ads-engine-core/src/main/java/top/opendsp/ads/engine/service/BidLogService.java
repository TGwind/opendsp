package top.opendsp.ads.engine.service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.javagossip.opendsp.dao.BidLogDao;
import io.github.javagossip.opendsp.model.BidLog;

/**
 * 竞价日志服务
 * 负责记录竞价日志
 * 
 * @author weiping wang
 */
@Service
public class BidLogService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BidLogService.class);
    
    @Autowired
    private BidLogDao bidLogDao;
    
    /**
     * 记录竞价日志
     */
    public void logBid(String requestId, AdCandidate candidate, boolean isWin) {
        // 异步记录日志，避免影响竞价性能
        CompletableFuture.runAsync(() -> {
            try {
                BidLog bidLog = BidLog.builder()
                    .requestId(requestId)
                    .advertiserId(candidate.getAdvertiserId())
                    .campaignId(candidate.getCampaignId())
                    .adGroupId(candidate.getAdGroupId())
                    .creativeId(candidate.getCreativeId())
                    .adSlotId(candidate.getAdSlotId())
                    .bidPrice(Math.round(candidate.getBidPrice()))
                    .isWin(isWin ? 1 : 0)
                    .createTime(LocalDateTime.now())
                    .build();
                
                bidLogDao.save(bidLog);
                
                LOGGER.debug("记录竞价日志, requestId: {}, creativeId: {}, bidPrice: {}, isWin: {}", 
                    requestId, candidate.getCreativeId(), candidate.getBidPrice(), isWin);
                
            } catch (Exception e) {
                LOGGER.error("记录竞价日志异常, requestId: {}, creativeId: {}", 
                    requestId, candidate.getCreativeId(), e);
            }
        });
    }
    
    /**
     * 记录胜出日志
     */
    public void logWin(String requestId, AdCandidate candidate, float winPrice) {
        CompletableFuture.runAsync(() -> {
            try {
                // 更新竞价日志为胜出状态
                BidLog updateLog = BidLog.builder()
                    .requestId(requestId)
                    .creativeId(candidate.getCreativeId())
                    .winPrice(Math.round(winPrice))
                    .isWin(1)
                    .build();
                
                bidLogDao.updateChain()
                    .set(BidLog::getWinPrice, Math.round(winPrice))
                    .set(BidLog::getIsWin, 1)
                    .eq(BidLog::getRequestId, requestId)
                    .eq(BidLog::getCreativeId, candidate.getCreativeId())
                    .update();
                
                LOGGER.debug("记录胜出日志, requestId: {}, creativeId: {}, winPrice: {}", 
                    requestId, candidate.getCreativeId(), winPrice);
                
            } catch (Exception e) {
                LOGGER.error("记录胜出日志异常, requestId: {}, creativeId: {}", 
                    requestId, candidate.getCreativeId(), e);
            }
        });
    }
} 