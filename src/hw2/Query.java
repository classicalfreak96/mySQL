package hw2;

import java.util.ArrayList;
import java.util.List;

import hw1.Catalog;
import hw1.Field;
import hw1.HeapFile;
import hw1.RelationalOperator;
import hw1.Tuple;
import hw1.TupleDesc;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;

public class Query {

	private String q;
	
	public Query(String q) {
		this.q = q;
	}
	
	public Relation execute()  {
		Statement statement = null;
		try {
			statement = CCJSqlParserUtil.parse(q);
		} catch (JSQLParserException e) {
			System.out.println("Unable to parse query");
			e.printStackTrace();
		}
		Select selectStatement = (Select) statement;
		PlainSelect sb = (PlainSelect)selectStatement.getSelectBody();
		
		System.out.println("==================== " + sb.toString());
		
		
		//your code here
		Catalog c = new Catalog();
		// FROM
		Table t = new Table();
		
		// t.accept(sb.getFromItem());
		System.out.println(sb.getFromItem().toString());
		int tableId = c.getTableId(sb.getFromItem().toString()); // for Catalog class
		TupleDesc td = c.getTupleDesc(tableId);
		HeapFile hf = c.getDbFile(tableId);
		// all tuples from table
		ArrayList<Tuple> tuples = hf.getAllTuples();
		
		
		// WHERE
		// Relation Operation: select
		Relation r = new Relation(tuples, td);
		Expression e = sb.getWhere();
		
		// want to parse WHERE clause for arguments (left, right, and operand)
		// Pre made class WhereExpressionVisitor let's us parse the WHERE clause easily
		WhereExpressionVisitor whereExpression = new WhereExpressionVisitor();

		// CAREFUL: not all queries have WHERE clauses, so can throw a NullPointerException
		e.accept(whereExpression);
		RelationalOperator op = whereExpression.getOp();
		String fieldName = whereExpression.getLeft();
		int field = td.nameToId(fieldName);
		Field operand = whereExpression.getRight();
		r = r.select(field, op, operand);
		
		// SELECT
		// Relation operation: project
		List<SelectItem> selectItems = sb.getSelectItems(); // desired columns
		ArrayList<Integer> fields = new ArrayList<Integer>();
		ColumnVisitor cv = new ColumnVisitor();
		selectItems.get(0).accept(cv);
		for(int i = 0; i < selectItems.size(); i++) {
			selectItems.get(i).accept(cv);
			int f = td.nameToId(cv.getColumn());
			fields.add(f);
		}
		r = r.project(fields);
		
		// GROUP BY
		// Relation operation: aggregate
		
		
		return r;
		
	}
}
