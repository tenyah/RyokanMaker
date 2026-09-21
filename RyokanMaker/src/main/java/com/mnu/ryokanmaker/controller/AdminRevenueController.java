package com.mnu.ryokanmaker.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mnu.ryokanmaker.dto.AdminDto;
import com.mnu.ryokanmaker.dto.PaymentMethodStatDto;
import com.mnu.ryokanmaker.dto.RevenueTxnDto;
import com.mnu.ryokanmaker.service.RevenueService;

import jakarta.servlet.http.HttpSession;

/** 관리자 - 매출현황(월매출조회 / 일자별 매출조회) 화면. */
@Controller
@RequestMapping("Admin")
public class AdminRevenueController {

	@Autowired
	private RevenueService revenueService;

	private AdminDto currentAdmin(HttpSession session) {
		return (AdminDto) session.getAttribute("admin");
	}

	/** 월매출조회 : ym(yyyy-MM) 파라미터로 조회할 달을 바꾼다. 값이 없으면 이번 달. */
	@GetMapping("revenue_monthly")
	public String revenueMonthly(@RequestParam(value = "ym", required = false) String ym,
			HttpSession session, Model model) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		Integer adminIdx = loginAdmin.getAdminIdx();

		YearMonth target = parseYearMonth(ym);
		YearMonth prev = target.minusMonths(1);

		var summary = revenueService.getMonthSummary(adminIdx, target);
		var prevSummary = revenueService.getMonthSummary(adminIdx, prev);

		model.addAttribute("summary", summary);
		model.addAttribute("prevSummary", prevSummary);
		model.addAttribute("monthlyTrend", revenueService.getMonthlyTrend(adminIdx, target.getYear()));
		model.addAttribute("ymValue", target.toString());
		model.addAttribute("prevYm", prev.toString());
		model.addAttribute("nextYm", target.plusMonths(1).toString());
		// 미래 달로는 이동할 필요가 없으므로(데이터가 없음) 이번 달 이후는 다음 달 이동을 막는다.
		model.addAttribute("hasNext", target.isBefore(YearMonth.now()));
		model.addAttribute("momChangePercent", prevSummary.getTotalRevenue() == 0 ? null
				: Math.round((summary.getTotalRevenue() - prevSummary.getTotalRevenue()) * 100.0 / prevSummary.getTotalRevenue()));
		model.addAttribute("dowLabels", dowLabels());

		return "Admin/revenue_monthly";
	}

	/** 일자별 매출조회 : period(today/week/month/lastmonth/custom)로 조회 기간을 정한다. */
	@GetMapping("revenue_daily")
	public String revenueDaily(@RequestParam(value = "period", required = false, defaultValue = "month") String period,
			@RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate customStart,
			@RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate customEnd,
			HttpSession session, Model model) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		Integer adminIdx = loginAdmin.getAdminIdx();

		LocalDate today = LocalDate.now();
		LocalDate rangeStart;
		LocalDate rangeEndExclusive;

		switch (period) {
			case "today" -> {
				rangeStart = today;
				rangeEndExclusive = today.plusDays(1);
			}
			case "week" -> {
				rangeStart = today.minusDays(today.getDayOfWeek().getValue() % 7);
				rangeEndExclusive = today.plusDays(1);
			}
			case "lastmonth" -> {
				YearMonth lastMonth = YearMonth.from(today).minusMonths(1);
				rangeStart = lastMonth.atDay(1);
				rangeEndExclusive = lastMonth.plusMonths(1).atDay(1);
			}
			case "custom" -> {
				rangeStart = customStart != null ? customStart : today.withDayOfMonth(1);
				rangeEndExclusive = (customEnd != null ? customEnd : today).plusDays(1);
			}
			default -> {
				period = "month";
				rangeStart = today.withDayOfMonth(1);
				rangeEndExclusive = today.plusDays(1);
			}
		}

		List<RevenueTxnDto> all = revenueService.getTransactions(adminIdx, rangeStart, rangeEndExclusive);
		List<RevenueTxnDto> paid = all.stream().filter(t -> RevenueService.PAID.equals(t.getResvPayStatus())).toList();
		List<RevenueTxnDto> pending = all.stream().filter(t -> !RevenueService.PAID.equals(t.getResvPayStatus())).toList();

		int paidTotal = paid.stream().mapToInt(RevenueTxnDto::getResvPrice).sum();

		model.addAttribute("period", period);
		model.addAttribute("rangeStart", rangeStart);
		model.addAttribute("rangeEnd", rangeEndExclusive.minusDays(1));
		model.addAttribute("paidTxns", paid);
		model.addAttribute("pendingTxns", pending);
		model.addAttribute("paidTotal", paidTotal);
		model.addAttribute("paidCount", paid.size());
		model.addAttribute("methodStats", methodBreakdown(paid));
		model.addAttribute("pendingCount", pending.size());

		return "Admin/revenue_daily";
	}

	/** 실제로 쓰인 결제수단만, 금액 내림차순으로 집계한다 (카드/계좌이체로 미리 못박지 않음). */
	private List<PaymentMethodStatDto> methodBreakdown(List<RevenueTxnDto> paid) {
		Map<String, int[]> byMethod = new LinkedHashMap<>();
		int paidTotal = 0;
		for (RevenueTxnDto t : paid) {
			String method = t.getResvPayMethod() != null ? t.getResvPayMethod() : "-";
			int[] agg = byMethod.computeIfAbsent(method, k -> new int[2]);
			agg[0] += 1;
			agg[1] += t.getResvPrice();
			paidTotal += t.getResvPrice();
		}
		List<PaymentMethodStatDto> result = new ArrayList<>();
		for (Map.Entry<String, int[]> e : byMethod.entrySet()) {
			int percent = paidTotal == 0 ? 0 : (int) Math.round(e.getValue()[1] * 100.0 / paidTotal);
			result.add(new PaymentMethodStatDto(e.getKey(), e.getValue()[0], e.getValue()[1], percent));
		}
		result.sort((a, b) -> b.getTotal() - a.getTotal());
		return result;
	}

	/** 일요일부터 시작하는 요일 짧은 이름 (현재 화면 언어 기준). */
	private List<String> dowLabels() {
		List<String> labels = new ArrayList<>();
		DayOfWeek day = DayOfWeek.SUNDAY;
		for (int i = 0; i < 7; i++) {
			labels.add(day.getDisplayName(TextStyle.SHORT, LocaleContextHolder.getLocale()));
			day = day.plus(1);
		}
		return labels;
	}

	private YearMonth parseYearMonth(String ym) {
		if (ym == null || ym.isBlank()) {
			return YearMonth.now();
		}
		try {
			return YearMonth.parse(ym, DateTimeFormatter.ofPattern("yyyy-MM"));
		} catch (Exception e) {
			return YearMonth.now();
		}
	}
}
