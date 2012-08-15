
package edu.usc.csci572.jtran;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.filemgr.tools.CASAnalyzer;
import org.apache.oodt.cas.filemgr.util.SqlParser;
import org.apache.oodt.cas.metadata.Metadata;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;

public final class QueryTool {

	private static String freeTextBlock = "__FREE__";

	private XmlRpcFileManagerClient client = null;

	private static enum QueryType {
		LUCENE, SQL
	};

	/* our log stream */
	private static final Logger LOG = Logger.getLogger(QueryTool.class
			.getName());

	public QueryTool(URL fmUrl) throws InstantiationException {
		try {
			client = new XmlRpcFileManagerClient(fmUrl);
		} catch (ConnectionException e) {
			throw new InstantiationException(e.getMessage());
		}
	}

	public List queryForIds(org.apache.oodt.cas.filemgr.structs.Query query) {
		List prodIds = new Vector();
		List products = new Vector();

		List productTypes = safeGetProductTypes();

		if (productTypes != null && productTypes.size() > 0) {
			for (Iterator i = productTypes.iterator(); i.hasNext();) {
				ProductType type = (ProductType) i.next();
				try {
					products = client.query(query, type);
					if (products != null && products.size() > 0) {
						for (Iterator j = products.iterator(); j.hasNext();) {
							Product product = (Product) j.next();
							prodIds.add(product.getProductId());
						}
					}
				} catch (CatalogException e) {
					LOG.log(Level.WARNING,
							"Exception querying for: [" + type.getName()
									+ "] products: Message: " + e.getMessage());
				}

			}

		}

		return prodIds;
	}

	public List queryForProducts(org.apache.oodt.cas.filemgr.structs.Query query) {
		List productsResult = new Vector();
		List products = new Vector();

		List productTypes = safeGetProductTypes();

		if (productTypes != null && productTypes.size() > 0) {
			for (Iterator i = productTypes.iterator(); i.hasNext();) {
				ProductType type = (ProductType) i.next();
				try {
					products = client.query(query, type);
					if (products != null && products.size() > 0) {
						for (Iterator j = products.iterator(); j.hasNext();) {
							Product product = (Product) j.next();
							productsResult.add(product);
						}
					}
				} catch (CatalogException e) {
					LOG.log(Level.WARNING,
							"Exception querying for: [" + type.getName()
									+ "] products: Message: " + e.getMessage());
				}

			}

		}

		return productsResult;
	}
	
	private void generateCASQuery(
			org.apache.oodt.cas.filemgr.structs.Query casQuery,
			Query luceneQuery) {
		if (luceneQuery instanceof TermQuery) {
			Term t = ((TermQuery) luceneQuery).getTerm();
			if (t.field().equals(freeTextBlock)) {
				// nothing for now
			} else {
				casQuery.addCriterion(new TermQueryCriteria(t.field(), t.text()));
			}
		} else if (luceneQuery instanceof PhraseQuery) {
			Term[] t = ((PhraseQuery) luceneQuery).getTerms();
			if (t[0].field().equals(freeTextBlock)) {
				// nothing for now
			} else {
				for (int i = 0; i < t.length; i++)
					casQuery.addCriterion(new TermQueryCriteria(t[i].field(),
							t[i].text()));
			}
		} else if (luceneQuery instanceof RangeQuery) {
			Term startT = ((RangeQuery) luceneQuery).getLowerTerm();
			Term endT = ((RangeQuery) luceneQuery).getUpperTerm();
			casQuery.addCriterion(new RangeQueryCriteria(startT.field(), startT
					.text(), endT.text()));
		} else if (luceneQuery instanceof BooleanQuery) {
			BooleanClause[] clauses = ((BooleanQuery) luceneQuery).getClauses();
			for (int i = 0; i < clauses.length; i++) {
				generateCASQuery(casQuery, (clauses[i]).getQuery());
			}
		} else {
			throw new RuntimeException(
					"Error parsing query! Cannot determine clause type: ["
							+ luceneQuery.getClass().getName() + "] !");
		}
	}

	private List safeGetProductTypes() {
		List prodTypes = null;

		try {
			prodTypes = client.getProductTypes();
		} catch (RepositoryManagerException e) {
			LOG.log(Level.WARNING,
					"Error obtaining product types from file manager: ["
							+ client.getFileManagerUrl() + "]: Message: "
							+ e.getMessage());
		}

		return prodTypes;
	}
	
	private static Query parseQuery(String query) {

		QueryParser parser;
		// note that "__FREE__" is a control work for free text searching
		parser = new QueryParser(freeTextBlock, new CASAnalyzer());
		Query luceneQ = null;
		try {
			luceneQ = (Query) parser.parse(query);
		} catch (ParseException e) {
			System.out.println("Error parsing query text.");
			System.exit(-1);
		}
		return luceneQ;
	}
	
