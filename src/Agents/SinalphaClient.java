package Agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.function.Supplier;

import Agents.Client;
import Utils.Constants;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

public class SinalphaClient extends Client {

	public Double min_accept_trust = Constants.MIN_ACCEPT_TRUST;
	public Double exp_number = 0.0;
	
	public HashMap<String, List<String>> past_experiences = new HashMap<String, List<String>>();
	
	public HashMap<String, List<Double>> X = new HashMap<String, List<Double>>();
	public HashMap<String, List<Double>> Y = new HashMap<String, List<Double>>();
	public HashMap<String, List<Double>> Beta0 = new HashMap<String, List<Double>>();
	public HashMap<String, List<Double>> Beta1 = new HashMap<String, List<Double>>();
	
	/**
	 * Setup method. Starts by reading the arguments and then adds a FIPAContractNetInit behaviour.
	 */
	public void setup() {
		
		Object [] args = getArguments();
		
		min_accept_trust = Double.parseDouble((String)args[0]);
		
		addBehaviour(new FIPAContractNetInit(this, new ACLMessage(ACLMessage.CFP)));
		
	}

	class FIPAContractNetInit extends ContractNetInitiator {

		/**
		 * Class constructor with two parameters.
		 * @param a - Agent.
		 * @param msg - Message.
		 */
		public FIPAContractNetInit(Agent a, ACLMessage msg) {
			
			super(a, msg);
			
		}
		
		/**
		 * Returns the vl value, depending on the experience.
		 * @param exp - Experience (F, Fd or V)
		 * @return Returns the vl value.
		 */
		private Double vl(String exp) {
			
			if(exp.equals("F")) //Fulfillment
				return 1.0;
			else if(exp.equals("Fd"))
				return 0.5;
			else
				return 0.0;
			
		}
		
		/**
		 * Adds the cumulative value at experience i (1, 2, 3, ..., N) in Y and adds the experience i in X
		 * 
		 * VERY IMPORTANT NOTE: See page 116 and 117!
		 *  
		 * @param exps - List of experiences with a certain supplier.
		 * @param supplier - Supplier name.
		 */
		private void cumValAgreem(List<String> exps, String supplier) {
			
			Double cumulative = 0.0;
			
			for(String exp : exps)
				cumulative += vl(exp);
			
			if(X.get(supplier) == null) {
				
				List<Double> X_exp = new ArrayList<Double>();
				X_exp.add(1.0);
				X.put(supplier, X_exp);
				
			} else {
				
				List<Double> X_values = X.get(supplier);
				
				X_values.add(X_values.get(X_values.size() - 1) + 1); // increase the number of experiences for a certain supplier
				
				X.put(supplier, X_values);
				
			}
			
			if(Y.get(supplier) == null) {
				
				List<Double> Y_exp = new ArrayList<Double>();
				Y_exp.add(cumulative);
				Y.put(supplier, Y_exp);
				
			} else {
				
				List<Double> Y_values = Y.get(supplier);
				Y_values.add(cumulative);
				
				Y.put(supplier, Y_values);
				
			}
			
			updateBetas(supplier);
			
		}
		
		/**
		 * Updates the betas based on a certain supplier
		 * @param supplier - Supplier name.
		 */
		private void updateBetas(String supplier) {
			
			Double sumX = 0.0, sumY = 0.0;
			Double x = 0.0, y = 0.0;
			Double xx = 0.0, yy = 0.0, xy = 0.0;
			Double beta0, beta1;
			
			List<Double> X_values = X.get(supplier);
			List<Double> Y_values = Y.get(supplier);
			List<Double> betas0;
			List<Double> betas1;
			
			if(Beta0.get(supplier) == null) {
				
				betas0 = new ArrayList<Double>();
				betas1 = new ArrayList<Double>();
				
			} else {
				
				betas0 = Beta0.get(supplier);
				betas1 = Beta1.get(supplier);
				
			}
			
			for(Double value : X_values)
				sumX += value;
			
			for(Double value : Y_values)
				sumY += value;
			
			x = sumX / X_values.size();
			y = sumY / Y_values.size();
			
			for(Integer i = 0; i < X_values.size(); i++) {
				
				xx += Math.pow((X_values.get(i) - x), 2);
				yy += Math.pow((Y_values.get(i) - y), 2);
				xy += (X_values.get(i) - x) * (Y_values.get(i) - y);
				
			}
			
			if(xx == 0)
				beta1 = 0.0;
			else
				beta1 = xy / xx;
			
			beta0 = y - (beta1 * x);
			
			if(betas0 == null) {
				
				betas0 = new ArrayList<Double>();
				betas1 = new ArrayList<Double>();
				
			}
			
			betas0.add(beta0);
			betas1.add(beta1);
			
			Beta0.put(supplier, betas0);
			Beta1.put(supplier, betas1);
			
		}

		/**
		 * Prepares the call for proposals.
		 * @param cfp - Call for proposal.
		 * @return Returns the vector with all the call for proposals to be sent.
		 */
		protected Vector prepareCfps(ACLMessage cfp) {
			Constants c = new Constants();
			int sup = c.getSup_number();
			Vector v = new Vector();
			for (int i = 0; i < sup; i++) {
				cfp.addReceiver(new AID("sup"+i, false));
			}
			String message = cfpContent();
			cfp.setContent(message);

			System.out.println("[" + myAgent.getLocalName() + "]: " + "Sending CFP to all suppliers");

			v.add(cfp);

			return v;
			
		}
		
