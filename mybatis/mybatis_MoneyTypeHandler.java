import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import qunar.api.pojo.Money;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author yiqun.fan@qunar.com create on 16-4-28. Money的typeHandler
 */
public class MoneyTypeHandler extends BaseTypeHandler<Money> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Money parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setBigDecimal(i, parameter.getAmount());
    }

    @Override
    public Money getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Money.of(rs.getBigDecimal(columnName));
    }

    @Override
    public Money getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Money.of(rs.getBigDecimal(columnIndex));
    }

    @Override
    public Money getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Money.of(cs.getBigDecimal(columnIndex));
    }
}