package com.example.canteen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.canteen.dto.OrderAdminView;
import com.example.canteen.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 一次性查出：总营收、总成本、订单数（SQL 中计算利润）
     */
    Map<String, Object> getOrderStats(@Param("startTime") LocalDateTime startTime);

    /**
     * 统计趋势图：按日期分组
     */
    List<Map<String, Object>> getSalesTrend(@Param("startTime") LocalDateTime startTime);

    /**
     * 管理端订单列表：带用户信息（用户名、下单地址）
     */
    List<OrderAdminView> selectAdminOrders();

    /**
     * 经营统计概览：营收、订单数、利润（按时间范围）
     */
    Map<String, Object> getStatisticsOverview(@Param("startTime") Object startTime);

    /**
     * 经营统计图表：按日期/时间分组
     */
    List<Map<String, Object>> getStatisticsChart(@Param("startTime") Object startTime,
                                                  @Param("dateFormat") String dateFormat);

    /**
     * 今日概览统计
     */
    Map<String, Object> getTodayStats(@Param("today") String today);

    /**
     * 近7天趋势
     */
    List<Map<String, Object>> getSevenDaysTrend(@Param("sevenDaysAgo") String sevenDaysAgo);

    /**
     * 最新 N 条订单（管理端仪表盘）
     */
    List<Order> selectLatestOrders(@Param("limit") int limit);
}
