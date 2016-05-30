package Agents;

import java.util.Collection;
import java.util.Random;
import java.util.Vector;
import java.util.function.Supplier;

import Agents.Client.Product;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;;

public class SinalphaClient extends Agent {

	public void setup() {
		addBehaviour(new FIPAContractNetInit(this, new ACLMessage(ACLMessage.CFP)));
	}

	class FIPAContractNetInit extends ContractNetInitiator{
		
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
			Random rand = null;
			int r = rand.nextInt(3);
			message = message + "COTTON ";
			r = rand.nextInt(3);
			message = message + "SMALL ";
			r = rand.nextInt(3);
			message = message + "MEDIUM ";
			r = rand.nextInt(3);
			message = message + "LONG ";
			
			return message;			
		}
		
	 	protected void handleAllResponses(Vector responses, Vector acceptances){
	 		
	 	}
	}
	
}