		/**
		 * Shows all the cumulatives values -> Y axis
		 * @param supplier - Supplier name
		 */
		private void showCumulatives(String supplier) {
			
			if(Y.get(supplier) == null)
				return;
			
			System.out.println("[" + myAgent.getLocalName() + "]: Cumulatives");
			
			List<Double> cumulatives = Y.get(supplier);
			
			for(Double cumulative : cumulatives)
				System.out.println(cumulative);
			
		}
		
		/**
		 * Shows all the past experiences values (1, 2, 3, ..., N) -> X axis
		 * @param supplier - Supplier name
		 */
		private void showPastExperiences(String supplier) {
			
			if(X.get(supplier) == null)
				return;
			
			System.out.println("[" + myAgent.getLocalName() + "]: Past Experiences (X axis)");
			
			List<Double> past_experiences = X.get(supplier);
			
			for(Double past_experience : past_experiences)
				System.out.println(past_experience);
			
		}
		
		/**
		 * Shows all the betas values (1, 2, 3, ..., N) -> X axis
		 * @param supplier - Supplier name
		 */
		private void showBetas(String supplier) {
			
			if(X.get(supplier) == null)
				return;
			
			System.out.println("[" + myAgent.getLocalName() + "]: Beta0");
			
			List<Double> betas0 = Beta0.get(supplier);
			List<Double> betas1 = Beta1.get(supplier);
			
			for(Double beta0 : betas0)
				System.out.println(beta0);
			
			System.out.println("[" + myAgent.getLocalName() + "]: Beta1");
			
			for(Double beta1 : betas1)
				System.out.println(beta1);
			
		}

		/**
		 * Generates the call for proposal content to be sent.
		 * @return Returns the message to be sent on the call for proposal.
		 */
		private String cfpContent() {
			
			String message = "";
			
			Random rand = new Random();
			
			Integer r = rand.nextInt(3);
			message = message + product.get(r);
			r = rand.nextInt(3);
			message = message + ", " + quantity.get(r);
			r = rand.nextInt(3);
			message = message + ", " + quality.get(r);
			r = rand.nextInt(3);
			message = message + ", " + delivery.get(r);
			System.out.println("cfp message: " + message);
			return message;
		}
		
		/**
		 * Gets the correlation coefficient.
		 * @param supplier - Supplier name.
		 * @return Returns the correlation coefficient.
		 */
		private Double getCoefficient(String supplier) {
			
			List<Double> betas0 = Beta0.get(supplier);
			List<Double> betas1 = Beta1.get(supplier);
			
			Double last_beta0 = betas0.get(betas0.size() - 1);
			Double last_beta1 = betas1.get(betas1.size() - 1);
			
			return last_beta1 + 0.1*last_beta0;
			
		}
		
		/**
		 * Gets the benevolence for a certain supplier.
		 * @param supplier - Supplier name.
		 * @return Returns the benevolence.
		 */
		private Double getBenevolence(String supplier) {
			
			Double coefficient = getCoefficient(supplier);
			Integer num_experiences = X.get(supplier).size();
			List<Double> cumulatives = Y.get(supplier);
			Double cumulative = cumulatives.get(cumulatives.size() - 1);
			
			Double benevolence = 0.5 * coefficient + 0.5 * (cumulative / num_experiences);
			
			return benevolence;
			
		}

		/**
		 * Handles all the responses and then adds the acceptances to the respective vector.
		 */
		protected void handleAllResponses(Vector responses, Vector acceptances) {
			
			//System.out.println("[" + myAgent.getLocalName() + "]: Got " + responses.size() + " responses!");
			
			for (int i = 0; i < responses.size(); i++) {
				
				ACLMessage response = (ACLMessage)responses.get(i);
				ACLMessage msg = response.createReply();
				
				List<String> past_exps_sup;
				String supplier = response.getSender().getLocalName();
				
				if(past_experiences.get(response.getSender().getLocalName()) == null)
					past_exps_sup = new ArrayList<String>();
				else
					past_exps_sup = past_experiences.get(supplier);
					
				msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				
				acceptances.add(msg);
				 // change for "F" or "Fd" or "V"
				
				/*if(X.get(supplier) != null) {
					
					if(X.get(supplier).size() >= 1) {
						
						if(Double.parseDouble(response.getContent()) >= min_accept_trust) {
							
							msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							past_exps_sup.add("F");
							acceptances.add(msg);
							
						} else {
							
							past_exps_sup.add("V");
							msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
							
						}
						
						past_experiences.put(supplier, past_exps_sup);
						
						cumValAgreem(past_exps_sup, supplier);
						
						System.out.println("[" + myAgent.getLocalName() + "]: Benevolence to supplier " + supplier + " = " + getBenevolence(supplier));
					}
					
				} */
				
			}
		}
		
		protected void handleAllResultNotifications(Vector notifications) {
			
			System.out.println("I'm HERE!!");
			
			/*for(Integer i = 0; i < notifications.size(); i++) {
				
				ACLMessage msg = (ACLMessage)notifications.get(i);
				
				System.out.println("NOTIF: "+  msg);
				
			}*/
			
		}
	}

}
