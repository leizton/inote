逻辑分页
	先查询出ResultSet, 由于ResultSet带有游标, 所以可以用ResultSet的next()方法进行分页.
	实现一, 循环调用next()并计数;
	实现二, 调用可滚动结果集的absolute(int row)跳至分页起始行,
		/* ResultSet.TYPE_FORWARD_ONLY        结果集不能滚动
		 * ResultSet.TYPE_SCROLL_INSENSITIVE  可滚动, 对数据的改变不敏感
		 * ResultSet.TYPE_SCROLL_SENSITIVE    可滚动, 对数据的改变敏感
		 * ResultSet.CONCUR_READ_ONLY         结果集只读
		 * ResultSet.CONCUR_UPDATABLE         可更新数据库
		 */
		ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		rs = ps.executeQuery();
		rs.absolute(startRow);
		for (int cnt = 0; cnt < pageSize && rs.next(); cnt++) {
			// rs.get
		}
物理分页
	利用数据库的特性实现, 如 Mysql的limit, Oracle的rownum, SqlServer的top.


Mybatis的RowBounds是逻辑分页,
实现物理分页可以编写plugin拦截Executor的StatementHandler来重写sql语句.
另外, 还要用插件ResultSetHandlerInterceptor清除RowBounds, 防止跳过结果集.


在"mybatis-config.xml"里配置
	<plugins>
		<plugin interceptor="com.xxx.StatementHandlerInterceptor"/>
		<plugin interceptor="com.xxx.ResultSetHandlerInterceptor"/>
	</plugins>
dao接口:
	List<Department> selectList(RowBounds rowBounds); // 直接用RowBounds, import org.apache.ibatis.session.RowBounds
*-mapper.xml:
	<select id="selectList" resultMap="">
		SELECT <include refid="Base_Column_List"/> FROM table  // 不用出现RowBounds
	</select>


使用RowBounds可以适配MySql和Oracle等, 不限与limit
这两个插件的代码如下:

@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }) })
public class StatementHandlerInterceptor implements Interceptor {

    private Dialect dialect;  // 默认用Mysql的方言, limit和offset

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取处理目标
        StatementHandler target = (StatementHandler) invocation.getTarget();
        if (target instanceof RoutingStatementHandler) {
            target = (BaseStatementHandler) ReflectUtil.getFieldValue(target, "delegate");
        }
        RowBounds rowBounds = (RowBounds) ReflectUtil.getFieldValue(target, "rowBounds");
        // 调整查询字符串
        if (rowBounds.getLimit() > 0 && rowBounds.getLimit() < RowBounds.NO_ROW_LIMIT) {
            BoundSql boundSql = target.getBoundSql();
            String sql = boundSql.getSql();

            sql = dialect.getLimitString(sql, rowBounds.getOffset(), rowBounds.getLimit());
            ReflectUtil.setFieldValue(boundSql, "sql", sql);
        }
        // 执行查询处理
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        String dialectClass = properties.getProperty("dialectClass");

        // 初始化物理查询处理程序
        if (dialectClass == null || dialectClass.isEmpty()) {
            dialect = new DefaultDialect();
        } else {
            try {
                dialect = (Dialect) Class.forName(dialectClass).newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid dialect class " + dialectClass, e);
            }
        }
    }
}

@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = { Statement.class }) })
public class ResultSetHandlerInterceptor implements Interceptor {

    private static final RowBounds ROW_BOUNDS = new RowBounds();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        ResultSetHandler target = (ResultSetHandler) invocation.getTarget();
        RowBounds rowBounds = (RowBounds) ReflectUtil.getFieldValue(target, "rowBounds");

        if (rowBounds.getLimit() > 0 && rowBounds.getLimit() < RowBounds.NO_ROW_LIMIT) {
            // 清除分页参数，禁止FastResultSetHandler#skipRows跳过结果集
            ReflectUtil.setFieldValue(target, "rowBounds", ROW_BOUNDS);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}