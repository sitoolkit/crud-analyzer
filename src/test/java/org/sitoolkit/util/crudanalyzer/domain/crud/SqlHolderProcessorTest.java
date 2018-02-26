package org.sitoolkit.util.crudanalyzer.domain.crud;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class SqlHolderProcessorTest {

	SqlHolderProcessor processor = new SqlHolderProcessor();
	
	@Test
	public void testIf() {
		String sql = processor.edit("a <if test=\"condition\"> b </if> c");
		assertThat(sql, is("a  b  c"));
	}

	@Test
	public void testForeach() {
		String sql = processor.edit("<foreach open=\"(\" separator=\",\" close=\")\"> 1 </foreach>");
		assertThat(sql, is("( 1 )"));
	}

	@Test
	public void testWhen() {
		String sql = processor.edit("<choose><when test=\"condition\"> 1 </when><otherwise> 2 </otherwise></choose>");
		assertThat(sql, is(" 1 "));
	}

}
