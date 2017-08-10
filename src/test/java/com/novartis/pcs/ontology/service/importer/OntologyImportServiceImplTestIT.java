package com.novartis.pcs.ontology.service.importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OntologyImportServiceImplTestIT {

	private IDatabaseTester databaseTester;

	private List<String> baseIgnoredColumns = Arrays.asList("CREATED_BY", "CREATED_DATE", "CREATED_VERSION_ID", "APPROVED_VERSION_ID", "OBSOLETE_VERSION_ID",
			"REPLACED_BY");

	public OntologyImportServiceImplTestIT() throws ClassNotFoundException, DataSetException {
		databaseTester = new JdbcDatabaseTester("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1521:xe", "artur", "artur");
		databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
		databaseTester.setDataSet(new FlatXmlDataSetBuilder().build(this.getClass().getResourceAsStream("/basic_dataset.xml")));
		databaseTester.setTearDownOperation(DatabaseOperation.DELETE_ALL);
	}
	
	@Before
	public void setUp() throws Exception {
//		databaseTester.onSetup();
	}

	@After
	public void tearDown() throws Exception {
//		databaseTester.onTearDown();
	}

	@Test
	public void shouldImportObo() throws Exception {
		// @formatter:off
		///*
	
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			HttpPut httpPut = new HttpPut("http://localhost:8080/ontobrowser/ontologies/BioAssay%20OBO");
			InputStreamEntity entity = new InputStreamEntity(getClass().getResourceAsStream("/bao.obo"));
			entity.setContentType("application/obo;charset=utf-8");
			entity.setContentEncoding("utf-8");
			httpPut.setEntity(entity);
			httpPut.setHeader("REMOTE_USER", "SYSTEM");
			CloseableHttpResponse response = httpClient.execute(httpPut);
			try {
				System.out.println("----------------------------------------");
				System.out.println(response.getStatusLine());
				System.out.println(EntityUtils.toString(response.getEntity()));
			} finally {
				response.close();
			}
		} finally {
			httpClient.close();
		}
		
		//*/
		// @formatter:on

		IDataSet actualDataSet = databaseTester.getConnection().createDataSet();
		IDataSet expectedDataSet = new FlatXmlDataSetBuilder().setColumnSensing(true).build(this.getClass().getResourceAsStream("/bao_obo_dbunit.xml"));

		compareTable(actualDataSet, expectedDataSet, "TERM", "TERM_NAME", Arrays.asList("TERM_ID", "ONTOLOGY_ID", "DEFINITION_URL"));
		compareTable(actualDataSet, expectedDataSet, "RELATIONSHIP_TYPE", "RELATIONSHIP_TYPE",
				Arrays.asList("RELATIONSHIP_TYPE_ID", "INVERSE_OF", "TRANSITIVE_OVER"));
		compareTable(actualDataSet, expectedDataSet, "TERM_SYNONYM", "TERM_SYNONYM",
				Arrays.asList("TERM_SYNONYM_ID", "TERM_ID", "CTRLD_VOCAB_TERM_ID", "DATASOURCE_ID", "REFERENCE_ID", "XREF_URL", "DESCRIPTION"));
		compareTable(actualDataSet, expectedDataSet, "DATASOURCE", "DATASOURCE_NAME", Arrays.asList("DATASOURCE_ID", "MODIFIED_BY", "MODIFIED_DATE",
				"RELEASE_DATE", "VERSION_NUMBER"));
		
	}

	private void compareTable(IDataSet actualDataSet, IDataSet expectedDataSet, String tableName, String sortColumn, List<String> ignoredColumns)
			throws DataSetException, DatabaseUnitException {
		List<String> allIgnored = new ArrayList<String>(baseIgnoredColumns);
		allIgnored.addAll(ignoredColumns);
		ITable actualTermTable = new SortedTable(actualDataSet.getTable(tableName), new String[] { sortColumn });
		ITable expectedTermTable = new SortedTable(expectedDataSet.getTable(tableName), new String[] { sortColumn });

		Assertion.assertEqualsIgnoreCols(expectedTermTable, actualTermTable, allIgnored.toArray(new String[] {}));
	}

}
