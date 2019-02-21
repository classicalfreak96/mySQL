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
		// setup
		Catalog c = new Catalog();
		// Pre made class WhereExpressionVisitor let's us parse the WHERE clause easily
		WhereExpressionVisitor whereExpressionVisitor = new WhereExpressionVisitor();
		
		// FROM
		int tableId = c.getTableId(sb.getFromItem().toString()); // for Catalog class
		TupleDesc td = c.getTupleDesc(tableId);
		HeapFile hf = c.getDbFile(tableId);
		// all tuples from table
		ArrayList<Tuple> tuples = hf.getAllTuples();
		
		Relation r = new Relation(tuples, td);
		 
		// JOIN		Relation Operation: join
		List<Join> joins = sb.getJoins();
		if(joins != null ) {
			System.out.println("GET ON EXPRESSION " + joins.get(0).getOnExpression().toString()); // expression
			System.out.println("GET RIGHT ITEM " + joins.get(0).getRightItem().toString()); // table to join on
			
			for(int i = 0; i < joins.size(); i++) {
				Join j = joins.get(i);				
				// .toString() FEELS OFF, Table() class?
				int tableIdTemp = c.getTableId(j.getRightItem().toString()); // for Catalog class
				HeapFile hfTemp = c.getDbFile(tableIdTemp);
				ArrayList<Tuple> tuplesTemp = hfTemp.getAllTuples();
				Expression joinExpression = j.getOnExpression();
				joinExpression.accept(whereExpressionVisitor);
				
				// Relation.join(other relation, this relation's field, other relation's field)
				System.out.println("LEFT SIDE " + whereExpressionVisitor.getLeft());
				System.out.println("RIGHT SIDE " + whereExpressionVisitor.getRight());
				
			}
		}
		
		// WHERE		Relation Operation: select
		// CAREFUL: not all queries have WHERE clauses, handle or else throws a NullPointerException
		if(sb.getWhere() != null) {
			Expression e = sb.getWhere();
			// want to parse WHERE clause for arguments (left, right, and operand)
			// visit WHERE clause to parse
			e.accept(whereExpressionVisitor);
			RelationalOperator op = whereExpressionVisitor.getOp();
			String fieldName = whereExpressionVisitor.getLeft();
			int field = td.nameToId(fieldName);
			Field operand = whereExpressionVisitor.getRight();
			r = r.select(field, op, operand);
		}
		
		// SELECT	Relation operation: project
		// GROUP BY	Relation operation: aggregate
		List<SelectItem> selectItems = sb.getSelectItems(); // desired columns
		ArrayList<Integer> fields = new ArrayList<Integer>(); // columns to keep
		ColumnVisitor cv = new ColumnVisitor();
		boolean selectAllCols = false;
		for(int i = 0; i < selectItems.size(); i++) {
			selectItems.get(i).accept(cv);
			String column = cv.getColumn();
			System.out.println("COLUMN " + cv.getColumn());
//			System.out.println("COLUMN OPERATOR " + cv.getOp());
//			System.out.println("COLUMN AGGREGATE? " + cv.isAggregate());
			if(column.equals("*")) {
				// keep all columns
				selectAllCols = true;
				break;
			}
			int f = td.nameToId(column);
			fields.add(f);
		}
		if(!selectAllCols) {
			// need to remove unwanted columns before performing GROUP BY (only 1 or 2 columns are handled)
			r = r.project(fields);
		}
		
		List<Expression> gbExpressions = sb.getGroupByColumnReferences();
		if(gbExpressions != null) { // GROUP BY performed
			// in this case, we are allowed to assume first column is GROUP BY and second is aggregate
			AggregateOperator op = cv.getOp();
			try {
				r = r.aggregate(op, true);
			} catch (Exception e) { // ** we aren't throwing anything inside Relation.aggregate()...
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(cv.isAggregate()) { // aggregate w/out GROUP BY
			// in this case, we are allowed to assume there is only one column referenced
			AggregateOperator op = cv.getOp();
			try {
				r = r.aggregate(op, false);
			} catch (Exception e) { // ** we aren't throwing anything inside Relation.aggregate()...
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return r;
		
	}
}
