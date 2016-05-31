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
		
		addBehaviour(new FIPAContractNetInit(this, new ACLMessage(ACLMessage.CFP)));
		
	}
	
	public SinalphaClient(Double _min_accept_trust) {
		
		min_accept_trust = _min_accept_trust;
		
	}

	class FIPAContractNetInit extends ContractNetInitiator {

		public FIPAContractNetInit(Agent a, ACLMessage msg) {
			super(a, msg);
			//prepareCfps(msg);
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
			int r = rand.nextInt(3);
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
			System.out.println("got " + responses.size() + " responses!");
			

			for (int i = 0; i < responses.size(); i++) {
				ACLMessage msg = ((ACLMessage) responses.get(i)).createReply();
				
				System.out.println(((ACLMessage)responses.get(i)).getContent());
				
				if(true) //change for received trust >= min_accept_trust
					msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				else
					msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
				
				acceptances.add(msg);
			}
		}
	}

}
