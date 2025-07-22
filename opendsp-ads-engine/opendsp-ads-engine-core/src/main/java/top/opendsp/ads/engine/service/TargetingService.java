package top.opendsp.ads.engine.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import top.opendsp.proto.rtb.OpenDspRtb.BidRequest;
import top.opendsp.proto.rtb.OpenDspRtb.BidRequest.Device;
import top.opendsp.proto.rtb.OpenDspRtb.BidRequest.Geo;

/**
 * 定向服务
 * 负责根据定向条件过滤广告
 * 
 * @author weiping wang
 */
@Service
public class TargetingService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TargetingService.class);
    
    /**
     * 根据定向条件过滤广告候选
     */
    public List<AdCandidate> filterByTargeting(List<AdCandidate> candidates, BidRequest request) {
        LOGGER.debug("开始定向过滤, 候选数量: {}", candidates.size());
        
        List<AdCandidate> filteredCandidates = new ArrayList<>();
        
        for (AdCandidate candidate : candidates) {
            if (isTargetingMatched(candidate, request)) {
                filteredCandidates.add(candidate);
            }
        }
        
        LOGGER.debug("定向过滤完成, 剩余候选数量: {}", filteredCandidates.size());
        return filteredCandidates;
    }
    
    /**
     * 检查定向条件是否匹配
     */
    private boolean isTargetingMatched(AdCandidate candidate, BidRequest request) {
        try {
            // 1. 地域定向检查
            if (!checkRegionTargeting(candidate, request)) {
                LOGGER.debug("地域定向不匹配, creativeId: {}", candidate.getCreativeId());
                return false;
            }
            
            // 2. 操作系统定向检查
            if (!checkOsTargeting(candidate, request)) {
                LOGGER.debug("操作系统定向不匹配, creativeId: {}", candidate.getCreativeId());
                return false;
            }
            
            // 3. 设备类型定向检查
            if (!checkDeviceTypeTargeting(candidate, request)) {
                LOGGER.debug("设备类型定向不匹配, creativeId: {}", candidate.getCreativeId());
                return false;
            }
            
            // 4. 设备品牌定向检查
            if (!checkDeviceMakeTargeting(candidate, request)) {
                LOGGER.debug("设备品牌定向不匹配, creativeId: {}", candidate.getCreativeId());
                return false;
            }
            
            // 5. 设备型号定向检查
            if (!checkDeviceModelTargeting(candidate, request)) {
                LOGGER.debug("设备型号定向不匹配, creativeId: {}", candidate.getCreativeId());
                return false;
            }
            
            // 6. 运营商定向检查
            if (!checkCarrierTargeting(candidate, request)) {
                LOGGER.debug("运营商定向不匹配, creativeId: {}", candidate.getCreativeId());
                return false;
            }
            
            // 7. 网络类型定向检查
            if (!checkConnectionTypeTargeting(candidate, request)) {
                LOGGER.debug("网络类型定向不匹配, creativeId: {}", candidate.getCreativeId());
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            LOGGER.error("定向检查异常, creativeId: {}", candidate.getCreativeId(), e);
            return false;
        }
    }
    
    /**
     * 地域定向检查
     */
    private boolean checkRegionTargeting(AdCandidate candidate, BidRequest request) {
        String targetRegion = (String) candidate.getTargetingData().get("targetRegion");
        
        // 如果没有设置地域定向，则通过
        if (!StringUtils.hasText(targetRegion)) {
            return true;
        }
        
        if (!request.hasDevice() || !request.getDevice().hasGeo()) {
            return false;
        }
        
        Geo geo = request.getDevice().getGeo();
        if (!geo.hasRegionCode()) {
            return false;
        }
        
        // 解析定向地域
        Set<String> targetRegions = parseTargetList(targetRegion);
        String userRegion = String.valueOf(geo.getRegionCode());
        
        return targetRegions.contains(userRegion);
    }
    
    /**
     * 操作系统定向检查
     */
    private boolean checkOsTargeting(AdCandidate candidate, BidRequest request) {
        String targetOs = (String) candidate.getTargetingData().get("targetOs");
        
        // 如果没有设置操作系统定向，则通过
        if (!StringUtils.hasText(targetOs)) {
            return true;
        }
        
        if (!request.hasDevice() || !StringUtils.hasText(request.getDevice().getOs())) {
            return false;
        }
        
        String userOs = request.getDevice().getOs().toLowerCase();
        Set<String> targetOsList = parseTargetList(targetOs);
        
        return targetOsList.contains(userOs);
    }
    
    /**
     * 设备类型定向检查
     */
    private boolean checkDeviceTypeTargeting(AdCandidate candidate, BidRequest request) {
        String targetDeviceType = (String) candidate.getTargetingData().get("targetDeviceType");
        
        // 如果没有设置设备类型定向，则通过
        if (!StringUtils.hasText(targetDeviceType)) {
            return true;
        }
        
        if (!request.hasDevice()) {
            return false;
        }
        
        Device device = request.getDevice();
        String deviceType = getDeviceTypeString(device);
        
        Set<String> targetDeviceTypes = parseTargetList(targetDeviceType);
        
        return targetDeviceTypes.contains(deviceType);
    }
    
    /**
     * 设备品牌定向检查
     */
    private boolean checkDeviceMakeTargeting(AdCandidate candidate, BidRequest request) {
        String targetDeviceMake = (String) candidate.getTargetingData().get("targetDeviceMake");
        
        // 如果没有设置设备品牌定向，则通过
        if (!StringUtils.hasText(targetDeviceMake)) {
            return true;
        }
        
        if (!request.hasDevice() || !StringUtils.hasText(request.getDevice().getMake())) {
            return false;
        }
        
        String userDeviceMake = request.getDevice().getMake().toLowerCase();
        Set<String> targetDeviceMakes = parseTargetList(targetDeviceMake);
        
        return targetDeviceMakes.contains(userDeviceMake);
    }
    
    /**
     * 设备型号定向检查
     */
    private boolean checkDeviceModelTargeting(AdCandidate candidate, BidRequest request) {
        String targetDeviceModel = (String) candidate.getTargetingData().get("targetDeviceModel");
        
        // 如果没有设置设备型号定向，则通过
        if (!StringUtils.hasText(targetDeviceModel)) {
            return true;
        }
        
        if (!request.hasDevice() || !StringUtils.hasText(request.getDevice().getModel())) {
            return false;
        }
        
        String userDeviceModel = request.getDevice().getModel().toLowerCase();
        Set<String> targetDeviceModels = parseTargetList(targetDeviceModel);
        
        return targetDeviceModels.contains(userDeviceModel);
    }
    
    /**
     * 运营商定向检查
     */
    private boolean checkCarrierTargeting(AdCandidate candidate, BidRequest request) {
        String targetCarrier = (String) candidate.getTargetingData().get("targetCarrier");
        
        // 如果没有设置运营商定向，则通过
        if (!StringUtils.hasText(targetCarrier)) {
            return true;
        }
        
        if (!request.hasDevice() || !StringUtils.hasText(request.getDevice().getCarrier())) {
            return false;
        }
        
        String userCarrier = request.getDevice().getCarrier().toLowerCase();
        Set<String> targetCarriers = parseTargetList(targetCarrier);
        
        return targetCarriers.contains(userCarrier);
    }
    
    /**
     * 网络类型定向检查
     */
    private boolean checkConnectionTypeTargeting(AdCandidate candidate, BidRequest request) {
        String targetConnectionType = (String) candidate.getTargetingData().get("targetConnectionType");
        
        // 如果没有设置网络类型定向，则通过
        if (!StringUtils.hasText(targetConnectionType)) {
            return true;
        }
        
        if (!request.hasDevice()) {
            return false;
        }
        
        String connectionType = getConnectionTypeString(request.getDevice());
        Set<String> targetConnectionTypes = parseTargetList(targetConnectionType);
        
        return targetConnectionTypes.contains(connectionType);
    }
    
    /**
     * 解析定向目标列表
     */
    private Set<String> parseTargetList(String targetString) {
        Set<String> targetSet = new HashSet<>();
        if (StringUtils.hasText(targetString)) {
            String[] targets = targetString.split(",");
            for (String target : targets) {
                targetSet.add(target.trim().toLowerCase());
            }
        }
        return targetSet;
    }
    
    /**
     * 获取设备类型字符串
     */
    private String getDeviceTypeString(Device device) {
        if (device.hasDevicetype()) {
            switch (device.getDevicetype().getNumber()) {
                case 1:
                    return "1"; // 手机
                case 2:
                    return "2"; // 平板
                case 3:
                    return "3"; // 桌面电脑
                case 4:
                    return "4"; // 智能电视
                default:
                    return "5"; // 其他
            }
        }
        return "5"; // 其他
    }
    
    /**
     * 获取网络类型字符串
     */
    private String getConnectionTypeString(Device device) {
        if (device.hasConnectiontype()) {
            switch (device.getConnectiontype().getNumber()) {
                case 1:
                    return "wifi";
                case 2:
                    return "2g";
                case 3:
                    return "3g";
                case 4:
                    return "4g";
                case 5:
                    return "5g";
                default:
                    return "unknown";
            }
        }
        return "unknown";
    }
} 