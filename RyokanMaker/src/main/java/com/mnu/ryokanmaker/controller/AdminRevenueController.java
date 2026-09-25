package com.mnu.ryokanmaker.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mnu.ryokanmaker.domain.AdminDto;
import com.mnu.ryokanmaker.domain.PaymentMethodStatDto;
import com.mnu.ryokanmaker.domain.RevenueTxnDto;
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

		return "admin/revenue_monthly";
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

		DateRange range = resolveRange(period, customStart, customEnd);

		List<RevenueTxnDto> all = revenueService.getTransactions(adminIdx, range.start(), range.endExclusive());
		List<RevenueTxnDto> paid = all.stream().filter(t -> RevenueService.PAID.equals(t.getResvPayStatus())).toList();
		List<RevenueTxnDto> pending = all.stream().filter(t -> !RevenueService.PAID.equals(t.getResvPayStatus())).toList();

		int paidTotal = paid.stream().mapToInt(RevenueTxnDto::getResvPrice).sum();

		model.addAttribute("period", range.period());
		model.addAttribute("rangeStart", range.start());
		model.addAttribute("rangeEnd", range.endExclusive().minusDays(1));
		model.addAttribute("paidTxns", paid);
		model.addAttribute("pendingTxns", pending);
		model.addAttribute("paidTotal", paidTotal);
		model.addAttribute("paidCount", paid.size());
		model.addAttribute("methodStats", methodBreakdown(paid));
		model.addAttribute("pendingCount", pending.size());

		return "admin/revenue_daily";
	}

	/** 일자별 매출조회 화면과 동일한 조회 조건으로 엑셀(.xlsx) 다운로드. */
	@GetMapping("revenue_daily/export")
	public ResponseEntity<byte[]> exportRevenueDaily(
			@RequestParam(value = "period", required = false, defaultValue = "month") String period,
			@RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate customStart,
			@RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate customEnd,
			HttpSession session) throws IOException {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		Integer adminIdx = loginAdmin.getAdminIdx();

		DateRange range = resolveRange(period, customStart, customEnd);
		List<RevenueTxnDto> all = revenueService.getTransactions(adminIdx, range.start(), range.endExclusive());
		List<RevenueTxnDto> paid = all.stream().filter(t -> RevenueService.PAID.equals(t.getResvPayStatus())).toList();
		List<RevenueTxnDto> pending = all.stream().filter(t -> !RevenueService.PAID.equals(t.getResvPayStatus())).toList();

		byte[] excel = buildRevenueExcel(paid, pending);

		LocalDate rangeEnd = range.endExclusive().minusDays(1);
		String filename = "매출내역_" + range.start() + "_" + rangeEnd + ".xlsx";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
		headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

		return new ResponseEntity<>(excel, headers, HttpStatus.OK);
	}

	/** period(today/week/month/lastmonth/custom) -> 실제 조회 시작일/종료일(배타적)로 변환. revenueDaily 화면·엑셀 다운로드가 공유. */
	private DateRange resolveRange(String period, LocalDate customStart, LocalDate customEnd) {
		LocalDate today = LocalDate.now();

		return switch (period) {
			case "today" -> new DateRange(today, today.plusDays(1), "today");
			case "week" -> new DateRange(today.minusDays(today.getDayOfWeek().getValue() % 7), today.plusDays(1), "week");
			case "lastmonth" -> {
				YearMonth lastMonth = YearMonth.from(today).minusMonths(1);
				yield new DateRange(lastMonth.atDay(1), lastMonth.plusMonths(1).atDay(1), "lastmonth");
			}
			case "custom" -> new DateRange(
					customStart != null ? customStart : today.withDayOfMonth(1),
					(customEnd != null ? customEnd : today).plusDays(1),
					"custom");
			default -> new DateRange(today.withDayOfMonth(1), today.plusDays(1), "month");
		};
	}

	private record DateRange(LocalDate start, LocalDate endExclusive, String period) {
	}

	private static final String[] TXN_HEADERS = {"체크인일", "예약번호", "고객명", "객실", "플랜", "결제수단", "금액", "상태"};

	/** 결제완료/결제대기 거래 목록을 시트 2개(결제완료/결제대기)로 담은 .xlsx 바이트를 만든다. */
	private byte[] buildRevenueExcel(List<RevenueTxnDto> paid, List<RevenueTxnDto> pending) throws IOException {
		try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			CellStyle headerStyle = headerStyle(wb);
			CellStyle moneyStyle = moneyStyle(wb);

			writeTxnSheet(wb, "결제완료", paid, headerStyle, moneyStyle, true);
			writeTxnSheet(wb, "결제대기", pending, headerStyle, moneyStyle, false);

			wb.write(out);
			return out.toByteArray();
		}
	}

	private void writeTxnSheet(Workbook wb, String sheetName, List<RevenueTxnDto> rows,
			CellStyle headerStyle, CellStyle moneyStyle, boolean withTotal) {
		Sheet sheet = wb.createSheet(sheetName);

		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < TXN_HEADERS.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(TXN_HEADERS[i]);
			cell.setCellStyle(headerStyle);
		}

		DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy.MM.dd");
		int r = 1;
		long total = 0;
		for (RevenueTxnDto t : rows) {
			Row row = sheet.createRow(r++);
			row.createCell(0).setCellValue(t.getResvCheckIn() != null ? t.getResvCheckIn().format(dateFmt) : "");
			row.createCell(1).setCellValue(t.getResvNum() != null ? "#" + t.getResvNum() : "");
			row.createCell(2).setCellValue(t.getUserNickname() != null ? t.getUserNickname() : "");
			row.createCell(3).setCellValue(t.getRoomName() != null ? t.getRoomName() : "");
			row.createCell(4).setCellValue(t.getPlanName() != null ? t.getPlanName() : "-");
			row.createCell(5).setCellValue(t.getResvPayMethod() != null ? t.getResvPayMethod() : "-");

			int price = t.getResvPrice() != null ? t.getResvPrice() : 0;
			Cell amountCell = row.createCell(6);
			amountCell.setCellValue(price);
			amountCell.setCellStyle(moneyStyle);
			total += price;

			row.createCell(7).setCellValue(t.getResvPayStatus() != null ? t.getResvPayStatus() : "");
		}

		if (withTotal && !rows.isEmpty()) {
			Row totalRow = sheet.createRow(r);
			totalRow.createCell(5).setCellValue("합계");
			Cell totalCell = totalRow.createCell(6);
			totalCell.setCellValue(total);
			totalCell.setCellStyle(moneyStyle);
		}

		for (int i = 0; i < TXN_HEADERS.length; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private CellStyle headerStyle(Workbook wb) {
		Font font = wb.createFont();
		font.setBold(true);
		CellStyle style = wb.createCellStyle();
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return style;
	}

	private CellStyle moneyStyle(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
		return style;
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
