package com.iana.api.dao;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public abstract class GenericDAO implements IGenericDAO {

	/*
	 * Methods :: SpringJdbcTemplate Start
	 * ............................................................
	 * ...............................................
	 */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> findAll(DataSource dataSource, String query, Class<T> requiredType, Object... params) throws Exception {

		return getSpringJdbcTemplate(dataSource).query(query, new BeanPropertyRowMapper(requiredType), params);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> findAll(DataSource dataSource, String query, Class<T> requiredType) throws Exception {

		return getSpringJdbcTemplate(dataSource).query(query, new BeanPropertyRowMapper(requiredType));
	}

	public <T> List<T> findAll(DataSource dataSource, String query, Object[] params, Class<T> requiredType) throws Exception {

		return findAll(dataSource, query, requiredType, params);
	}

	public List<Map<String, Object>> findList(DataSource dataSource, String query, Object... params) throws Exception {

		return getSpringJdbcTemplate(dataSource).queryForList(query, params);
	}

	public List<Map<String, Object>> findList(DataSource dataSource, Object[] params, String query) throws Exception {

		return findList(dataSource, query, params);
	}

	public <T> List<T> findList(DataSource dataSource, Object[] params, String query, Class<T> requiredType) throws Exception {

		return getSpringJdbcTemplate(dataSource).queryForList(query, requiredType, params);
	}

	public List<Map<String, Object>> findList(DataSource dataSource, String query) throws Exception {

		return getSpringJdbcTemplate(dataSource).queryForList(query);
	}

	public Map<String, Object> findMap(DataSource dataSource, String query, Object... params) throws Exception {

		try {
			return getSpringJdbcTemplate(dataSource).queryForMap(query, params);
		} catch (EmptyResultDataAccessException e) {
			return new HashMap<String, Object>();
		}
	}

	public Map<String, Object> findMap(DataSource dataSource, Object[] params, String query) throws Exception {

		return findMap(dataSource, query, params);
	}

	public Map<String, Object> findMap(DataSource dataSource, String query) throws Exception {

		return getSpringJdbcTemplate(dataSource).queryForMap(query);
	}

	public <T> T findObject(DataSource dataSource, String query, Class<T> requiredType) throws Exception {

		return getSpringJdbcTemplate(dataSource).queryForObject(query, requiredType);
	}

	public <T> T findObject(DataSource dataSource, String query, Class<T> requiredType, Object... params) throws Exception {

		return getSpringJdbcTemplate(dataSource).queryForObject(query, requiredType, params);
	}

	public <T> T findObject(DataSource dataSource, String query, Object[] params, Class<T> requiredType) throws Exception {

		return findObject(dataSource, query, requiredType, params);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T findBean(DataSource dataSource, String query, Class<T> requiredType, Object... params) throws Exception {

		T object = null;
		if (params == null || params.length == 0) {
			throw new Exception("Please send valid params values, it should not be empty or null.");
		} else {
			List<T> objectList = (List<T>) getSpringJdbcTemplate(dataSource).query(query, new BeanPropertyRowMapper(requiredType), params);

			if (objectList.isEmpty()) {
				object = (T) requiredType.newInstance();

			} else {
				object = objectList.get(0);
			}
		}
		return object;
	}

	public <T> T findBean(DataSource dataSource, String query, Object[] params, Class<T> requiredType) throws Exception {

		return findBean(dataSource, query, requiredType, params);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T findBean(DataSource dataSource, String query, Class<T> requiredType) throws Exception {

		T object = null;
		List<T> objectList = (List<T>) getSpringJdbcTemplate(dataSource).query(query, new BeanPropertyRowMapper(requiredType));
		if (objectList.isEmpty()) {
			object = (T) requiredType.newInstance();
		} else {
			object = objectList.get(0);
		}
		return object;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> findBeanList(DataSource dataSource, String query, Class<T> requiredType) throws Exception {

		List<T> object = getSpringJdbcTemplate(dataSource).query(query, new BeanPropertyRowMapper(requiredType));
		return object;
	}

	public Long findTotalRecordCount(DataSource dataSource, String query, Object... params) throws Exception {

		Long count = 0l;
		if (params == null || params.length == 0) {
			count = getSpringJdbcTemplate(dataSource).queryForObject(query, Long.class);
		} else {
			count = getSpringJdbcTemplate(dataSource).queryForObject(query, Long.class, params);
		}
		return count;
	}

	public Long findTotalRecordCount(DataSource dataSource, Object[] params, String query) throws Exception {

		Long count = 0l;
		if (params == null || params.length == 0) {
			count = getSpringJdbcTemplate(dataSource).queryForObject(query, Long.class);
		} else {
			count = getSpringJdbcTemplate(dataSource).queryForObject(query, Long.class, params);
		}
		return count;
	}

	public Long findTotalRecordCount(DataSource dataSource, String query) throws Exception {

		Long count = 0l;
		count = getSpringJdbcTemplate(dataSource).queryForObject(query, Long.class);
		return count;
	}

	public void rollBackTransaction(PlatformTransactionManager transactionManager, TransactionStatus status) {

		transactionManager.rollback(status);
	}

	public void commitTransaction(PlatformTransactionManager transactionManager, TransactionStatus status) {

		transactionManager.commit(status);
	}

	public TransactionStatus beginTransAndGetStatus(PlatformTransactionManager transactionManager) {

		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		return status;
	}

	public JdbcTemplate getSpringJdbcTemplate(DataSource dataSource) {

		return new JdbcTemplate(dataSource);
	}

	public PlatformTransactionManager getTransactionManager(DataSource dataSource) {

		return new DataSourceTransactionManager(dataSource);
	}

	public int saveOrUpdate(DataSource dataSource, String query, boolean enableTransMgmt) throws Exception {

		int affectedRows = 0;
		if (enableTransMgmt) {
			affectedRows = getSpringJdbcTemplate(dataSource).update(query);
			return affectedRows;

		} else {
			PlatformTransactionManager transactionManager = getTransactionManager(dataSource);
			TransactionStatus status = beginTransAndGetStatus(transactionManager);
			try {
				affectedRows = getSpringJdbcTemplate(dataSource).update(query);
				if (affectedRows > 0)
					commitTransaction(transactionManager, status);
				else
					rollBackTransaction(transactionManager, status);
			} catch (Exception e) {
				System.err.println("Excpetion Raised in saveOrUpdate(DataSource dataSource, String query,boolean enableTransMgmt): " + e);
				Map<String, Object> inputParamMap = new HashMap<String, Object>();
				inputParamMap.put("query", query);
				inputParamMap.put("enableTransMgmt", enableTransMgmt);
				// EmailUtility.prepareAndSendExceptionEmail(null,e,inputParamMap);
				rollBackTransaction(transactionManager, status);
				throw e;
			}
			return affectedRows;
		}
	}

	public int saveOrUpdate(DataSource dataSource, String query, boolean enableTransMgmt, Object... params) throws Exception {

		int affectedRows = 0;
		if (enableTransMgmt) {
			affectedRows = getSpringJdbcTemplate(dataSource).update(query, params);
			return affectedRows;
		} else {
			PlatformTransactionManager transactionManager = getTransactionManager(dataSource);
			TransactionStatus status = beginTransAndGetStatus(transactionManager);
			try {
				affectedRows = getSpringJdbcTemplate(dataSource).update(query, params);
				if (affectedRows > 0)
					commitTransaction(transactionManager, status);
				else
					rollBackTransaction(transactionManager, status);
			} catch (Exception e) {
				// log.error(
				// "Excpetion Raised in saveOrUpdate(DataSource dataSource, String query,boolean
				// enableTransMgmt,Object... params): ",
				// e);
				Map<String, Object> inputParamMap = new HashMap<String, Object>();
				inputParamMap.put("query", query);
				inputParamMap.put("enableTransMgmt", enableTransMgmt);
				inputParamMap.put("params", params);
				// EmailUtility.prepareAndSendExceptionEmail(null,e,inputParamMap);
				rollBackTransaction(transactionManager, status);
				throw e;
			}
			return affectedRows;
		}

	}

	public int saveOrUpdate(DataSource dataSource, String query, Object[] params, boolean enableTransMgmt) throws Exception {

		int affectedRows = 0;
		if (enableTransMgmt) {
			affectedRows = getSpringJdbcTemplate(dataSource).update(query, params);
			return affectedRows;
		} else {
			PlatformTransactionManager transactionManager = getTransactionManager(dataSource);
			TransactionStatus status = beginTransAndGetStatus(transactionManager);
			try {
				affectedRows = getSpringJdbcTemplate(dataSource).update(query, params);
				if (affectedRows > 0)
					commitTransaction(transactionManager, status);
				else
					rollBackTransaction(transactionManager, status);
			} catch (Exception e) {
				// log.error(
				// "Excpetion Raised in saveOrUpdate(DataSource dataSource, String query,boolean
				// enableTransMgmt,Object... params): ",
				// e);
				Map<String, Object> inputParamMap = new HashMap<String, Object>();
				inputParamMap.put("query", query);
				inputParamMap.put("enableTransMgmt", enableTransMgmt);
				inputParamMap.put("params", params);
				// EmailUtility.prepareAndSendExceptionEmail(null,e,inputParamMap);
				rollBackTransaction(transactionManager, status);
				throw e;
			}
			return affectedRows;
		}

	}

	public int delete(DataSource dataSource, String query, boolean enableTransMgmt) throws Exception {

		return saveOrUpdate(dataSource, query, enableTransMgmt);
	}

	public int delete(DataSource dataSource, String query, boolean enableTransMgmt, Object... params) throws Exception {

		return saveOrUpdate(dataSource, query, enableTransMgmt, params);
	}

	/*
	 * Methods :: SpringJdbcTemplate Ends
	 * ............................................................
	 * .........................................................
	 */

	/*
	 * Methods :: NamedParameterJdbcTemplate Starts
	 * ...................................................
	 * ........................................................
	 */

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(DataSource dataSource) {

		return new NamedParameterJdbcTemplate(dataSource);
	}

	/**
	 * If you set column values in bean then send object instance which contains values. In case
	 * where there no column value required to be send then simple pass ClassName.class For Ex:
	 * query = "INSERT INTO USERLOGIN (USER_ID) VALUES ('80') ";
	 * saveOrUpdateBeanUsingNamedJDBC(dataSource, query,Login.class);
	 * 
	 * If contains Value : then pass object of Login class Login login = new Login();
	 * login.setUserId("25"); query = "INSERT INTO USERLOGIN (USER_ID) VALUES (:userId) ";
	 * saveOrUpdateBeanUsingNamedJDBC(dataSource, query,login);
	 */

	public <T> int saveOrUpdateBeanUsingNamedJDBC(DataSource dataSource, String query, T obj, boolean enableTransMgmt) throws Exception {

		int affectedRows = 0;
		if (enableTransMgmt) {
			try {
				SqlParameterSource sqlParams = new BeanPropertySqlParameterSource(obj);
				affectedRows = getNamedParameterJdbcTemplate(dataSource).update(query, sqlParams);
				return affectedRows;
			} catch (Exception e) {
				// log.error(
				// "Excpetion Raised in saveOrUpdateBeanUsingNamedJDBC(DataSource dataSource,String
				// query,T obj,boolean enableTransMgmt)",
				// e);
				Map<String, Object> inputParamMap = new HashMap<String, Object>();
				inputParamMap.put("query", query);
				inputParamMap.put("enableTransMgmt", enableTransMgmt);
				inputParamMap.put("obj", obj);
				// EmailUtility.prepareAndSendExceptionEmail(null,e,inputParamMap);
				throw e;
			}
		} else {
			PlatformTransactionManager transactionManager = getTransactionManager(dataSource);
			TransactionStatus status = beginTransAndGetStatus(transactionManager);
			try {
				SqlParameterSource sqlParams = new BeanPropertySqlParameterSource(obj);
				affectedRows = getNamedParameterJdbcTemplate(dataSource).update(query, sqlParams);
				if (affectedRows > 0)
					commitTransaction(transactionManager, status);
				else
					rollBackTransaction(transactionManager, status);
			} catch (Exception e) {
				// log.error(
				// "Excpetion Raised in saveOrUpdateBeanUsingNamedJDBC(DataSource dataSource,String
				// query,T obj,boolean enableTransMgmt)",
				// e);
				rollBackTransaction(transactionManager, status);
				Map<String, Object> inputParamMap = new HashMap<String, Object>();
				inputParamMap.put("query", query);
				inputParamMap.put("enableTransMgmt", enableTransMgmt);
				inputParamMap.put("obj", obj);
				// EmailUtility.prepareAndSendExceptionEmail(null,e,inputParamMap);
				throw e;
			}

			return affectedRows;
		}

	}

	public <T> T findObjectUsingNamedJDBC(DataSource dataSource, String query, Class<T> requiredType, Object obj) throws Exception {

		SqlParameterSource sqlParams = new BeanPropertySqlParameterSource(obj);
		T t = (T) getNamedParameterJdbcTemplate(dataSource).queryForObject(query, sqlParams, requiredType);
		return t;
	}

	public Map<String, Object> findMapUsingNamedJDBC(DataSource dataSource, String query, Object obj) throws Exception {

		SqlParameterSource sqlParams = new BeanPropertySqlParameterSource(obj);
		return getNamedParameterJdbcTemplate(dataSource).queryForMap(query, sqlParams);
	}

	public List<Map<String, Object>> findAllUsingNamedJDBC(DataSource dataSource, String query, Object obj) throws Exception {

		SqlParameterSource sqlParams = new BeanPropertySqlParameterSource(obj);
		return getNamedParameterJdbcTemplate(dataSource).queryForList(query, sqlParams);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> findAllUsingNamedJDBC(DataSource dataSource, String query, Class<T> requiredType, T obj) throws Exception {

		SqlParameterSource sqlParams = new BeanPropertySqlParameterSource(obj);
		return getNamedParameterJdbcTemplate(dataSource).query(query, sqlParams, new BeanPropertyRowMapper(requiredType));
	}

	public <T> long saveAndGetAutoGeneratedKeyUsingNamedJDBC(DataSource dataSource, String query, Class<T> requiredType, T obj) {

		SqlParameterSource fileParameters = new BeanPropertySqlParameterSource(obj);
		KeyHolder keyHolder = new GeneratedKeyHolder();
		getNamedParameterJdbcTemplate(dataSource).update(query, fileParameters, keyHolder);
		return keyHolder.getKey().longValue();
	}

	public <T> int deleteUsingNamedJDBC(DataSource dataSource, String query, T obj, boolean enableTransMgmt) throws Exception {

		return saveOrUpdateBeanUsingNamedJDBC(dataSource, query, obj, enableTransMgmt);
	}

	/*
	 * Methods :: NamedParameterJdbcTemplate Ends
	 * ...................................................
	 * ........................................................
	 */

	public boolean resetSQL(StringBuilder sql) {

		sql.setLength(0);
		return true;
	}

	protected <T> T newInstance(Class<T> c) throws Exception {

		return c.newInstance();

	}

	public int insert(DataSource dataSource, String tableName, Map<String, Object> paramMap) {

		int insertedRow = new SimpleJdbcInsert(dataSource).withTableName(tableName).execute(paramMap);
		return insertedRow;
	}

	public int insert(DataSource dataSource, String tableName, Map<String, Object> paramMap, String... columnNamesToBeInserted) {

		int insertedRow = new SimpleJdbcInsert(dataSource).withTableName(tableName).usingColumns(columnNamesToBeInserted).execute(paramMap);
		return insertedRow;
	}

	public Number insertAndReturnGeneratedKey(DataSource dataSource, String tableName, Map<String, Object> paramMap, String autoGeneratedKeycolumnName) {

		Number autoGeneratedRow = new SimpleJdbcInsert(dataSource).withTableName(tableName).usingGeneratedKeyColumns(autoGeneratedKeycolumnName).executeAndReturnKey(paramMap);
		return autoGeneratedRow;
	}

	public Number insertAndReturnGeneratedKey(DataSource dataSource, String tableName, Map<String, Object> paramMap, String autoGeneratedKeycolumnName, String... columnNamesToBeInserted) {

		Number autoGeneratedRow = new SimpleJdbcInsert(dataSource).withTableName(tableName).usingColumns(columnNamesToBeInserted).usingGeneratedKeyColumns(autoGeneratedKeycolumnName)
			.executeAndReturnKey(paramMap);
		return autoGeneratedRow;
	}

	public KeyHolder insertAndReturnGeneratedKeyHolder(DataSource dataSource, String tableName, Map<String, Object> paramMap, String... autoGeneratedKeycolumnName) {

		KeyHolder keyHolder = new SimpleJdbcInsert(dataSource).withTableName(tableName).usingGeneratedKeyColumns(autoGeneratedKeycolumnName).executeAndReturnKeyHolder(paramMap);
		return keyHolder;
	}

	public static String preparedMultipleSQLParamInput(int length) {

		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= length; i++) {
			sb.append("?,");
		}
		return sb.toString().substring(0, sb.toString().length() - 1);
	}

	public static List<Object> preparedMultipleSQLValueInput(List<Object> params, String[] ids) {

		for (int i = 0; i < ids.length; i++) {
			params.add(ids[i]);
		}
		return params;
	}
	
	public static List<Object> preparedMultipleSQLValueInput(List<Object> params, Object[] ids) {
		for (int i = 0; i < ids.length; i++) {
			params.add(ids[i]);
		}
		return params;
	}

	@Override
	public Number insert(DataSource dataSource, String tableName, String primaryColumnName, Map<String, ?> mapParameterSource) throws IllegalArgumentException {

		SimpleJdbcInsert insertActor = new SimpleJdbcInsert(dataSource).withTableName(tableName).usingGeneratedKeyColumns(primaryColumnName);
		if (mapParameterSource == null || mapParameterSource.isEmpty())
			throw new IllegalArgumentException("Please call this method with parameterized map in which key must be column key and map value should be the value which you need to insert.");
		SqlParameterSource parameters = new MapSqlParameterSource(mapParameterSource);
		Number newId = insertActor.executeAndReturnKey(parameters);
		return newId;
	}
	
	@Override
	public Map<String,Object> doHistoryUpdate(DataSource dataSource, long userId, 
			String actionType, String tableName, String idColumnName, long idValue,
		      String tableColumns, String reason) throws Exception {
		  JdbcTemplate jdbcTemplate = getSpringJdbcTemplate(dataSource);
	      jdbcTemplate.setResultsMapCaseInsensitive(true);
	      SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
	                .withProcedureName("sp_HistoryUpdate")
	                .withoutProcedureColumnMetaDataAccess()
	                .declareParameters(
	                        new SqlParameter("userId", Types.INTEGER),
	                        new SqlParameter("actionCode", Types.VARCHAR),
	                        new SqlParameter("tableName", Types.VARCHAR),
	                        new SqlParameter("columnIdName", Types.VARCHAR),
	                        new SqlParameter("columnIdValue", Types.INTEGER),
	                        new SqlParameter("columnList", Types.VARCHAR),
	                        new SqlParameter("reason", Types.VARCHAR)
	                        
	                		);
	        
	        SqlParameterSource in = new MapSqlParameterSource()
	        		.addValue("userId", userId)
	        		.addValue("actionCode", actionType)
    				.addValue("tableName", tableName)
					.addValue("columnIdName", idColumnName)		
    				.addValue("columnIdValue", idValue)
    				.addValue("columnList", tableColumns)
    				.addValue("reason", getStringForSql(reason));
	                
	        return simpleJdbcCall.execute(in);
	}
	 public static String getStringForSql(String s) {
	    // function that replaces single quotations in SQL strings
	    if (s == null) {
	      return "";
	    }
	    String sForSql = s.replaceAll("'", "''");

	    return sForSql;
	  }
	 
	 public static String getStringForSqlSearch(String s) {
	    // function that replaces special characters in SQL strings
	    if (s == null) {
	      return "";
	    }
	    String sForSql = s.replaceAll("'", "''").replaceAll("\\\\", "\\\\").replaceAll("%", "\\%").replaceAll("_", "\\_");

	    return sForSql;
	  }

}
