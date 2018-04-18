package com.bca.boot.controller;

import com.bca.boot.service.BcaService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
@RequestMapping("/bussiness")
public class BussinessBankingController {
    
    @Autowired private BcaService bcaService;
    private final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    
    @GetMapping("/balance")
    public Map<String, Object> getBalance(@RequestParam(required = false) String account) {
        if(account == null)
            account = bcaService.properties.getAccounts().get(0);
        String path = "/banking/v3/corporates/{}/accounts/{}";
        List<String> variables = new ArrayList<>();
        variables.add(bcaService.properties.getCorpId());
        variables.add(account);
        String relativeUrl = bcaService.buildUrl(path, variables, null);
        return bcaService.request(HttpMethod.GET, relativeUrl, null);
    }
    
    @GetMapping("/statement")
    public Map<String, Object> getStatement(@RequestParam(required = false) String account, @RequestParam(required = false) String start, @RequestParam(required = false) String end){
        if(account == null)
            account = bcaService.properties.getAccounts().get(0);
        
        Date today = new Date();
        Calendar days31 = Calendar.getInstance();
        days31.setTime(today);
        days31.add(Calendar.DATE, -31);
        
        String strToday = dateFormatter.format(today);
        String strDays31 = dateFormatter.format(days31.getTime());
        
        String path = "/banking/v3/corporates/{}/accounts/{}/statements";
        List<String> variables = new ArrayList<>();
        variables.add(bcaService.properties.getCorpId());
        variables.add(account);
        
        Map<String, Object> requestParam = new HashMap<>();
        requestParam.put("StartDate", start != null ? start : strDays31);
        requestParam.put("EndDate", end != null ? end : strToday);
        
        String relativeUrl = bcaService.buildUrl(path, variables, requestParam);
        return bcaService.request(HttpMethod.GET, relativeUrl, null);
    }
    
    @GetMapping("/transfer")
    public Map<String, Object> transfer(@RequestParam(required = false) String account, @RequestParam String to, @RequestParam String amount){
        if(account == null)
            account = bcaService.properties.getAccounts().get(0);
        Map<String, Object> params = new HashMap<>();
        params.put("CorporateID", bcaService.properties.getCorpId());
        params.put("SourceAccountNumber", account);
        params.put("TransactionID", randNumber());
        params.put("TransactionDate", dateFormatter.format(new Date()));
        params.put("ReferenceID", randNumber());
        params.put("CurrencyCode", "IDR");
        params.put("Amount", amount);
        params.put("BeneficiaryAccountNumber", to);
        params.put("Remark1", "Online Transfer");
        params.put("Remark2", "Test Transfer");
        String relativeUrl = bcaService.buildUrl("/banking/corporates/transfers", null, null);
        return bcaService.request(HttpMethod.POST,relativeUrl, params);
    }
    
    public String randNumber(){
        return String.valueOf(new Random().nextInt(999_999));
    }
}