	private static String performSqlQuery(String query, String sortBy,
			String outputFormat, String delimiter, String filemgrUrl)
			throws MalformedURLException, CatalogException,
			ConnectionException, QueryFormulationException {
		ComplexQuery complexQuery = SqlParser.parseSqlQuery(query);
		complexQuery.setSortByMetKey(sortBy);
		complexQuery.setToStringResultFormat(outputFormat);
		List<QueryResult> results = new XmlRpcFileManagerClient(new URL(
				filemgrUrl)).complexQuery(complexQuery);
		StringBuffer returnString = new StringBuffer("");
		for (QueryResult qr : results)
			returnString.append(qr.toString() + delimiter);
		return returnString.substring(0,
				returnString.length() - delimiter.length());
	}

	private static void exit(String msg) {
		System.err.println(msg);
		System.exit(1);
	}

	public Metadata getMetadata(Product product) {
		Metadata met = null;

		try {
			met = this.client.getMetadata(product);
		} catch (Exception e) {
			throw new RuntimeException("Unable to get metadata for product: ["
					+ product.getProductName() + "]");
		}

		return met;
	}

	public static List searchForProducts(String fmUrlStr, String column, String value) {
		List products = null;
		String queryStr = column + ":" + value;
		URL fmUrl;
		try {
			fmUrl = new URL(fmUrlStr);
			QueryTool queryTool = new QueryTool(fmUrl);
			org.apache.oodt.cas.filemgr.structs.Query casQuery = 
				new org.apache.oodt.cas.filemgr.structs.Query();
			queryTool.generateCASQuery(casQuery, parseQuery(queryStr));

			products = queryTool.queryForProducts(casQuery);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		
		return products;
	}
	
	public static List searchForIds(String fmUrlStr, String column, String value) {
		List productIds = null;
		String queryStr = column + ":" + value;
		URL fmUrl;
		try {
			fmUrl = new URL(fmUrlStr);
			QueryTool queryTool = new QueryTool(fmUrl);
			org.apache.oodt.cas.filemgr.structs.Query casQuery = 
				new org.apache.oodt.cas.filemgr.structs.Query();
			queryTool.generateCASQuery(casQuery, parseQuery(queryStr));

			productIds = queryTool.queryForIds(casQuery);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		
		return productIds;
	}
	
	/* test to see if a product exist */
	public static boolean exist(String fmUrlStr, String column, String value) {
		boolean found = false;
		String queryStr = column + ":" + value;
		URL fmUrl;
		try {
			fmUrl = new URL(fmUrlStr);
			QueryTool queryTool = new QueryTool(fmUrl);
			org.apache.oodt.cas.filemgr.structs.Query casQuery = 
				new org.apache.oodt.cas.filemgr.structs.Query();
			queryTool.generateCASQuery(casQuery, parseQuery(queryStr));

			List prodIds = queryTool.queryForIds(casQuery);
			if (prodIds != null && prodIds.size() > 0) 
				found = true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		
		return found;
	}
	
	public static void main(String[] args) throws Exception {
		String usage = "Usage: QueryTool [options] \n" + "options: \n"
				+ "--url <fm url> \n" + "  Lucene like query options: \n"
				+ "    --lucene \n" + "         -query <query> \n"
				+ "  SQL like query options: \n" + "    --sql \n"
				+ "         -query <query> \n"
				+ "         -sortBy <metadata-key> \n"
				+ "         -outputFormat <output-format-string> \n";

		String fmUrlStr = null, queryStr = null, sortBy = null, outputFormat = null, delimiter = null;
		QueryType queryType = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--lucene")) {
				if (queryType != null)
					exit("ERROR: Can only perform one query at a time! \n"
							+ usage);
				if (args[++i].equals("-query"))
					queryStr = args[++i];
				else
					exit("ERROR: Must specify a query! \n" + usage);
				queryType = QueryType.LUCENE;
			} else if (args[i].equals("--sql")) {
				if (queryType != null)
					exit("ERROR: Can only perform one query at a time! \n"
							+ usage);
				if (args[++i].equals("-query"))
					queryStr = args[++i];
				else
					exit("ERROR: Must specify a query! \n" + usage);
				for (; i < args.length; i++) {
					if (args[i].equals("-sortBy"))
						sortBy = args[++i];
					else if (args[i].equals("-outputFormat"))
						outputFormat = args[++i];
					else if (args[i].equals("-delimiter"))
						delimiter = args[++i];
				}
				queryType = QueryType.SQL;
			} else if (args[i].equals("--url")) {
				fmUrlStr = args[++i];
			}
		}

		if (queryStr == null || fmUrlStr == null)
			exit("Must specify a query and filemgr url! \n" + usage);

		if (queryType == QueryType.LUCENE) {
			URL fmUrl = new URL(fmUrlStr);
			QueryTool queryTool = new QueryTool(fmUrl);
			org.apache.oodt.cas.filemgr.structs.Query casQuery = 
				new org.apache.oodt.cas.filemgr.structs.Query();
			queryTool.generateCASQuery(casQuery, parseQuery(queryStr));

			List prodIds = queryTool.queryForIds(casQuery);
			if (prodIds != null && prodIds.size() > 0) {
				for (Iterator i = prodIds.iterator(); i.hasNext();) {
					String prodId = (String) i.next();
					System.out.println(prodId);
				}
			}
		} else {
			System.out.println(performSqlQuery(queryStr, sortBy, outputFormat,
					delimiter != null ? delimiter : "\n", fmUrlStr));
		}
	}

}
