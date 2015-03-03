package start;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.ArrayList;

public class Mip1 {

	private static Integer numOfPlayers = 8;
	private static Integer numOfPositions = 5;
	private static Integer numOfPeriods = 4;

	private static Integer pMax = 3;
	private static Integer pMin = 2;
	private static Integer tMin = 2;
	private static Integer tMax = 3;

	private static ArrayList<GRBVar> y_ij = new ArrayList<GRBVar>();
	private static ArrayList<GRBVar> x_ijt = new ArrayList<GRBVar>();
	private static ArrayList<GRBVar> z_it = new ArrayList<GRBVar>();

	public static void main(String[] args) {
		try {
			GRBEnv env = new GRBEnv("mip1.log");
			GRBModel model = new GRBModel(env);

			// Create variables

			for (int i = 0; i < numOfPlayers; i++) {
				for (int j = 0; j < numOfPositions; j++) {

					y_ij.add(model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y_" + i
							+ "_" + j));

					for (int t = 0; t < numOfPeriods; t++) {

						x_ijt.add(model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x_"
								+ i + "_" + j + "_" + t));

					}
				}
			}

			for (int i = 0; i < numOfPlayers; i++) {
				for (int t = 0; t < numOfPeriods; t++) {

					z_it.add(model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "z_" + i
							+ "_" + t));

				}
			}

			// Integrate new variables

			model.update();

			// Set objective: maximize x + y + 2 z

			// GRBLinExpr expr = new GRBLinExpr();
			// expr.addTerm(1.0, x);
			// expr.addTerm(1.0, y);
			// expr.addTerm(2.0, z);
			// expr.addTerm(5.0, m);
			// expr.addTerm(6.0, j);
			// model.setObjective(expr, GRB.MAXIMIZE);

			// Add constraint: x + 2 y + 3 z <= 4

			addObjective(model, "objective");
			add1Bconstraints(model, "1b");
			add2_1constraints(model, "2.1");
			add2_2constraints(model, "2.2");
			add3_0constraints(model, "3.0");
			add4_bconstraints(model, "4b");
			add5_0constraints(model, "5.0");

			// Optimize model

			model.optimize();

			// model.optimizeasync();

			// results below

			for (GRBVar gv : x_ijt) {
				System.out.println(gv.get(GRB.StringAttr.VarName) + " "
						+ gv.get(GRB.DoubleAttr.X));
			}

			for (GRBVar gv : y_ij) {
				System.out.println(gv.get(GRB.StringAttr.VarName) + " "
						+ gv.get(GRB.DoubleAttr.X));
			}

			for (GRBVar gv : z_it) {
				System.out.println(gv.get(GRB.StringAttr.VarName) + " "
						+ gv.get(GRB.DoubleAttr.X));
			}

			System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));

			// Dispose of model and environment

			model.dispose();
			env.dispose();

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". "
					+ e.getMessage());
		}
	}

	private static void addObjective(GRBModel model, String string) {

		// model.setObjective(expr, GRB.MAXIMIZE);

	}

	private static void add5_0constraints(GRBModel model, String string)
			throws GRBException {

		for (int i = 0; i < numOfPlayers; i++) {
			GRBLinExpr expr = new GRBLinExpr();
			for (int j = 0; j < numOfPositions; j++) {
				for (int t = 0; t < numOfPeriods; t++) {

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

		for (int t = 0; t < numOfPeriods; t++) {
			for (int j = 0; j < numOfPositions; j++) {

				GRBLinExpr expr = new GRBLinExpr();
				for (int i = 0; i < numOfPlayers; i++) {

					expr.addTerm(1.0,
							model.getVarByName("x_" + i + "_" + j + "_" + t));

				}

				model.addConstr(expr, GRB.EQUAL, 1.0, "");
			}
		}
	}

	private static void add3_0constraints(GRBModel model, String string)
			throws GRBException {

		for (int i = 0; i < numOfPlayers; i++) {

			GRBLinExpr expr = new GRBLinExpr();
			for (int j = 0; j < numOfPositions; j++) {

				expr.addTerm(1.0, model.getVarByName("y_" + i + "_" + j));
			}
			model.addConstr(expr, GRB.GREATER_EQUAL, pMin, "");
			model.addConstr(expr, GRB.LESS_EQUAL, pMax, "");
		}
	}

	private static void add2_2constraints(GRBModel model, String string)
			throws GRBException {

		for (int i = 0; i < numOfPlayers; i++) {
			for (int j = 0; j < numOfPositions; j++) {

				GRBLinExpr expr = new GRBLinExpr();
				for (int t = 0; t < numOfPeriods; t++) {

					// divide term by 4
					expr.addTerm(.250,
							model.getVarByName("x_" + i + "_" + j + "_" + t));

				}

				expr.addTerm(-1.0, model.getVarByName("y_" + i + "_" + j));
				model.addConstr(expr, GRB.LESS_EQUAL, 0.0, "");
			}
		}

	}

	private static void add2_1constraints(GRBModel model, String string)
			throws GRBException {

		for (int i = 0; i < numOfPlayers; i++) {
			for (int j = 0; j < numOfPositions; j++) {

				GRBLinExpr expr = new GRBLinExpr();
				for (int t = 0; t < numOfPeriods; t++) {

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

		for (int i = 0; i < numOfPlayers; i++) {
			for (int t = 0; t < numOfPeriods; t++) {

				GRBLinExpr expr = new GRBLinExpr();
				for (int j = 0; j < numOfPositions; j++) {
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