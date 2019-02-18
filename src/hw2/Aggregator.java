package hw2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import hw1.IntField;
import hw1.RelationalOperator;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

/**
 * A class to perform various aggregations, by accepting one tuple at a time
 * 
 * @author Doug Shook
 *
 */
public class Aggregator {

	private AggregateOperator aggregateOperation;
	private boolean groupBy;
	private TupleDesc td;
	private ArrayList<Tuple> result;
	private ArrayList<String> groups; //contains list of groups 

	public Aggregator(AggregateOperator o, boolean groupBy, TupleDesc td) throws Exception {
		// your code here
		this.aggregateOperation = o;
		this.groupBy = groupBy;
		this.td = td;
		result = new ArrayList<Tuple>();
		groups = new ArrayList<String>();
	}

	/**
	 * Merges the given tuple into the current aggregation
	 * 
	 * @param t the tuple to be aggregated
	 * @throws Exception if average or sum function is called on a string
	 */
	public void merge(Tuple t) throws Exception {
		//if groupBy is true
		if (groupBy) {
			if (aggregateOperation == AggregateOperator.MIN) {						//aggregate min
				if (groups != null && groups.contains(t.getField(0).toString())) {	//if group already exists
					int index = groups.indexOf(t.getField(0).toString());			//get index of group
					this.result.set(index, compare(RelationalOperator.LT, t, this.result.get(index), 1));	//update based on which value is less
				} else {									//if group doesn't exist
					groups.add(t.getField(0).toString());	//add group
					this.result.add(t);
				}
			}
			if (aggregateOperation == AggregateOperator.MAX) {						//aggregate max, same idea as above
				if (groups != null && groups.contains(t.getField(0).toString())) {
					int index = groups.indexOf(t.getField(0).toString());
					this.result.set(index, compare(RelationalOperator.GT, t, this.result.get(index), 1));
				} else {
					groups.add(t.getField(0).toString());
					this.result.add(t);
				}
			}
			if (aggregateOperation == AggregateOperator.COUNT) {					//aggregate count
				if (groups != null && groups.contains(t.getField(0).toString())) {	//if group exists
					int index = groups.indexOf(t.getField(0).toString());			//get index of group in results array
					int count = ((IntField) this.result.get(index).getField(1)).getValue();	//get current count
					count++;								//increment count
					Tuple tuple = new Tuple(this.td);		//create new tuple with updated count, place into results array
					tuple.setField(0, t.getField(0));
					tuple.setField(1, new IntField(count));
					this.result.set(index, tuple);
				} else {									//if group doesn't exist
					groups.add(t.getField(0).toString());	//add into group
					Tuple tuple = new Tuple(this.td);		//create new tuple with count 1, place into results array
					tuple.setField(0, t.getField(0));
					tuple.setField(1, new IntField(1));
					this.result.add(tuple);
				}
			}
			if (aggregateOperation == AggregateOperator.AVG) {			//aggregate average
				if (this.td.getType(1) == Type.STRING) {				//throws error if trying to aggregate string
					throw new Exception("Strings cannot be averaged");
				}
				this.result.add(t);										//adds tuple to results array. average handled by getResults
			}
			if (aggregateOperation == AggregateOperator.SUM) {			//aggregate sum
				if (this.td.getType(1) == Type.STRING) {				//throws error if trying to aggregate string
					throw new Exception("Strings cannot be summed");
				}
				if (groups != null && groups.contains(t.getField(0).toString())) {	//all same as count, except instead of incrementing count, sum together values in field
					if (groups.contains(t.getField(0).toString())) {
						int index = groups.indexOf(t.getField(0).toString());
						int sum = ((IntField) this.result.get(index).getField(1)).getValue();
						sum += ((IntField) t.getField(1)).getValue();
						Tuple tuple = new Tuple(this.td);
						tuple.setField(0, t.getField(0));
						tuple.setField(1, new IntField(sum));
						this.result.set(index, tuple);
					} 
				} 
				else {
					groups.add(t.getField(0).toString());
					Tuple tuple = new Tuple(this.td);
					tuple.setField(0, t.getField(0));
					tuple.setField(1, t.getField(1));
					this.result.add(tuple);
				}
			}
		} else {	//if not group by. All functions same as above except doesn't check for existing group/no group name column
			if (aggregateOperation == AggregateOperator.MIN) {
				if (result.isEmpty()) {
					result.add(new Tuple(this.td));
				}
				this.result.set(0, compare(RelationalOperator.LT, t, this.result.get(0), 0));
			}
			if (aggregateOperation == AggregateOperator.MAX) {
				if (result.isEmpty()) {
					result.add(new Tuple(this.td));
				}
				this.result.set(0, compare(RelationalOperator.GT, t, this.result.get(0), 0));
			}
			if (aggregateOperation == AggregateOperator.COUNT) {
				if (result.isEmpty()) {
					Tuple tuple = new Tuple(this.td);
					tuple.setField(0, new IntField(1));
					result.add(tuple);
				} else {
					int count = ((IntField) this.result.get(0).getField(0)).getValue();
					count++;
					Tuple tuple = new Tuple(this.td);
					tuple.setField(0, new IntField(count));
					this.result.set(0, tuple);
				}
			}
			if (aggregateOperation == AggregateOperator.AVG) {
				if (this.td.getType(0) == Type.STRING) {
					throw new Exception("Strings cannot be averaged");
				}
				this.result.add(t);
			}
			if (aggregateOperation == AggregateOperator.SUM) {
				if (this.td.getType(0) == Type.STRING) {
					throw new Exception("Strings cannot be summed");
				}
				if (result.isEmpty()) {
					Tuple tuple = new Tuple(this.td);
					tuple.setField(0, t.getField(0));
					result.add(tuple);
				} else {
					int sum = ((IntField) this.result.get(0).getField(0)).getValue();
					sum += ((IntField) t.getField(0)).getValue();
					Tuple tuple = new Tuple(this.td);
					tuple.setField(0, new IntField(sum));
					this.result.set(0, tuple);
				}
			}
		}
	}

