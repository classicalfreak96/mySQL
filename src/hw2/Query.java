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
import net.sf.jsqlparser.expression.ExpressionVisitor;
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
		Table t = (Table) sb.getFromItem();
		int tableId = c.getTableId(sb.getFromItem().toString()); // for Catalog class
		TupleDesc td = c.getTupleDesc(tableId);
		HeapFile hf = c.getDbFile(tableId);
		// all tuples from table
		ArrayList<Tuple> tuples = hf.getAllTuples();
		
		Relation r = new Relation(tuples, td);
		
		// JOIN		Relation Operation: join
		List<Join> joins = sb.getJoins();
		if(joins != null ) {
			for(int i = 0; i < joins.size(); i++) {
				Join j = joins.get(i);
				// JOIN performed on "this" table (refers to the A in ... JOIN A ON ...)
				Table thisTable = (Table) j.getRightItem();
				int tableIdJoin = c.getTableId(thisTable.getName()); // for Catalog class
				TupleDesc tdJoin = c.getTupleDesc(tableIdJoin);
				HeapFile hfJoin = c.getDbFile(tableIdJoin);
				ArrayList<Tuple> tuplesJoin = hfJoin.getAllTuples();
				Relation rJoin = new Relation(tuplesJoin, tdJoin);

				// extra setup to get columns and tables on both sides of expression
				EqualsTo joinExpression = (EqualsTo) j.getOnExpression(); // we can assume all JOIN conditions will use equals
				Column cLeft = (Column) joinExpression.getLeftExpression();
				Column cRight = (Column) joinExpression.getRightExpression();
				Table tLeft = cLeft.getTable();
				Table tRight = cRight.getTable();
				
				System.out.println("LEFT SIDE " + joinExpression.getLeftExpression().toString());
				System.out.println("RIGHT SIDE " + joinExpression.getRightExpression().toString());
				// create Relation of other table
				Table otherTable;
				if(tLeft.getName().equalsIgnoreCase(thisTable.getName())) {
					otherTable = tRight; // other table is on right side of expression
				} else {
					otherTable = tLeft; // other table is on left side of expression
				}
//				int otherTableId = c.getTableId(otherTable.getName()); // for Catalog class
//				TupleDesc otherTd = c.getTupleDesc(otherTableId);
//				HeapFile otherHf = c.getDbFile(otherTableId);
//				ArrayList<Tuple> otherTuples = otherHf.getAllTuples();
//				Relation otherR = new Relation(otherTuples, otherTd);
				// Relation otherR = r;
				System.out.println("THIS TABLE " + thisTable.getName());
				System.out.println("OTHER TABLE " + otherTable.getName());
				
				int thisField; // field from current table
				int otherField; // field from "other" table
				if(thisTable.getName().equalsIgnoreCase(tLeft.getName())) {
					thisField = tdJoin.nameToId(cLeft.getColumnName());
					otherField = td.nameToId(cRight.getColumnName());
//					otherField = otherTd.nameToId(cRight.getColumnName());
				} else {
					thisField = tdJoin.nameToId(cRight.getColumnName());
					otherField = td.nameToId(cLeft.getColumnName());
//					otherField = otherTd.nameToId(cLeft.getColumnName());
				}
				
				System.out.println("THIS COLUMN " + tdJoin.getFieldName(thisField));
				System.out.println("OTHER COLUMN " + td.getFieldName(otherField));
				
				try {
//					r = rJoin.join(r, thisField, otherField);
					r = r.join(rJoin, otherField, thisField);
					
//					if(i == 0) {
//						r = rJoin.join(r, thisField, otherField);
//						
//					} else {						
//						r = r.join(rJoin, otherField, thisField);
//					}
					
					td = r.getDesc();
					System.out.println("SIZE " + r.getTuples().size());
					System.out.println("CURRENT " + td.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("=============================================");
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
		System.out.println(td.toString());
		for(int i = 0; i < selectItems.size(); i++) {
			selectItems.get(i).accept(cv);
			String column = cv.getColumn();
			System.out.println("COLUMN " + column);
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
