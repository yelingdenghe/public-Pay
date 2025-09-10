package com.ruoyi.backend.service;

import com.ruoyi.backend.dto.ExpenseRequest;
import com.ruoyi.backend.dto.ExpenseResponse;
import com.ruoyi.backend.dto.UserBalanceResponse;
import com.ruoyi.backend.entity.*;
import com.ruoyi.backend.enums.SplitType;
import com.ruoyi.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 费用服务类
 */
@Service
public class ExpenseService {
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private ExpenseShareRepository expenseShareRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ExpenseCategoryRepository categoryRepository;
    
    @Autowired
    private ExpensePaymentRepository expensePaymentRepository;
    
    /**
     * 添加费用记录
     */
    @Transactional
    public ExpenseResponse addExpense(ExpenseRequest request) {
        // 验证付款人
        List<ExpenseRequest.PaymentRequest> paymentRequests = request.getPayments();
        if (paymentRequests == null || paymentRequests.isEmpty()) {
            throw new RuntimeException("付款人列表不能为空");
        }
        
        // 验证所有付款人存在
        List<Long> payerIds = paymentRequests.stream()
                .map(ExpenseRequest.PaymentRequest::getPayerId)
                .collect(Collectors.toList());
        List<User> payers = userRepository.findAllById(payerIds);
        if (payers.size() != payerIds.size()) {
            throw new RuntimeException("部分付款人不存在");
        }
        
        // 验证付款总金额
        BigDecimal totalPaymentAmount = paymentRequests.stream()
                .map(ExpenseRequest.PaymentRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalPaymentAmount.compareTo(request.getAmount()) != 0) {
            throw new RuntimeException("付款总金额与费用金额不匹配");
        }
        
        // 验证分摊用户
        List<User> shareUsers = userRepository.findAllById(request.getShareUserIds());
        if (shareUsers.size() != request.getShareUserIds().size()) {
            throw new RuntimeException("部分分摊用户不存在");
        }
        
        // 验证费用分类
        ExpenseCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("费用分类不存在: " + request.getCategoryId()));
        }
        
        // 创建费用记录
        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(category);
        expense.setSplitType(SplitType.valueOf(request.getSplitType()));
        expense.setExpenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDateTime.now());
        
        expense = expenseRepository.save(expense);
        
        // 创建付款记录
        List<ExpensePayment> payments = new ArrayList<>();
        for (ExpenseRequest.PaymentRequest paymentRequest : paymentRequests) {
            User payer = payers.stream()
                    .filter(p -> p.getId().equals(paymentRequest.getPayerId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("付款人不存在: " + paymentRequest.getPayerId()));
            
            ExpensePayment payment = new ExpensePayment();
            payment.setExpense(expense);
            payment.setPayer(payer);
            payment.setAmount(paymentRequest.getAmount());
            payments.add(payment);
        }
        expensePaymentRepository.saveAll(payments);
        
        // 创建分摊记录
        List<ExpenseShare> shares = new ArrayList<>();
        
        if (SplitType.EQUAL.name().equals(request.getSplitType())) {
            // 平均分摊
            BigDecimal shareAmount = request.getAmount().divide(
                    BigDecimal.valueOf(shareUsers.size()), 2, RoundingMode.HALF_UP);
            
            for (User user : shareUsers) {
                ExpenseShare share = new ExpenseShare();
                share.setExpense(expense);
                share.setUser(user);
                share.setAmount(shareAmount);
                share.setIsPaid(false);
                shares.add(share);
            }
        } else {
            // 自定义分摊
            Map<Long, BigDecimal> customAmounts = new HashMap<>();
            if (request.getCustomShares() != null) {
                for (ExpenseRequest.CustomShareRequest customShare : request.getCustomShares()) {
                    customAmounts.put(customShare.getUserId(), customShare.getAmount());
                }
            }
            
            for (User user : shareUsers) {
                ExpenseShare share = new ExpenseShare();
                share.setExpense(expense);
                share.setUser(user);
                
                BigDecimal customAmount = customAmounts.get(user.getId());
                if (customAmount != null) {
                    share.setAmount(customAmount);
                    share.setCustomAmount(customAmount);
                } else {
                    // 如果没有自定义金额，使用平均分摊
                    BigDecimal shareAmount = request.getAmount().divide(
                            BigDecimal.valueOf(shareUsers.size()), 2, RoundingMode.HALF_UP);
                    share.setAmount(shareAmount);
                }
                share.setIsPaid(false);
                shares.add(share);
            }
        }
        
        expenseShareRepository.saveAll(shares);
        
        return convertToResponse(expense);
    }
    
    /**
     * 获取所有费用记录
     */
    public List<ExpenseResponse> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAllOrderByExpenseDateDesc();
        return expenses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据ID获取费用记录
     */
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("费用记录不存在: " + id));
        return convertToResponse(expense);
    }
    
    /**
     * 更新费用记录
     */
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        // 查找现有费用记录
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("费用记录不存在: " + id));
        
        // 验证付款人
        List<ExpenseRequest.PaymentRequest> paymentRequests = request.getPayments();
        if (paymentRequests == null || paymentRequests.isEmpty()) {
            throw new RuntimeException("付款人列表不能为空");
        }
        
        // 验证所有付款人存在
        List<Long> payerIds = paymentRequests.stream()
                .map(ExpenseRequest.PaymentRequest::getPayerId)
                .collect(Collectors.toList());
        List<User> payers = userRepository.findAllById(payerIds);
        if (payers.size() != payerIds.size()) {
            throw new RuntimeException("部分付款人不存在");
        }
        
        // 验证付款总金额
        BigDecimal totalPaymentAmount = paymentRequests.stream()
                .map(ExpenseRequest.PaymentRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalPaymentAmount.compareTo(request.getAmount()) != 0) {
            throw new RuntimeException("付款总金额与费用金额不匹配");
        }
        
        // 验证分摊用户
        List<User> shareUsers = userRepository.findAllById(request.getShareUserIds());
        if (shareUsers.size() != request.getShareUserIds().size()) {
            throw new RuntimeException("部分分摊用户不存在");
        }
        
        // 验证费用分类
        ExpenseCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("费用分类不存在: " + request.getCategoryId()));
        }
        
        // 更新费用记录基本信息
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(category);
        expense.setSplitType(SplitType.valueOf(request.getSplitType()));
        expense.setExpenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : expense.getExpenseDate());
        
        // 删除现有的付款记录和分摊记录
        expensePaymentRepository.deleteByExpenseId(id);
        expenseShareRepository.deleteByExpenseId(id);
        
        // 创建新的付款记录
        List<ExpensePayment> payments = new ArrayList<>();
        for (ExpenseRequest.PaymentRequest paymentRequest : paymentRequests) {
            User payer = payers.stream()
                    .filter(p -> p.getId().equals(paymentRequest.getPayerId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("付款人不存在: " + paymentRequest.getPayerId()));
            
            ExpensePayment payment = new ExpensePayment();
            payment.setExpense(expense);
            payment.setPayer(payer);
            payment.setAmount(paymentRequest.getAmount());
            payments.add(payment);
        }
        expensePaymentRepository.saveAll(payments);
        
        // 创建新的分摊记录
        List<ExpenseShare> shares = new ArrayList<>();
        
        if (SplitType.EQUAL.name().equals(request.getSplitType())) {
            // 平均分摊
            BigDecimal shareAmount = request.getAmount().divide(
                    BigDecimal.valueOf(shareUsers.size()), 2, RoundingMode.HALF_UP);
            
            for (User user : shareUsers) {
                ExpenseShare share = new ExpenseShare();
                share.setExpense(expense);
                share.setUser(user);
                share.setAmount(shareAmount);
                share.setIsPaid(false);
                shares.add(share);
            }
        } else {
            // 自定义分摊
            Map<Long, BigDecimal> customAmounts = new HashMap<>();
            if (request.getCustomShares() != null) {
                for (ExpenseRequest.CustomShareRequest customShare : request.getCustomShares()) {
                    customAmounts.put(customShare.getUserId(), customShare.getAmount());
                }
            }
            
            for (User user : shareUsers) {
                ExpenseShare share = new ExpenseShare();
                share.setExpense(expense);
                share.setUser(user);
                
                BigDecimal customAmount = customAmounts.get(user.getId());
                if (customAmount != null) {
                    share.setAmount(customAmount);
                    share.setCustomAmount(customAmount);
                } else {
                    // 如果没有自定义金额，使用平均分摊
                    BigDecimal shareAmount = request.getAmount().divide(
                            BigDecimal.valueOf(shareUsers.size()), 2, RoundingMode.HALF_UP);
                    share.setAmount(shareAmount);
                }
                share.setIsPaid(false);
                shares.add(share);
            }
        }
        
        expense = expenseRepository.save(expense);
        expenseShareRepository.saveAll(shares);
        
        return convertToResponse(expense);
    }
    
    /**
     * 标记分摊为已支付
     */
    @Transactional
    public void markShareAsPaid(Long shareId) {
        ExpenseShare share = expenseShareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("分摊记录不存在: " + shareId));
        
        share.setIsPaid(true);
        expenseShareRepository.save(share);
    }
    
    /**
     * 获取用户余额统计
     */
    public List<UserBalanceResponse> getUserBalances() {
        List<User> users = userRepository.findAll();
        List<UserBalanceResponse> balances = new ArrayList<>();
        
        for (User user : users) {
            UserBalanceResponse balance = new UserBalanceResponse();
            balance.setUserId(user.getId());
            balance.setUserName(user.getName());
            
            // 计算总支付金额
            BigDecimal totalPaid = expensePaymentRepository.findByPayerId(user.getId())
                    .stream()
                    .map(ExpensePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            balance.setTotalPaid(totalPaid);
            
            // 计算总欠款金额
            BigDecimal totalOwed = expenseShareRepository.findUnpaidByUserId(user.getId())
                    .stream()
                    .map(ExpenseShare::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            balance.setTotalOwed(totalOwed);
            
            // 计算余额
            balance.setBalance(totalPaid.subtract(totalOwed));
            
            // 计算详细信息
            balance.setDetails(calculateBalanceDetails(user));
            
            balances.add(balance);
        }
        
        return balances;
    }
    
    /**
     * 计算余额详细信息
     */
    private List<UserBalanceResponse.BalanceDetail> calculateBalanceDetails(User user) {
        List<UserBalanceResponse.BalanceDetail> details = new ArrayList<>();
        
        // 别人欠我的钱 - 需要重新计算，因为现在没有直接的payer关系
        // 这里需要根据费用记录和分摊记录来计算
        List<Expense> allExpenses = expenseRepository.findAll();
        for (Expense expense : allExpenses) {
            // 检查这个用户是否为此费用付款
            boolean isPayer = expense.getPayments() != null && 
                    expense.getPayments().stream()
                            .anyMatch(payment -> payment.getPayer().getId().equals(user.getId()));
            
            if (isPayer) {
                // 检查是否有未支付的分摊
                List<ExpenseShare> unpaidShares = expense.getShares().stream()
                        .filter(share -> !share.getIsPaid() && !share.getUser().getId().equals(user.getId()))
                        .collect(Collectors.toList());
                
                for (ExpenseShare share : unpaidShares) {
                    UserBalanceResponse.BalanceDetail detail = new UserBalanceResponse.BalanceDetail();
                    detail.setType("should_receive");
                    detail.setDescription("应收款项");
                    detail.setAmount(share.getAmount());
                    detail.setRelatedUserName(share.getUser().getName());
                    details.add(detail);
                }
            }
        }
        
        // 我欠别人的钱
        List<ExpenseShare> myUnpaidShares = expenseShareRepository.findUnpaidByUserId(user.getId());
        for (ExpenseShare share : myUnpaidShares) {
            UserBalanceResponse.BalanceDetail detail = new UserBalanceResponse.BalanceDetail();
            detail.setType("owed");
            detail.setDescription("应付款项");
            detail.setAmount(share.getAmount());
            
            // 找到这个费用的付款人
            String payerNames = share.getExpense().getPayments() != null ? 
                    share.getExpense().getPayments().stream()
                            .map(payment -> payment.getPayer().getName())
                            .collect(Collectors.joining(", ")) : "未知";
            detail.setRelatedUserName(payerNames);
            details.add(detail);
        }
        
        return details;
    }
    
    /**
     * 转换为响应DTO
     */
    private ExpenseResponse convertToResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setDescription(expense.getDescription());
        response.setAmount(expense.getAmount());
        response.setCategoryName(expense.getCategory() != null ? expense.getCategory().getName() : null);
        response.setSplitType(expense.getSplitType().getDescription());
        response.setExpenseDate(expense.getExpenseDate());
        response.setCreatedAt(expense.getCreatedAt());
        
        // 转换付款信息
        List<ExpenseResponse.PaymentResponse> paymentResponses = new ArrayList<>();
        if (expense.getPayments() != null && !expense.getPayments().isEmpty()) {
            paymentResponses = expense.getPayments()
                    .stream()
                    .filter(payment -> payment != null && payment.getPayer() != null)
                    .map(payment -> {
                        ExpenseResponse.PaymentResponse paymentResponse = new ExpenseResponse.PaymentResponse();
                        paymentResponse.setId(payment.getId());
                        paymentResponse.setPayerName(payment.getPayer().getName());
                        paymentResponse.setAmount(payment.getAmount());
                        return paymentResponse;
                    })
                    .collect(Collectors.toList());
        }
        response.setPayments(paymentResponses);
        
        // 转换分摊信息
        List<ExpenseResponse.ExpenseShareResponse> shareResponses = new ArrayList<>();
        if (expense.getShares() != null && !expense.getShares().isEmpty()) {
            shareResponses = expense.getShares()
                    .stream()
                    .filter(share -> share != null && share.getUser() != null)
                    .map(share -> {
                        ExpenseResponse.ExpenseShareResponse shareResponse = new ExpenseResponse.ExpenseShareResponse();
                        shareResponse.setId(share.getId());
                        shareResponse.setUserName(share.getUser().getName());
                        shareResponse.setAmount(share.getAmount());
                        shareResponse.setCustomAmount(share.getCustomAmount());
                        shareResponse.setIsPaid(share.getIsPaid());
                        return shareResponse;
                    })
                    .collect(Collectors.toList());
        }
        
        response.setShares(shareResponses);
        return response;
    }
}
