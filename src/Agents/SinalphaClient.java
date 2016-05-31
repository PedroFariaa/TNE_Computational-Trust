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
	
	public void setup() {
		
		Object [] args = getArguments();
		
		min_accept_trust = Double.parseDouble((String)args[0]);
		
		addBehaviour(new FIPAContractNetInit(this, new ACLMessage(ACLMessage.CFP)));
		
	}

	class FIPAContractNetInit extends ContractNetInitiator {

		public FIPAContractNetInit(Agent a, ACLMessage msg) {
			super(a, msg);
		}

		protected Vector prepareCfps(ACLMessage cfp) {
			Vector v = new Vector();
			cfp.addReceiver(new AID("supplier1", false));
			String message = "";

			cfpContent(message);
			cfp.setContent(message);

			System.out.println("sent a cfp");

			v.add(cfp);

			return v;
		}

		private String cfpContent(String message) {
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

		//ver documentação
		protected void handleAllResponses(Vector responses, Vector acceptances) {
			
			System.out.println("[" + getAID().getLocalName() + "]: Got " + responses.size() + " responses!");
			
			for (Integer i = 0; i < responses.size(); i++) {
				
				ACLMessage response = (ACLMessage)responses.get(i);
				ACLMessage msg = response.createReply();
				
				Double trust = Double.parseDouble(response.getContent());
				
				System.out.println("[" + getAID().getLocalName() + "]: Received message from " + response.getSender().getLocalName());
				
				if(trust >= min_accept_trust)
					msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				else
					msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
				
				acceptances.add(msg);
			}
		}
	}

}
