package com.bca.boot.controller;

import com.bca.boot.service.BcaService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author opaw
 */

@RestController
@RequestMapping("/info")
public class GeneralInformationController {
    
    @Autowired private BcaService bcaService;
    
    @GetMapping("/forex")
    public Map<String, Object> getForeignExchange(@RequestParam(required = false) String rateType, @RequestParam(required = false) String currencyCode){
        String path = "/general/rate/forex";
        Map<String, Object> requestParam = new HashMap<>();
        if(rateType != null){ requestParam.put("RateType", rateType); }
        if(currencyCode != null){ requestParam.put("CurrencyCode", currencyCode); }
        String relativeUrl = bcaService.buildUrl(path, null, requestParam);
        return bcaService.request(HttpMethod.GET, relativeUrl, null);
    }
    
    @GetMapping("/deposit")
    public Map<String, Object> getDepositRate(@RequestParam(required = false) String rateType, @RequestParam(required = false) String currencyCode){
        String path = "/general/rate/deposit";
        Map<String, Object> requestParam = new HashMap<>();
        if(rateType != null){ requestParam.put("RateType", rateType); }
        if(currencyCode != null){ requestParam.put("CurrencyCode", currencyCode); }
        String relativeUrl = bcaService.buildUrl(path, null, requestParam);
        return bcaService.request(HttpMethod.GET, relativeUrl, null);
    }
}
