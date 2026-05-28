package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        LocalDate[] range = normalizeRange(begin, end);
        begin = range[0];
        end = range[1];
        List<LocalDate> dateList = getDateList(begin, end);
        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate date : dateList) {
            Map<String, Object> map = buildTimeRange(date, date);
            map.put("status", Orders.COMPLETED);
            turnoverList.add(orderMapper.sumByMap(map));
        }

        return TurnoverReportVO.builder()
                .dateList(join(dateList))
                .turnoverList(join(turnoverList))
                .build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        LocalDate[] range = normalizeRange(begin, end);
        begin = range[0];
        end = range[1];
        List<LocalDate> dateList = getDateList(begin, end);
        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            Map<String, Object> totalMap = new HashMap<>();
            totalMap.put("end", endOfDay(date));
            totalUserList.add(userMapper.countByMap(totalMap));

            newUserList.add(userMapper.countByMap(buildTimeRange(date, date)));
        }

        return UserReportVO.builder()
                .dateList(join(dateList))
                .totalUserList(join(totalUserList))
                .newUserList(join(newUserList))
                .build();
    }

    /**
     * 订单统计
     * */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        LocalDate[] range = normalizeRange(begin, end);
        begin = range[0];
        end = range[1];
        List<LocalDate> dateList = getDateList(begin, end);
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            Map<String, Object> totalMap = buildTimeRange(date, date);
            orderCountList.add(orderMapper.countByMap(totalMap));

            Map<String, Object> validMap = buildTimeRange(date, date);
            validMap.put("status", Orders.COMPLETED);
            validOrderCountList.add(orderMapper.countByMap(validMap));
        }

        Map<String, Object> totalMap = buildTimeRange(begin, end);
        Integer totalOrderCount = orderMapper.countByMap(totalMap);

        Map<String, Object> validMap = buildTimeRange(begin, end);
        validMap.put("status", Orders.COMPLETED);
        Integer validOrderCount = orderMapper.countByMap(validMap);

        return OrderReportVO.builder()
                .dateList(join(dateList))
                .orderCountList(join(orderCountList))
                .validOrderCountList(join(validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(calculateRate(validOrderCount, totalOrderCount))
                .build();
    }

    /**
     * 导出营业数据
     * @param begin
     * @param end
     * @param
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDate[] range = normalizeRange(begin, end);
        begin = range[0];
        end = range[1];
        List<GoodsSalesDTO> goodsSalesList = orderMapper.getSalesTop10(startOfDay(begin), endOfDay(end));

        return SalesTop10ReportVO.builder()
                .nameList(goodsSalesList.stream().map(GoodsSalesDTO::getName).collect(Collectors.joining(",")))
                .numberList(goodsSalesList.stream().map(dto -> String.valueOf(dto.getNumber())).collect(Collectors.joining(",")))
                .build();
    }

    /**
     * 导出营业数据
     * @param begin
     * @param end
     * @param
     */
    @Override
    public BusinessDataVO getBusinessData(LocalDate begin, LocalDate end) {
        LocalDate[] range = normalizeRange(begin, end);
        begin = range[0];
        end = range[1];
        Map<String, Object> totalMap = buildTimeRange(begin, end);
        Integer totalOrderCount = orderMapper.countByMap(totalMap);

        Map<String, Object> validMap = buildTimeRange(begin, end);
        validMap.put("status", Orders.COMPLETED);
        Integer validOrderCount = orderMapper.countByMap(validMap);
        Double turnover = orderMapper.sumByMap(validMap);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(calculateRate(validOrderCount, totalOrderCount))
                .unitPrice(validOrderCount == 0 ? 0.0 : turnover / validOrderCount)
                .newUsers(userMapper.countByMap(buildTimeRange(begin, end)))
                .build();
    }

    // 导出营业数据
    @Override
    public void exportBusinessData(LocalDate begin, LocalDate end, HttpServletResponse response) {
        LocalDate[] range = normalizeRange(begin, end);
        LocalDate actualBegin = range[0];
        LocalDate actualEnd = range[1];

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("运营数据");
            Row title = sheet.createRow(0);
            title.createCell(0).setCellValue("日期");
            title.createCell(1).setCellValue("营业额");
            title.createCell(2).setCellValue("有效订单数");
            title.createCell(3).setCellValue("订单完成率");
            title.createCell(4).setCellValue("平均客单价");
            title.createCell(5).setCellValue("新增用户数");

            List<LocalDate> dateList = getDateList(actualBegin, actualEnd);
            for (int i = 0; i < dateList.size(); i++) {
                LocalDate date = dateList.get(i);
                BusinessDataVO data = getBusinessData(date, date);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(date.toString());
                row.createCell(1).setCellValue(data.getTurnover());
                row.createCell(2).setCellValue(data.getValidOrderCount());
                row.createCell(3).setCellValue(data.getOrderCompletionRate());
                row.createCell(4).setCellValue(data.getUnitPrice());
                row.createCell(5).setCellValue(data.getNewUsers());
            }

            String fileName = URLEncoder.encode("运营数据报表.xlsx", StandardCharsets.UTF_8.name());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            log.error("导出运营数据失败", e);
            throw new RuntimeException("导出运营数据失败", e);
        }
    }

    /**
     * 获取指定日期范围内的日期列表
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate date = begin;
        while (!date.isAfter(end)) {
            dateList.add(date);
            date = date.plusDays(1);
        }
        return dateList;
    }

    /**
     * 获取指定日期范围内的日期列表
     * @param begin
     * @param end
     * @return
     */
    private LocalDate[] normalizeRange(LocalDate begin, LocalDate end) {
        LocalDate actualEnd = end == null ? LocalDate.now() : end;
        LocalDate actualBegin = begin == null ? actualEnd.plusDays(-6) : begin;
        if (actualBegin.isAfter(actualEnd)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        return new LocalDate[]{actualBegin, actualEnd};
    }

    /**
     * 构建时间范围查询条件
     * */
    private Map<String, Object> buildTimeRange(LocalDate begin, LocalDate end) {
        Map<String, Object> map = new HashMap<>();
        map.put("begin", startOfDay(begin));
        map.put("end", endOfDay(end));
        return map;
    }

    /**
     * 获取指定日期的开始时间
     * @param date
     * @return
     *  */
    private LocalDateTime startOfDay(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MIN);
    }

    /**
     * 获取指定日期的结束时间
     * @param date
     * @return
     *  */
    private LocalDateTime endOfDay(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MAX);
    }

    /**
     * 计算订单完成率
     * @param validCount
     * @param totalCount
     * @return
     *  */
    private Double calculateRate(Integer validCount, Integer totalCount) {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return validCount.doubleValue() / totalCount;
    }

    /**
     * 拼接字符串
     * @param list
     * @return
     *  */
    private String join(List<?> list) {
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
