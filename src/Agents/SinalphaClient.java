package Agents;

import java.util.Collection;
import java.util.Random;
import java.util.Vector;
import java.util.function.Supplier;

import Agents.Client;
import Utils.Constants;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;;

public class SinalphaClient extends Client {

	public Double min_accept_trust = Constants.MIN_ACCEPT_TRUST;
	
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
		 * Prepares the call for proposals.
		 * @param cfp - Call for proposal.
		 * @return Returns the vector with all the call for proposals to be sent.
		 */
		protected Vector prepareCfps(ACLMessage cfp) {
			
			Vector v = new Vector();
			cfp.addReceiver(new AID("supplier1", false));
			
			String message = cfpContent();
			cfp.setContent(message);

			System.out.println("[" + myAgent.getLocalName() + "]: " + "Sending CFP to supplier 1");

			v.add(cfp);

			return v;
			
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

			return message;
		}

		/**
		 * Handles all the responses and then adds the acceptances to the respective vector.
		 */
		protected void handleAllResponses(Vector responses, Vector acceptances) {
			
			System.out.println("[" + myAgent.getLocalName() + "]: Got " + responses.size() + " responses!");
			
			for (Integer i = 0; i < responses.size(); i++) {
				
				ACLMessage response = (ACLMessage)responses.get(i);
				ACLMessage msg = response.createReply();
				
				Double trust = Double.parseDouble(response.getContent());
				
				System.out.println("[" + getAID().getLocalName() + "]: Received message from " + response.getSender().getLocalName());
				
				if(trust >= min_accept_trust)
					msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				else
					msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
				
				System.out.println("[" + getAID().getLocalName() + "]: Sending response to " + response.getSender().getLocalName());
				
				acceptances.add(msg);
				
			}
		}
	}

}
