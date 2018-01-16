package org.nalby.yobatis.mybatis;

import java.util.List;

import org.nalby.yobatis.sql.Table;

/**
 * It's common that models are grouped/categorized in some way, to support grouping models,
 * tables must be put into different contexts.
 * 
 * @author Kyle Lin
 *
 */
public interface ContextGrouper {
	
	/**
	 * Group tables in order to apply multiple contexts.
	 * @param tableList tables.
	 * @return a map whose keys are package names, and values are tables.
	 */
	List<TableGroup> group(List<Table> tableList);

}
