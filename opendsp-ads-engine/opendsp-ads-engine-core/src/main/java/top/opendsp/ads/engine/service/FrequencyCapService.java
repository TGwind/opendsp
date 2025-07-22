package top.opendsp.ads.engine.service;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import io.github.javagossip.opendsp.model.FreqCapping;

/**
 * 频次控制服务
 * 负责检查用户频次限制
 * 
 * @author weiping wang
 */
@Service
public class FrequencyCapService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FrequencyCapService.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 根据频次控制过滤广告候选
     */
    public List<AdCandidate> filterByFrequency(List<AdCandidate> candidates, String userId) {
        LOGGER.debug("开始频次控制过滤, 候选数量: {}, userId: {}", candidates.size(), userId);
        
        List<AdCandidate> filteredCandidates = new ArrayList<>();
        
        // 如果没有用户ID，跳过频次控制
        if (!StringUtils.hasText(userId)) {
            LOGGER.debug("用户ID为空，跳过频次控制");
            return candidates;
        }
        
        for (AdCandidate candidate : candidates) {
            if (checkFrequency(candidate, userId)) {
                filteredCandidates.add(candidate);
            }
        }
        
        LOGGER.debug("频次控制过滤完成, 剩余候选数量: {}", filteredCandidates.size());
        return filteredCandidates;
    }
    
    /**
     * 检查频次控制
     */
    private boolean checkFrequency(AdCandidate candidate, String userId) {
        try {
            Object freqCappingObj = candidate.getTargetingData().get("freqCapping");
            if (freqCappingObj == null) {
                // 没有设置频次控制，通过
                return true;
            }
            
            FreqCapping freqCapping = parseFreqCapping(freqCappingObj);
            if (freqCapping == null || freqCapping.getFreq() <= 0) {
                // 频次控制配置无效，通过
                return true;
            }
            
            // 检查用户对该广告组的展示频次
            int currentFreq = getUserAdGroupFrequency(userId, candidate.getAdGroupId(), freqCapping.getPeriod());
            
            if (currentFreq >= freqCapping.getFreq()) {
                LOGGER.debug("用户频次超限, userId: {}, adGroupId: {}, 当前频次: {}, 限制频次: {}", 
                    userId, candidate.getAdGroupId(), currentFreq, freqCapping.getFreq());
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            LOGGER.error("频次控制检查异常, adGroupId: {}, userId: {}", candidate.getAdGroupId(), userId, e);
            return true; // 异常时放行
        }
    }
    
    /**
     * 解析频次控制配置
     */
    private FreqCapping parseFreqCapping(Object freqCappingObj) {
        try {
            if (freqCappingObj instanceof FreqCapping) {
                return (FreqCapping) freqCappingObj;
            } else if (freqCappingObj instanceof String) {
                String jsonStr = (String) freqCappingObj;
                if (StringUtils.hasText(jsonStr)) {
                    JSONObject jsonObject = JSON.parseObject(jsonStr);
                    FreqCapping freqCapping = new FreqCapping();
                    freqCapping.setPeriod(jsonObject.getString("period"));
                    freqCapping.setFreq(jsonObject.getInteger("freq"));
                    return freqCapping;
                }
            }
        } catch (Exception e) {
            LOGGER.error("解析频次控制配置异常: {}", freqCappingObj, e);
        }
        return null;
    }
    
    /**
     * 获取用户对广告组的展示频次
     */
    private int getUserAdGroupFrequency(String userId, Integer adGroupId, String period) {
        String key = buildFrequencyKey(userId, adGroupId, period);
        
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return Integer.parseInt(value.toString());
            }
        } catch (Exception e) {
            LOGGER.error("获取用户频次异常, key: {}", key, e);
        }
        
        return 0;
    }
    
    /**
     * 增加用户广告组展示频次
     */
    public void incrementUserAdGroupFrequency(String userId, Integer adGroupId, String period) {
        if (!StringUtils.hasText(userId) || adGroupId == null) {
            return;
        }
        
        String key = buildFrequencyKey(userId, adGroupId, period);
        
        try {
            redisTemplate.opsForValue().increment(key);
            
            // 设置过期时间
            long expireTime = getExpireTime(period);
            if (expireTime > 0) {
                redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            }
            
            LOGGER.debug("增加用户频次, userId: {}, adGroupId: {}, period: {}", userId, adGroupId, period);
            
        } catch (Exception e) {
            LOGGER.error("增加用户频次异常, key: {}", key, e);
        }
    }
    
    /**
     * 构建频次控制Redis Key
     */
    private String buildFrequencyKey(String userId, Integer adGroupId, String period) {
        return String.format("freq:%s:%d:%s", userId, adGroupId, period);
    }
    
    /**
     * 获取过期时间
     */
    private long getExpireTime(String period) {
        if (period == null) {
            return 0;
        }
        
        switch (period.toLowerCase()) {
            case "hour":
                return 3600; // 1小时
            case "day":
                return 86400; // 24小时
            case "week":
                return 604800; // 7天
            case "month":
                return 2592000; // 30天
            default:
                return 86400; // 默认24小时
        }
    }
} 