package top.opendsp.ads.engine.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.javagossip.opendsp.dao.AdStatDao;
import io.github.javagossip.opendsp.dao.AdvertiserBalanceDao;
import io.github.javagossip.opendsp.model.AdStat;
import io.github.javagossip.opendsp.model.AdvertiserBalance;

/**
 * 预算服务
 * 负责检查广告组预算和广告主余额
 * 
 * @author weiping wang
 */
@Service
public class BudgetService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetService.class);
    
    @Autowired
    private AdStatDao adStatDao;
    
    @Autowired
    private AdvertiserBalanceDao advertiserBalanceDao;
    
    // 内存中的预算缓存，避免频繁查询数据库
    private final ConcurrentHashMap<String, AtomicLong> budgetCache = new ConcurrentHashMap<>();
    
    /**
     * 根据预算过滤广告候选
     */
    public List<AdCandidate> filterByBudget(List<AdCandidate> candidates) {
        LOGGER.debug("开始预算过滤, 候选数量: {}", candidates.size());
        
        List<AdCandidate> filteredCandidates = new ArrayList<>();
        
        for (AdCandidate candidate : candidates) {
            if (checkBudget(candidate)) {
                filteredCandidates.add(candidate);
            }
        }
        
        LOGGER.debug("预算过滤完成, 剩余候选数量: {}", filteredCandidates.size());
        return filteredCandidates;
    }
    
    /**
     * 检查广告候选的预算是否充足
     */
    private boolean checkBudget(AdCandidate candidate) {
        try {
            // 1. 检查广告主余额
            if (!checkAdvertiserBalance(candidate)) {
                LOGGER.debug("广告主余额不足, advertiserId: {}", candidate.getAdvertiserId());
                return false;
            }
            
            // 2. 检查广告组日预算
            if (!checkAdGroupDailyBudget(candidate)) {
                LOGGER.debug("广告组日预算不足, adGroupId: {}", candidate.getAdGroupId());
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            LOGGER.error("预算检查异常, adGroupId: {}", candidate.getAdGroupId(), e);
            return false;
        }
    }
    
    /**
     * 检查广告主余额
     */
    private boolean checkAdvertiserBalance(AdCandidate candidate) {
        try {
            AdvertiserBalance balance = advertiserBalanceDao.getOne(
                advertiserBalanceDao.queryChain().eq(AdvertiserBalance::getAdvertiserId, candidate.getAdvertiserId())
            );
            
            if (balance == null) {
                LOGGER.warn("广告主余额记录不存在, advertiserId: {}", candidate.getAdvertiserId());
                return false;
            }
            
            // 检查可用余额（总余额 - 冻结余额）
            long availableBalance = balance.getBalance() - balance.getFrozenBalance();
            if (availableBalance <= 0) {
                LOGGER.debug("广告主可用余额不足, advertiserId: {}, 可用余额: {}", 
                    candidate.getAdvertiserId(), availableBalance);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            LOGGER.error("检查广告主余额异常, advertiserId: {}", candidate.getAdvertiserId(), e);
            return false;
        }
    }
    
    /**
     * 检查广告组日预算
     */
    private boolean checkAdGroupDailyBudget(AdCandidate candidate) {
        try {
            Long budget = (Long) candidate.getTargetingData().get("budget");
            if (budget == null || budget <= 0) {
                LOGGER.debug("广告组预算未设置或为0, adGroupId: {}", candidate.getAdGroupId());
                return false;
            }
            
            // 获取今日已消费金额
            long todaySpend = getTodaySpend(candidate.getAdGroupId());
            
            // 检查是否超过预算
            if (todaySpend >= budget) {
                LOGGER.debug("广告组今日预算已用完, adGroupId: {}, 预算: {}, 已消费: {}", 
                    candidate.getAdGroupId(), budget, todaySpend);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            LOGGER.error("检查广告组日预算异常, adGroupId: {}", candidate.getAdGroupId(), e);
            return false;
        }
    }
    
    /**
     * 获取今日已消费金额
     */
    private long getTodaySpend(Integer adGroupId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String cacheKey = "spend_" + adGroupId + "_" + today;
        
        // 先从缓存中获取
        AtomicLong cachedSpend = budgetCache.get(cacheKey);
        if (cachedSpend != null) {
            return cachedSpend.get();
        }
        
        try {
            // 从数据库查询今日消费
            List<AdStat> stats = adStatDao.list(
                adStatDao.queryChain()
                    .eq(AdStat::getAdGroupId, adGroupId)
                    .eq(AdStat::getStatDate, today)
            );
            
            long totalSpend = stats.stream()
                .mapToLong(AdStat::getCost)
                .sum();
            
            // 存入缓存
            budgetCache.put(cacheKey, new AtomicLong(totalSpend));
            
            return totalSpend;
            
        } catch (Exception e) {
            LOGGER.error("获取今日消费异常, adGroupId: {}", adGroupId, e);
            return 0;
        }
    }
    
    /**
     * 更新消费缓存
     */
    public void updateSpendCache(Integer adGroupId, long cost) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String cacheKey = "spend_" + adGroupId + "_" + today;
        
        AtomicLong cachedSpend = budgetCache.computeIfAbsent(cacheKey, k -> new AtomicLong(0));
        cachedSpend.addAndGet(cost);
        
        LOGGER.debug("更新消费缓存, adGroupId: {}, 增加消费: {}, 总消费: {}", 
            adGroupId, cost, cachedSpend.get());
    }
    
    /**
     * 清理过期缓存
     */
    public void cleanExpiredCache() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        budgetCache.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            // 如果不是今天的缓存，则清理
            return !key.contains(today);
        });
        
        LOGGER.debug("清理过期预算缓存完成");
    }
} 