	// helper function to use in min and max AggregateOperator calls
	public Tuple compare(RelationalOperator o, Tuple t, Tuple other, int index) {
		Tuple toSet = new Tuple(t.getDesc());
		if (t.getDesc().getType(0) == Type.INT) {
			IntField operand1 = (IntField) t.getField(index);
			IntField operand2 = (IntField) other.getField(index);
			if (operand2 == null) {
				toSet.setField(index, operand1);
				return toSet;
			}
			if (operand1.compare(o, operand2)) {
				toSet.setField(index, operand1);
			} else {
				toSet.setField(index, operand2);
			}
		}
		if (t.getDesc().getType(index) == Type.STRING) {
			StringField operand1 = (StringField) t.getField(0);
			StringField operand2 = (StringField) other.getField(0);
			if (operand2 == null) {
				toSet.setField(index, operand1);
				return toSet;
			}
			if (operand1.compare(o, operand2)) {
				toSet.setField(index, operand1);
			} else {
				toSet.setField(index, operand2);
			}
		}
		return toSet;
	}

	/**
	 * Returns the result of the aggregation
	 * 
	 * @return a list containing the tuples after aggregation
	 */
	public ArrayList<Tuple> getResults() {
		if (this.aggregateOperation != AggregateOperator.AVG) {
			return this.result;
		} 
		else {
			if (groupBy) {
				ArrayList<Integer> counts = new ArrayList<Integer>();		//counts number of tuples in each group
				ArrayList<Integer> sums = new ArrayList<Integer>();			//sums up all tuples in each group
				for (Tuple tuple : this.result) {
					if (groups.contains(tuple.getField(0).toString())) {	//places all tuples into correct group, aggregating sum and incrementing count
						int index = groups.indexOf(tuple.getField(0).toString());
						sums.set(index, sums.get(index) + ((IntField) tuple.getField(1)).getValue());
						counts.set(index, counts.get(index) + 1);
					} else {
						groups.add(tuple.getField(0).toString());
						sums.add(((IntField) tuple.getField(1)).getValue());
						counts.add(1);
					}
				}
				ArrayList<Tuple> toReturn = new ArrayList<Tuple>();			//return ArrayList
				for (int i = 0; i < counts.size(); i++) {
					Tuple tuple = new Tuple(this.td);
					tuple.setField(0, new StringField(groups.get(i)));
					tuple.setField(1, new IntField((int) (sums.get(i) / counts.get(i))));	//compute average of each group
					toReturn.add(tuple);
				}
				return toReturn;	//returns
			} else {
				int sum = 0;		//same as above except no groups
				int count = 0;
				for (Tuple tuple : this.result) {
					if (tuple.getField(0) != null) {
						count++;
						sum += ((IntField) tuple.getField(0)).getValue();
					}
				}
				Tuple tuple = new Tuple(this.td);
				tuple.setField(0, new IntField((int) (sum / count)));
				ArrayList<Tuple> toReturn = new ArrayList<Tuple>();
				toReturn.add(tuple);
				return toReturn;
			}
		}
	}

}
