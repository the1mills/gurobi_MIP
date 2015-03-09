package start;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.ArrayList;
import static java.lang.System.out;

public class Mip1 {
	

	private static Integer I = 15;  //number of players
	private static Integer J = 11;  //number of positions
	private static Integer T = 6;   //number of time periods

	private static Integer pMax = 3;  //maximum # of positions for each player
	private static Integer pMin = 2; //minimum # of positions for each players
	private static Integer tMin = 4; //minimum # of time blocks for each players
	private static Integer tMax = 6; //maximum # of time blocks for each players
	private static Integer bMax = 3;

	private static ArrayList<GRBVar> y_ij = new ArrayList<GRBVar>();
	private static ArrayList<GRBVar> x_ijt = new ArrayList<GRBVar>();
	private static ArrayList<GRBVar> z_it = new ArrayList<GRBVar>();
	

	public static void main(String[] args) {
		try {
			GRBEnv env = new GRBEnv("mip1.log");
			GRBModel model = new GRBModel(env);

			// Create variables, add them to model

			for (int i = 0; i < I; i++) {
				for (int j = 0; j < J; j++) {

					y_ij.add(model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y_" + i
							+ "_" + j));

					for (int t = 0; t < T; t++) {

						x_ijt.add(model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x_"
								+ i + "_" + j + "_" + t));

					}
				}
			}

			for (int i = 0; i < I; i++) {
				for (int t = 0; t < T; t++) {

					z_it.add(model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "z_" + i
							+ "_" + t));

				}
			}

			// Integrate variables into model

			model.update();

			
			//all constraints and objective function with variables defined from above

			//addObjective(model, "objective");
			add1Bconstraints(model, "1b");
			add2_1constraints(model, "2.1");
			add2_2constraints(model, "2.2");
			add3_0constraints(model, "3.0");
			add4_bconstraints(model, "4b");
			add5_0constraints(model, "5.0");
			add8_0constraints(model, "8.0");

			// Optimize model

			// model.optimizeasync();
			model.optimize();

			// results below

			for (GRBVar gv : x_ijt) {
				out.println(gv.get(GRB.StringAttr.VarName) + " "
						+ gv.get(GRB.DoubleAttr.X));
			}

			for (GRBVar gv : y_ij) {
				out.println(gv.get(GRB.StringAttr.VarName) + " "
						+ gv.get(GRB.DoubleAttr.X));
			}

			for (GRBVar gv : z_it) {
				out.println(gv.get(GRB.StringAttr.VarName) + " "
						+ gv.get(GRB.DoubleAttr.X));
			}

			out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));

			// Dispose of model and environment

			model.dispose();
			env.dispose();

		} catch (GRBException e) {
			out.println("Error code: " + e.getErrorCode() + ". "
					+ e.getMessage());
		}
	}

	private static void addObjective(GRBModel model, String string)
			throws GRBException {

		GRBLinExpr expr = new GRBLinExpr();
		for (int i = 0; i < I; i++) {
			for (int t = 0; t < T; t++) {
				if (i % 2 == 0) {
					expr.addTerm(2.0, model.getVarByName("z_" + i + "_" + t));
				}
			}
		}
		model.setObjective(expr, GRB.MINIMIZE);
	}

	
	private static void add8_0constraints(GRBModel model, String string) throws GRBException {
		
		
		for (int i = 0; i < I; i++) {
			for (int t = 0; t < T - bMax; t++) {
				GRBLinExpr expr = new GRBLinExpr();
				for (int q = t; q <= t + bMax; q++) {
					expr.addTerm(1.0,
							model.getVarByName("z_" + i + "_" + q));
//					out.println("z_" + i + "_" + q);
				}
//				out.println(expr);
				model.addConstr(expr, GRB.LESS_EQUAL, bMax, "");
			}
		}
	}
	
	private static void add5_0constraints(GRBModel model, String string)
			throws GRBException {

		for (int i = 0; i < I; i++) {
			GRBLinExpr expr = new GRBLinExpr();
			for (int j = 0; j < J; j++) {
				for (int t = 0; t < T; t++) {

					expr.addTerm(1.0,
							model.getVarByName("x_" + i + "_" + j + "_" + t));

				}
			}
			model.addConstr(expr, GRB.LESS_EQUAL, tMax, "");
			model.addConstr(expr, GRB.GREATER_EQUAL, tMin, "");
		}
	}

	private static void add4_bconstraints(GRBModel model, String string)
			throws GRBException {

		for (int t = 0; t < T; t++) {
			for (int j = 0; j < J; j++) {

				GRBLinExpr expr = new GRBLinExpr();
				for (int i = 0; i < I; i++) {

					expr.addTerm(1.0,
							model.getVarByName("x_" + i + "_" + j + "_" + t));

				}

				model.addConstr(expr, GRB.EQUAL, 1.0, "");
			}
		}
	}

	private static void add3_0constraints(GRBModel model, String string)
			throws GRBException {

		for (int i = 0; i < I; i++) {

			GRBLinExpr expr = new GRBLinExpr();
			for (int j = 0; j < J; j++) {

				expr.addTerm(1.0, model.getVarByName("y_" + i + "_" + j));
			}
			model.addConstr(expr, GRB.GREATER_EQUAL, pMin, "");
			model.addConstr(expr, GRB.LESS_EQUAL, pMax, "");
		}
	}

	private static void add2_2constraints(GRBModel model, String string)
			throws GRBException {

		for (int i = 0; i < I; i++) {
			for (int j = 0; j < J; j++) {

				GRBLinExpr expr = new GRBLinExpr();
				for (int t = 0; t < T; t++) {

					// add fractional coefficient to variables (1/numOfPeriods)
					expr.addTerm(((double)1.0)/((double)T),
							model.getVarByName("x_" + i + "_" + j + "_" + t));

				}

				expr.addTerm(-1.0, model.getVarByName("y_" + i + "_" + j));
				model.addConstr(expr, GRB.LESS_EQUAL, 0.0, "");
			}
		}

	}

	private static void add2_1constraints(GRBModel model, String string)
			throws GRBException {

		for (int i = 0; i < I; i++) {
			for (int j = 0; j < J; j++) {

				GRBLinExpr expr = new GRBLinExpr();
				for (int t = 0; t < T; t++) {

					expr.addTerm(1.0,
							model.getVarByName("x_" + i + "_" + j + "_" + t));

				}

				expr.addTerm(-1.0, model.getVarByName("y_" + i + "_" + j));
				model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, "");
			}
		}
	}

	private static void add1Bconstraints(GRBModel model, String s)
			throws GRBException {

		int index = 0;

		for (int i = 0; i < I; i++) {
			for (int t = 0; t < T; t++) {

				GRBLinExpr expr = new GRBLinExpr();
				for (int j = 0; j < J; j++) {
					index++;

					GRBVar v = model.getVarByName("x_" + i + "_" + j + "_" + t);
					expr.addTerm(1.0, v);

				}

				GRBVar v = model.getVarByName("z_" + i + "_" + t);
				expr.addTerm(1.0, v);
				model.addConstr(expr, GRB.EQUAL, 1.0, s + "" + index);
			}
		}

	}
}